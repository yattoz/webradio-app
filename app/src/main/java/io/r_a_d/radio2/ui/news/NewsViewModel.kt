package io.r_a_d.radio2.ui.news

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import io.r_a_d.radio2.Async
import io.r_a_d.radio2.tag
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NewsViewModel : ViewModel() {

    val newsArray : ArrayList<News> = ArrayList()

    private val urlToScrape = "https://r-a-d.io/api/news"

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

    fun fetch(root: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null, viewAdapter: RecyclerView.Adapter<*>? = null)
    {
        val post : (parameter: Any?) -> Unit = {
            root?.isRefreshing = false
            viewAdapter?.notifyDataSetChanged()
        }
        Async(scrape, post)
    }
}