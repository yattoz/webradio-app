package io.r_a_d.radio2.ui.songs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import io.r_a_d.radio2.R
import io.r_a_d.radio2.preferenceStore
import io.r_a_d.radio2.ui.songs.queuelp.LastPlayedFragment
import io.r_a_d.radio2.ui.songs.queuelp.QueueFragment
import io.r_a_d.radio2.ui.songs.request.FavoritesFragment
import io.r_a_d.radio2.ui.songs.request.RequestFragment
import io.r_a_d.radio2.ui.songs.request.Requestor

class SongsFragment : Fragment() {

    private lateinit var adapter : SongsPagerAdapter
    private lateinit var snackBar : Snackbar
    private lateinit var root: View
    private lateinit var viewPager: ViewPager


    private val snackBarTextObserver: Observer<String?> = Observer {
        if (Requestor.instance.snackBarText.value != "")
        {
            val snackBarLength = if (preferenceStore.getBoolean("snackbarPersistent", true))
                Snackbar.LENGTH_INDEFINITE
                else Snackbar.LENGTH_LONG
            snackBar = Snackbar.make(viewPager, "", snackBarLength)

            if (snackBarLength == Snackbar.LENGTH_INDEFINITE)
            snackBar.setAction("OK") {
                snackBar.dismiss()
            }

            snackBar.behavior = BaseTransientBottomBar.Behavior().apply {
                setSwipeDirection(BaseTransientBottomBar.Behavior.SWIPE_DIRECTION_ANY)
            }

            val snackBarView = snackBar.view
            val textView =
                snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            if (Requestor.instance.addRequestMeta != "")
                textView.maxLines = 4
            else
                textView.maxLines = 2
            snackBar.setText((it as CharSequence))
            snackBar.show()
            Requestor.instance.snackBarText.value = "" // resetting afterwards to avoid re-triggering it when we enter again the fragment
            Requestor.instance.addRequestMeta = ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_songs, container, false)
        viewPager = root.findViewById(R.id.tabPager)
        adapter = SongsPagerAdapter(childFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        adapter.addFragment(LastPlayedFragment.newInstance(), "last played")
        adapter.addFragment(QueueFragment.newInstance(), "queue")
        adapter.addFragment(RequestFragment.newInstance(), "request")
        adapter.addFragment(FavoritesFragment.newInstance(), "Favorites")

        viewPager.adapter = adapter

        val tabLayout : TabLayout = root.findViewById(R.id.tabLayout)
        tabLayout.setupWithViewPager(viewPager)
        Log.d(tag, "SongFragment view created")

        Requestor.instance.snackBarText.observeForever(snackBarTextObserver)

        return root
    }

    override fun onDestroyView() {
        Requestor.instance.snackBarText.removeObserver(snackBarTextObserver)
        super.onDestroyView()
    }

}