package fr.forum_thalie.tsumugi.ui.programme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.planning.Planning

class ProgrammeDayFragment(day: String) : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_programme_day, container, false)
        viewManager = LinearLayoutManager(context)
        viewAdapter =
            ProgrammeAdapter(Planning.instance.programmes)
        recyclerView = root.findViewById<RecyclerView>(R.id.programme_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        Planning.instance.isProgrammeUpdated.observeForever(isProgrammeUpdatedObserver)
        return root
    }

    private val isProgrammeUpdatedObserver = Observer<Boolean> {
        viewAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Planning.instance.isProgrammeUpdated.removeObserver(isProgrammeUpdatedObserver)
    }

    companion object {
        @JvmStatic
        fun newInstance(day: String) =
            ProgrammeDayFragment(day)
    }
}