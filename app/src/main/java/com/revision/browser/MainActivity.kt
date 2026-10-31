package com.revision.browser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayInputStream

/**
 * Revision — a private, WebView-based Android browser.
 *
 * Real features (not placeholders):
 * - Address/search bar: URLs load directly, anything else searches DuckDuckGo.
 * - Back / forward / reload, plus a simple multi-tab list (in-memory only).
 * - HTTPS upgrade: http:// URLs are rewritten to https://, and the OS
 *   network-security config forbids cleartext traffic.
 * - Tracker/ad blocking in shouldInterceptRequest (see TrackerBlocker).
 * - No location / camera / microphone: permission requests are denied,
 *   and the app does not request those Android permissions at all.
 * - Third-party cookies off, Do Not Track header sent, Safe Browsing on,
 *   mixed content never allowed, file access off.
 * - Clear data button + optional clear-on-exit; Revision itself never
 *   writes visited URLs to disk (no history database, no saved tabs).
 * - Liquid-glass sci-fi UI with entrance / shield-pulse animations.
 * - Long-press drag for the top bar and the quick panel (see
 *   DraggablePanelLayout); positions persist in SharedPreferences.
 *
 * Honest limits are documented in README.md — this is not "unhackable".
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlInput: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var topBar: DraggablePanelLayout
    private lateinit var quickPanel: DraggablePanelLayout
    private lateinit var shieldButton: Button
    private lateinit var clearOnExitCheck: CheckBox

    private val prefs by lazy { getSharedPreferences("revision_prefs", MODE_PRIVATE) }
    private var shieldOn: Boolean = true
    private var blockedThisPage: Int = 0

    // Simple in-memory tabs: a list of URLs, nothing persisted to disk.
    private val tabs = mutableListOf<String>()
    private var currentTabIndex = -1

    private val startPageHtml: String
        get() = """
            <html><head><meta name="viewport" content="width=device-width,initial-scale=1">
            <style>
              body{background:#04061A;color:#fff;font-family:sans-serif;text-align:center;padding:48px 20px}
              h1{color:#00E5FF;letter-spacing:6px;font-size:42px;margin:8px 0}
              p{color:#B8C6E8;line-height:1.6}
              .card{border:1px solid #00E5FF55;border-radius:24px;padding:20px;max-width:520px;margin:24px auto;background:#12244888}
              .ok{color:#2EEA8A}
            </style></head><body>
              <h1>REVISION</h1>
              <p>Private browser &bull; متصفح خاص</p>
              <div class="card">
                <p class="ok">Shield ON &bull; HTTPS only &bull; Trackers blocked</p>
                <p>Type a search or an address in the glass bar above.<br>
                اكتب بحثاً أو عنواناً في الشريط الزجاجي أعلاه.</p>
                <p>Long-press a glass panel, then drag it — like a home-screen icon.<br>
                اضغط مطوّلاً على أي لوحة زجاجية ثم اسحبها.</p>
              </div>
            </body></html>
        """.trimIndent()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        urlInput = findViewById(R.id.urlInput)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        topBar = findViewById(R.id.topBar)
        quickPanel = findViewById(R.id.quickPanel)
        shieldButton = findViewById(R.id.shieldButton)
        clearOnExitCheck = findViewById(R.id.clearOnExitCheck)
        val dragHint: TextView = findViewById(R.id.dragHint)

        shieldOn = prefs.getBoolean("shield_on", true)
        clearOnExitCheck.isChecked = prefs.getBoolean("clear_on_exit", true)

        // --- Draggable panels: positions restore from SharedPreferences ---
        topBar.prefsKey = "drag_topBar"
        quickPanel.prefsKey = "drag_quickPanel"
        topBar.onDragStarted = { dragHint.visibility = View.GONE }
        quickPanel.onDragStarted = { dragHint.visibility = View.GONE }

        // Entrance animation: liquid-glass panels glide + fade in.
        topBar.alpha = 0f
        quickPanel.alpha = 0f
        topBar.animate().alpha(1f).translationYBy(0f).setDuration(600).start()
        quickPanel.animate().alpha(1f).setDuration(700).setStartDelay(150).start()
        dragHint.visibility = View.VISIBLE
        dragHint.postDelayed({ dragHint.visibility = View.GONE }, 5000)

        // --- WebView privacy settings ---
        webView.settings.apply {
            javaScriptEnabled = true // needed for most modern sites; trackers are still filtered
            domStorageEnabled = true
            databaseEnabled = false
            allowFileAccess = false
            allowContentAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            safeBrowsingEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = "$userAgentString Revision/1.0"
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, false)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://")) {
                    // HTTPS upgrade, where feasible.
                    Toast.makeText(this@MainActivity, R.string.https_upgraded, Toast.LENGTH_SHORT).show()
                    loadUrlWithPrivacy(url.replaceFirst("http://", "https://"))
                    return true
                }
                return false
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                if (shieldOn && TrackerBlocker.isBlocked(url)) {
                    blockedThisPage++
                    runOnUiThread { updateStatus() }
                    // Empty response: the tracker/ad request never completes.
                    return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                blockedThisPage = 0
                progressBar.visibility = View.VISIBLE
                if (url != null && !url.startsWith("data:")) urlInput.setText(url)
                updateStatus()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                webView.alpha = 0f
                webView.animate().alpha(1f).setDuration(350).start()
                updateStatus()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                // Deny location / camera / microphone by default — always.
                request?.deny()
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: android.webkit.GeolocationPermissions.Callback?
            ) {
                callback?.invoke(origin, false, false)
            }
        }

        // --- Controls ---
        val go = { navigate(urlInput.text.toString()) }
        findViewById<Button>(R.id.goButton).setOnClickListener { go() }
        urlInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                go(); true
            } else false
        }
        findViewById<Button>(R.id.backButton).setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
        }
        findViewById<Button>(R.id.forwardButton).setOnClickListener {
            if (webView.canGoForward()) webView.goForward()
        }
        findViewById<Button>(R.id.reloadButton).setOnClickListener { webView.reload() }

        shieldButton.setOnClickListener {
            shieldOn = !shieldOn
            prefs.edit().putBoolean("shield_on", shieldOn).apply()
            refreshShieldButton()
            // Sci-fi pulse animation on toggle.
            shieldButton.animate().scaleX(1.15f).scaleY(1.15f).setDuration(120)
                .withEndAction { shieldButton.animate().scaleX(1f).scaleY(1f).setDuration(200).start() }
                .start()
            Toast.makeText(
                this,
                if (shieldOn) R.string.shield_on else R.string.shield_off,
                Toast.LENGTH_SHORT
            ).show()
            if (shieldOn) webView.reload()
        }
        refreshShieldButton()

        findViewById<Button>(R.id.newTabButton).setOnClickListener { openNewTab() }
        findViewById<Button>(R.id.tabsButton).setOnClickListener { showTabsDialog() }
        findViewById<Button>(R.id.clearButton).setOnClickListener { clearBrowsingData() }
        findViewById<Button>(R.id.resetLayoutButton).setOnClickListener {
            topBar.resetPosition()
            quickPanel.resetPosition()
            Toast.makeText(this, R.string.layout_reset, Toast.LENGTH_SHORT).show()
        }
        clearOnExitCheck.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("clear_on_exit", checked).apply()
        }

        // First tab = start page (in-memory only, never saved to disk).
        if (tabs.isEmpty()) openNewTab() else loadUrlWithPrivacy(tabs[currentTabIndex])
    }

    private fun refreshShieldButton() {
        shieldButton.text = getString(if (shieldOn) R.string.shield_on else R.string.shield_off)
        shieldButton.setTextColor(
            getColor(if (shieldOn) R.color.safe_green else R.color.danger)
        )
    }

    private fun updateStatus() {
        statusText.text = if (shieldOn) {
            "Shield ON • Blocked here: $blockedThisPage • Private — no history saved"
        } else {
            "Shield OFF • Private — no history saved"
        }
    }

    /** Resolve typed text to a URL, or to a DuckDuckGo search, then load it. */
    private fun navigate(raw: String) {
        val input = raw.trim()
        if (input.isEmpty()) return
        val url = when {
            input.startsWith("https://") || input.startsWith("http://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> "https://duckduckgo.com/?q=" +
                android.net.Uri.encode(input)
        }
        openInCurrentTab(url)
    }

    private fun openInCurrentTab(url: String) {
        if (currentTabIndex < 0) {
            tabs.add(url); currentTabIndex = 0
        } else {
            tabs[currentTabIndex] = url
        }
        loadUrlWithPrivacy(url)
    }

    private fun openNewTab() {
        tabs.add(START_PAGE_MARKER)
        currentTabIndex = tabs.lastIndex
        loadStartPage()
    }

    private fun loadStartPage() {
        webView.loadDataWithBaseURL(
            "https://revision.local/", startPageHtml, "text/html", "utf-8", null
        )
        urlInput.setText("")
    }

    private fun showTabsDialog() {
        if (tabs.isEmpty()) return
        val labels = tabs.mapIndexed { i, url ->
            val label = if (url == START_PAGE_MARKER) "Revision — Start" else url
            "${if (i == currentTabIndex) "● " else ""}Tab ${i + 1}: $label"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Tabs (${tabs.size})")
            .setItems(labels) { _, which ->
                currentTabIndex = which
                val url = tabs[which]
                if (url == START_PAGE_MARKER) loadStartPage() else loadUrlWithPrivacy(url)
            }
            .setNeutralButton("Close tab") { _, _ ->
                if (tabs.size > 1) {
                    tabs.removeAt(currentTabIndex)
                    currentTabIndex = minOf(currentTabIndex, tabs.lastIndex)
                    val url = tabs[currentTabIndex]
                    if (url == START_PAGE_MARKER) loadStartPage() else loadUrlWithPrivacy(url)
                }
            }
            .show()
    }

    /** Load with a Do Not Track header. WebView does not add DNT itself. */
    private fun loadUrlWithPrivacy(url: String) {
        val finalUrl = if (url.startsWith("http://")) {
            Toast.makeText(this, R.string.https_upgraded, Toast.LENGTH_SHORT).show()
            url.replaceFirst("http://", "https://")
        } else url
        webView.loadUrl(finalUrl, mapOf("DNT" to "1"))
    }

    private fun clearBrowsingData() {
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        Toast.makeText(this, R.string.data_cleared, Toast.LENGTH_SHORT).show()
        loadStartPage()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (clearOnExitCheck.isChecked) {
            // Best-effort clear on exit; the WebView is being torn down.
            try {
                webView.clearCache(true)
                webView.clearHistory()
                CookieManager.getInstance().removeAllCookies(null)
                WebStorage.getInstance().deleteAllData()
            } catch (e: Exception) {
                // Never crash on exit cleanup.
            }
        }
        super.onDestroy()
    }

    companion object {
        private const val START_PAGE_MARKER = "__revision_start__"
    }
}
