package io.r_a_d.radio2.ui.songs.queuelp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.r_a_d.radio2.R
import io.r_a_d.radio2.playerstore.PlayerStore

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LastPlayedFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LastPlayedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class LastPlayedFragment : Fragment() {

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
        viewAdapter = SongAdaptater(PlayerStore.instance.lp)

        recyclerView = root.findViewById<RecyclerView>(R.id.queue_lp_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        PlayerStore.instance.isLpUpdated.observeForever(queueObserver)

        return root
    }

    override fun onDestroyView() {
        PlayerStore.instance.isLpUpdated.removeObserver(queueObserver)
        super.onDestroyView()
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener

    companion object {
        @JvmStatic
        fun newInstance() = LastPlayedFragment()
    }
}
