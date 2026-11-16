package com.tanjid.hisabapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * Minimal WebView wrapper for Hisab App.
 *
 * Setup:
 * 1. Copy index.html to: app/src/main/assets/index.html
 * 2. Put this file at: app/src/main/java/com/tanjid/hisabapp/MainActivity.kt
 * 3. In AndroidManifest.xml, set this Activity as LAUNCHER.
 * 4. No internet permission is needed — the app works 100% offline.
 *    Login, data and all calculations use localStorage inside the WebView.
 */
class MainActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        setContentView(webView)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true

        // Keep all links inside the app instead of opening a browser.
        webView.webViewClient = WebViewClient()

        // Load the local Hisab App file from assets.
        webView.loadUrl("file:///android_asset/index.html")
    }

    // Optional: press Back to go back inside the app history.
    // If you add this, keep a reference to the WebView as a field.
    // override fun onBackPressed() {
    //     if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    // }
}
