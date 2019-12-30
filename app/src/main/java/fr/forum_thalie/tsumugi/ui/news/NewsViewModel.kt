package fr.forum_thalie.tsumugi.ui.news

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.prof.rssparser.Parser
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.newsDateTimePattern
import fr.forum_thalie.tsumugi.tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min


class NewsViewModel : ViewModel() {

    var screenRatio: Int = 100
    lateinit var root: View
    var webView: WebView? = null
    var webViewNews: WebViewNews? = null
    var isPreLoadingNews = false

    val newsArray : ArrayList<News> = ArrayList()
    var isWebViewLoaded = false

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun fetch(root: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null, viewAdapter: RecyclerView.Adapter<*>? = null, c: Context, isPreloading: Boolean = false)
    {
        val urlToScrape = c.getString(R.string.rss_url)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            return // the RSS Parser does not support API20- because of no TLS v1.2

        val maxNumberOfArticles = 5
        coroutineScope.launch(Dispatchers.Main) {
            //[REMOVE LOG CALLS]//[REMOVE LOG CALLS]Log.d(tag, "launching coroutine")
                val parser = Parser()
            try {
                val articleList = parser.getArticles(urlToScrape)
                newsArray.clear()
                for (i in 0 until min(articleList.size, maxNumberOfArticles)) {
                    val item = articleList[i]
                    //[REMOVE LOG CALLS]//[REMOVE LOG CALLS]Log.d(tag, "i = $i / ${articleList.size}")
                    val news = News()
                    news.title = item.title ?: ""
                    news.link = item.link ?: urlToScrape
                    news.author = item.author ?: ""
                    news.text = item.content ?: ""
                    news.header = item.description ?: ""

                    val formatter6 = SimpleDateFormat(newsDateTimePattern, Locale.ENGLISH)
                    val dateString = item.pubDate.toString()
                    //[REMOVE LOG CALLS]//[REMOVE LOG CALLS]Log.d(tag, "$news --- $dateString")

                    news.date = formatter6.parse(dateString) ?: Date(0)

                    newsArray.add(news)
                }
                // The list contains all article's data. For example you can use it for your adapter.
                root?.isRefreshing = false
                isPreLoadingNews = isPreloading
                viewAdapter?.notifyDataSetChanged()
            }catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }
}