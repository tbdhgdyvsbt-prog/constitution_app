package com.revision.browser

/**
 * Small, honest tracker/ad domain blocklist used by shouldInterceptRequest.
 * It is a curated list, not a complete shield: new tracker domains appear
 * constantly, so this reduces tracking but cannot claim to stop all of it.
 * قائمة حظر متتبّعات معروفة — تقلّل التتبّع ولا تدّعي منعه كلياً.
 */
object TrackerBlocker {
    private val blockedHosts = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "google-analytics.com",
        "googletagmanager.com",
        "analytics.google.com",
        "facebook.net",
        "connect.facebook.net",
        "ads.yahoo.com",
        "scorecardresearch.com",
        "hotjar.com",
        "mixpanel.com",
        "segment.io",
        "amplitude.com",
        "criteo.com",
        "outbrain.com",
        "taboola.com",
        "adnxs.com",
        "adsystem.com",
        "moatads.com",
        "newrelic.com",
        "fullstory.com",
        "mouseflow.com",
        "clarity.ms",
    )

    private val blockedSubstrings = listOf(
        "/ads/",
        "/track",
        "tracking",
        "analytics",
        "beacon",
        "pixel.gif",
    )

    fun isBlocked(url: String): Boolean {
        val lower = url.lowercase()
        val host = try {
            android.net.Uri.parse(lower).host ?: lower
        } catch (e: Exception) {
            lower
        }
        // Block if the host ends with (or equals) a known tracker host,
        // or the URL path looks like a tracking beacon.
        if (blockedHosts.any { h -> host == h || host.endsWith(".$h") }) return true
        // tiktok.com itself is a site users may visit; only block its beacon paths.
        if (host.endsWith("tiktok.com") && blockedSubstrings.any { lower.contains(it) }) return true
        if (host.contains("ads.") || host.startsWith("ads-")) return true
        return false
    }
}
