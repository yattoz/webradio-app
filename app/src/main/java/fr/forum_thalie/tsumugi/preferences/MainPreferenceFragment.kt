package fr.forum_thalie.tsumugi.preferences

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import fr.forum_thalie.tsumugi.R
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*

class MainPreferenceFragment : PreferenceFragmentCompat() {
    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = context?.getString(R.string.settings)
    }

    @SuppressLint("ApplySharedPref")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceScreen.isIconSpaceReserved = false

        val submitBug = preferenceScreen.findPreference<Preference>("submitBug")
        submitBug!!.setOnPreferenceClickListener {
            val url = getString(R.string.github_url_new_issue)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            true
        }

    }
}