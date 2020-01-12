package fr.forum_thalie.tsumugi.ui.news

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import fr.forum_thalie.tsumugi.R

class NewsFragment : Fragment() {

    private lateinit var newsViewModel: NewsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
        {
            newsViewModel =
            ViewModelProviders.of(this).get(NewsViewModel::class.java)

            if (!newsViewModel.isWebViewLoaded)
            {
                try {
                    newsViewModel.root = inflater.inflate(R.layout.fragment_news, container, false)
                    newsViewModel.webView = newsViewModel.root.findViewById(R.id.news_webview)
                    newsViewModel.webViewNews = WebViewNews(newsViewModel.webView as WebView)
                    newsViewModel.webViewNews!!.start(getString(R.string.website_url))
                } catch (e: Exception) {
                    newsViewModel.root = inflater.inflate(R.layout.fragment_error_webview, container, false)
                }

                newsViewModel.isWebViewLoaded = true
                //[REMOVE LOG CALLS]Log.d(tag, "webview created")
            } else {
                //[REMOVE LOG CALLS]Log.d(tag, "webview already created!?")
            }

            newsViewModel.root.addOnLayoutChangeListener(orientationLayoutListener)
            return newsViewModel.root
        }

        newsViewModel =
                ViewModelProviders.of(this).get(NewsViewModel::class.java)

        newsViewModel.root = inflater.inflate(R.layout.fragment_news, container, false) as SwipeRefreshLayout

        viewManager = LinearLayoutManager(context)
        viewAdapter = NewsAdapter(newsViewModel.newsArray, context!!, newsViewModel)
        recyclerView = newsViewModel.root.findViewById<RecyclerView>(R.id.news_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        (newsViewModel.root as SwipeRefreshLayout).setOnRefreshListener {
            newsViewModel.fetch((newsViewModel.root as SwipeRefreshLayout), viewAdapter, context!!)
        }

        newsViewModel.root.addOnLayoutChangeListener(orientationLayoutListener)
        return newsViewModel.root
    }

    private val orientationLayoutListener : View.OnLayoutChangeListener = View.OnLayoutChangeListener { _: View, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->

        val viewHeight = (newsViewModel.root.rootView?.height ?: 1)
        val viewWidth = (newsViewModel.root.rootView?.width ?: 1)

        val newRatio = if (viewWidth > 0)
            (viewHeight*100)/viewWidth
        else
            100

        if (newsViewModel.screenRatio != newRatio) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
            newsViewModel.fetch((newsViewModel.root as SwipeRefreshLayout), viewAdapter, context!!)
            newsViewModel.screenRatio = newRatio
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        newsViewModel =
            ViewModelProviders.of(this).get(NewsViewModel::class.java)

        newsViewModel.fetch(c = context!!, isPreloading = true)
        //[REMOVE LOG CALLS]Log.d(tag, "news fetched onCreate")
        super.onCreate(savedInstanceState)
    }
}