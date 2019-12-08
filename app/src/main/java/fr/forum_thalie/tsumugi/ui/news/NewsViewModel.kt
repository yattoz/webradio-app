package fr.forum_thalie.tsumugi.ui.news

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.prof.rssparser.Parser
import fr.forum_thalie.tsumugi.Async
import fr.forum_thalie.tsumugi.tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min


class NewsViewModel : ViewModel() {

    val newsArray : ArrayList<News> = ArrayList()

    private val urlToScrape = "https://tsumugi.forum-thalie.fr/?feed=rss2"

    private val scrape : (Any?) -> Unit =
    {
        val t = URL(urlToScrape).readText()
        val result = JSONArray(t)
        newsArray.clear()
        for (n in 0 until result.length())
        {
            val news = News()
            news.title = (result[n] as JSONObject).getString("title")
            news.author = (result[n] as JSONObject).getJSONObject("author").getString("user")
            news.text = (result[n] as JSONObject).getString("text")
            news.header = (result[n] as JSONObject).getString("header")

            val formatter6 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            news.date = formatter6.parse((result[n] as JSONObject).getString("updated_at")) ?: Date()

            Log.d(tag, "$news")
            newsArray.add(news)
        }
    }

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun fetch(root: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null, viewAdapter: RecyclerView.Adapter<*>? = null)
    {
        val maxNumberOfArticles = 5
        coroutineScope.launch(Dispatchers.Main) {
            Log.d(tag, "launching coroutine")
            try {
                val parser = Parser()
                val articleList = parser.getArticles(urlToScrape)
                newsArray.clear()
                for (i in 0 until min(articleList.size, maxNumberOfArticles))
                {
                    val item = articleList[i]
                    Log.d(tag, "i = $i / ${articleList.size}")
                    val news = News()
                    news.title = item.title ?: ""
                    news.link = item.link ?: urlToScrape
                    news.author = item.author ?: ""
                    news.text = item.content ?: ""
                    news.header = item.description ?: ""

                    //val formatter6 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                    //news.date = formatter6.parse(item.pubDate ?: Date().toString()) ?: Date()

                    Log.d(tag, "$news - ${item.pubDate}")
                    newsArray.add(news)
                }
                // The list contains all article's data. For example you can use it for your adapter.
                root?.isRefreshing = false
                viewAdapter?.notifyDataSetChanged()
            } catch (e: Exception) {
                // Handle the exception
            }
        }
    }
}