package fr.forum_thalie.tsumugi.preferences

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.preferenceStore

class CustomizeFragment : PreferenceFragmentCompat() {
    override fun onAttach(context: Context) {
        (activity as AppCompatActivity).supportActionBar?.title = context.getString(R.string.customizeAppBehavior)
        super.onAttach(context)
    }

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


        val fetchPeriod = preferenceScreen.findPreference<ListPreference>("fetchPeriod")
        fetchPeriod?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        fetchPeriod?.setOnPreferenceChangeListener { _, newValue ->
            val builder1 = AlertDialog.Builder(context!!)
            if (Integer.parseInt(newValue as String) == 0)
                builder1.setMessage(R.string.restart_the_app)
            else
                builder1.setMessage(R.string.restart_the_app)
            builder1.setCancelable(true)

            builder1.setPositiveButton("Close" ) { dialog, _ ->
                dialog.cancel()
            }

            val alert11 = builder1.create()
            alert11.show()
            true
        }

    }
}