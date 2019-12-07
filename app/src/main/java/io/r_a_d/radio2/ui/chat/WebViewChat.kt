package io.r_a_d.radio2.ui.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView

class WebViewChat(private val webView: WebView) {

        @SuppressLint("SetJavaScriptEnabled")
        fun start() {

            val webSetting = this.webView.settings
            webSetting.javaScriptEnabled = true
            webSetting.setSupportZoom(false)

            /* TODO: in the future, it could be nice to have a parameters screen where you can:
         - Set the text zoom
         - Set your username (to not type it every time, would it be possible?)
         - Hide the chat?
         - do more? */
            webSetting.textZoom = 90

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

            webView.loadUrl("file:///android_asset/chat.html")
        }

    }
