package fr.forum_thalie.tsumugi.ui.programme

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
import fr.forum_thalie.tsumugi.planning.Planning
import fr.forum_thalie.tsumugi.ui.APagerAdapter
import fr.forum_thalie.tsumugi.weekdays
import java.util.*

class ProgrammeFragment : Fragment() {

    private lateinit var adapter : APagerAdapter
    private lateinit var root: View
    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_programme, container, false)
        viewPager = root.findViewById(R.id.dayTabPager)
        adapter = APagerAdapter(childFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        // You can add more fragments to the adapter, to display more information (for example with R/a/dio, queue, request, faves...)
        weekdays.forEach {
            adapter.addFragment(ProgrammeDayFragment.newInstance(it), it)
        }

        viewPager.adapter = adapter
        val todaySundayFirst = Calendar.getInstance(Planning.instance.timeZone).get(Calendar.DAY_OF_WEEK) - 1
        viewPager.currentItem = (todaySundayFirst - 1 + 7)%7 // don't do modulos on negative, seems like it's weird

        val tabLayout : TabLayout = root.findViewById(R.id.dayTabLayout)
        tabLayout.setupWithViewPager(viewPager)
        //[REMOVE LOG CALLS]//[REMOVE LOG CALLS]Log.d(tag, "SongFragment view created")

        return root
    }

}