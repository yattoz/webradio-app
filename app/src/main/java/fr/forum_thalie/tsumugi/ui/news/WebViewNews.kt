package fr.forum_thalie.tsumugi.ui.news

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView

class WebViewNews(private val webView: WebView) {

    @SuppressLint("SetJavaScriptEnabled")
    fun start(url: String) {

        val webSetting = this.webView.settings
        webSetting.javaScriptEnabled = true
        webSetting.setSupportZoom(true)

        webSetting.textZoom = 100

        webSetting.setSupportMultipleWindows(true)
        // needs to open target="_blank" links as KiwiIRC links have this attribute.
        // shamelessly ripped off https://stackoverflow.com/questions/18187714/android-open-target-blank-links-in-webview-with-external-browser
        this.webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                dialog: Boolean,
                userGesture: Boolean,
                resultMsg: android.os.Message
            ): Boolean {
                val result = view.hitTestResult
                val data = result.extra
                val context = view.context
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(data))
                context.startActivity(browserIntent)
                return false
            }
        }

        webView.loadUrl(url)
    }

}
