package io.r_a_d.radio2.ui.songs.queuelp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.r_a_d.radio2.R
import io.r_a_d.radio2.playerstore.PlayerStore
import io.r_a_d.radio2.playerstore.Song

class QueueFragment : Fragment(){
    private val lastPlayedFragmentTag = this::class.java.name

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager


    private val queueObserver = Observer<Boolean> {
        Log.d(tag, lastPlayedFragmentTag + "queue changed")
        viewAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_last_played, container, false)

        viewManager = LinearLayoutManager(context)
        viewAdapter = SongAdaptater(
            if (PlayerStore.instance.queue.isEmpty())
                ArrayList<Song>(listOf((Song("No queue - "))))
            else
                PlayerStore.instance.queue
        )

        recyclerView = root.findViewById<RecyclerView>(R.id.queue_lp_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        PlayerStore.instance.isQueueUpdated.observeForever(queueObserver)

        return root
    }

    override fun onDestroyView() {
        PlayerStore.instance.isQueueUpdated.removeObserver(queueObserver)
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance() = QueueFragment()
    }
}