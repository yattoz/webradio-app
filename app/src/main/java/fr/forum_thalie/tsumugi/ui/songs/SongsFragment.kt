package fr.forum_thalie.tsumugi.ui.songs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.ui.APagerAdapter
import fr.forum_thalie.tsumugi.ui.programme.ProgrammeDayFragment
import fr.forum_thalie.tsumugi.ui.songs.queuelp.LastPlayedFragment

class SongsFragment : Fragment() {

    private lateinit var adapter : APagerAdapter
    private lateinit var root: View
    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_songs, container, false)
        viewPager = root.findViewById(R.id.tabPager)
        adapter = APagerAdapter(
            childFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        adapter.addFragment(LastPlayedFragment.newInstance(), getString(R.string.lp))
        // You can add more fragments to the adapter, to display more information (for example with R/a/dio, queue, request, faves...)

        viewPager.adapter = adapter

        val tabLayout : TabLayout = root.findViewById(R.id.tabLayout)
        tabLayout.setupWithViewPager(viewPager)
        //[REMOVE LOG CALLS]//[REMOVE LOG CALLS]Log.d(tag, "SongFragment view created")

        return root
    }

}