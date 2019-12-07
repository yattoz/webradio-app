package io.r_a_d.radio2.ui.chat

import android.view.View
import android.webkit.WebView
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {
    lateinit var root: View
    var webView: WebView? = null
    var isChatLoaded = false
    var webViewChat: WebViewChat? = null
}