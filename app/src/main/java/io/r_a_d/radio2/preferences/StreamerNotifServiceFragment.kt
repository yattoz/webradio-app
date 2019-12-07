package io.r_a_d.radio2.preferences

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import io.r_a_d.radio2.R
import io.r_a_d.radio2.preferenceStore
import io.r_a_d.radio2.streamerNotificationService.WorkerStore
import io.r_a_d.radio2.streamerNotificationService.startStreamerMonitor
import io.r_a_d.radio2.streamerNotificationService.stopStreamerMonitor

class StreamerNotifServiceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.streamer_notif_service_preferences, rootKey)


        val streamerPeriod = preferenceScreen.findPreference<Preference>("streamerMonitorPeriodPref")

        val streamerNotification = preferenceScreen.findPreference<Preference>("newStreamerNotification")
        streamerNotification?.setOnPreferenceChangeListener { _, newValue ->
            if ((newValue as Boolean)) {
                val builder1 = AlertDialog.Builder(context!!)
                builder1.setMessage(R.string.warningStreamerNotif)
                builder1.setCancelable(false)
                builder1.setPositiveButton(
                    "Yes"
                ) { dialog, _ ->
                    startStreamerMonitor(context!!, force = true) // force enabled because the preference value is not yet set when running this callback.
                    streamerPeriod?.isEnabled = true
                    dialog.cancel()
                }

                builder1.setNegativeButton(
                    "No"
                ) { dialog, _ ->

                    stopStreamerMonitor(context!!)
                    (streamerNotification as SwitchPreferenceCompat).isChecked = false
                    dialog.cancel()
                }

                val alert11 = builder1.create()
                alert11.show()
            }
            else {
                stopStreamerMonitor(context!!)
                streamerPeriod?.isEnabled = false
                WorkerStore.instance.isServiceStarted = false
            }
            true
        }

        streamerPeriod?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        streamerPeriod?.isEnabled = preferenceStore.getBoolean("newStreamerNotification", true)
        streamerPeriod?.setOnPreferenceChangeListener { _, newValue ->
            WorkerStore.instance.tickerPeriod = (Integer.parseInt(newValue as String)).toLong() * 60
            // this should be sufficient, the next alarm schedule should take the new tickerPeriod.
            true
        }


    }
}