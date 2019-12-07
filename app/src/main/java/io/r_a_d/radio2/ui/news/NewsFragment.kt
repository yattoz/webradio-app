package io.r_a_d.radio2.ui.news

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.r_a_d.radio2.R

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
        newsViewModel =
                ViewModelProviders.of(this).get(NewsViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_news, container, false) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout

        viewManager = LinearLayoutManager(context)
        viewAdapter = NewsAdapter(newsViewModel.newsArray)
        recyclerView = root.findViewById<RecyclerView>(R.id.news_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        root.setOnRefreshListener {

            newsViewModel.fetch(root, viewAdapter)

        }

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        newsViewModel =
            ViewModelProviders.of(this).get(NewsViewModel::class.java)

        newsViewModel.fetch()
        Log.d(tag, "news fetched onCreate")
        super.onCreate(savedInstanceState)
    }
}