package com.revision.browser

import android.app.Application
import android.webkit.CookieManager

/**
 * App-level privacy defaults: no backup (so browsing state is not copied
 * to a cloud backup), and third-party cookies are off by default.
 */
class RevisionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(null, false)
        }
    }
}
