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
import fr.forum_thalie.tsumugi.planning.Programme
import fr.forum_thalie.tsumugi.weekdays
import kotlinx.android.synthetic.main.fragment_programme.*

class ProgrammeDayFragment(private val day: String) : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val programmeOfTheDay: ArrayList<Programme> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_programme_day, container, false)
        Planning.instance.programmes.forEach {
            if (it.isThisDay(day = weekdays.indexOf(day)))
                programmeOfTheDay.add(it)
        }
        viewManager = LinearLayoutManager(context)
        viewAdapter =
            ProgrammeAdapter(programmeOfTheDay, day)
        recyclerView = root.findViewById<RecyclerView>(R.id.programme_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        Planning.instance.currentProgramme.observeForever(isProgrammeUpdatedObserver)
        return root
    }

    private val isProgrammeUpdatedObserver = Observer<String> {
        programmeOfTheDay.clear()
        Planning.instance.programmes.forEach {
            if (it.isThisDay(day = weekdays.indexOf(day)))
                programmeOfTheDay.add(it)
        }
        viewAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Planning.instance.currentProgramme.removeObserver(isProgrammeUpdatedObserver)
    }

    companion object {
        @JvmStatic
        fun newInstance(day: String) =
            ProgrammeDayFragment(day)
    }
}