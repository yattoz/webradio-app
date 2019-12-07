package io.r_a_d.radio2.preferences

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.r_a_d.radio2.R
import android.annotation.SuppressLint
import androidx.preference.*

class MainPreferenceFragment : PreferenceFragmentCompat() {

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