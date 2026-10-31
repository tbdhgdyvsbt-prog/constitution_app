package com.revision.browser

import android.app.Application
import android.webkit.CookieManager

/**
 * App-level privacy defaults: no backup (so browsing state is not copied
 * to a cloud backup), and first-party cookies are accepted.
 *
 * Third-party cookies are NOT set here: setAcceptThirdPartyCookies()
 * is a per-WebView policy and requires a real WebView instance.
 * Passing null there crashes the app at launch, before any screen
 * appears. The real WebView in MainActivity already calls
 * setAcceptThirdPartyCookies(webView, false), and apps targeting
 * API 21+ already default to disallowing third-party cookies.
 * Cookie work here is also wrapped so a WebView-provider problem
 * can never crash the app before it starts.
 */
class RevisionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            CookieManager.getInstance().setAcceptCookie(true)
        } catch (e: Exception) {
            // Best effort only: cookie defaults must never crash launch.
        }
    }
}
