package fr.forum_thalie.tsumugi.preferences

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.preferenceStore

class CustomizeFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.customize_preferences, rootKey)


        val snackbarPersistent = preferenceScreen.findPreference<SwitchPreferenceCompat>("snackbarPersistent")
        snackbarPersistent!!.summary = if (preferenceStore.getBoolean("snackbarPersistent", false))
            getString(R.string.snackbarPersistent)
        else
            getString(R.string.snackbarNonPersistent)
        snackbarPersistent.setOnPreferenceChangeListener { preference, newValue ->
            if (newValue as Boolean)
                preference.setSummary(R.string.snackbarPersistent)
            else
                preference.setSummary(R.string.snackbarNonPersistent)
            true
        }

        val splitLayout = preferenceScreen.findPreference<SwitchPreferenceCompat>("splitLayout")
        splitLayout!!.summary = if (preferenceStore.getBoolean("splitLayout", true))
            getString(R.string.split_layout)
        else
            getString(R.string.not_split_layout)
        splitLayout.setOnPreferenceChangeListener { preference, newValue ->
            if (newValue as Boolean)
                preference.setSummary(R.string.split_layout)
            else
                preference.setSummary(R.string.not_split_layout)
            true
        }

    }
}