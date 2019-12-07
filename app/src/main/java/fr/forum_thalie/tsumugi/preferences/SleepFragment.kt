package fr.forum_thalie.tsumugi.preferences

import android.os.Bundle
import androidx.preference.*
import fr.forum_thalie.tsumugi.R
import android.text.InputType
import fr.forum_thalie.tsumugi.alarm.RadioSleeper


class SleepFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sleep_preference, rootKey)

        val durationBeforeSleep = findPreference<EditTextPreference>("sleepDuration")
        val isSleeping = findPreference<SwitchPreferenceCompat>("isSleeping")

        isSleeping?.setOnPreferenceChangeListener { _, newValue ->
            if (!(newValue as Boolean))
                RadioSleeper.instance.cancelSleep(context!!)
            else
                RadioSleeper.instance.setSleep(context!!, isForce = true)

            true
        }


        durationBeforeSleep?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }

        durationBeforeSleep?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        durationBeforeSleep?.setOnPreferenceChangeListener {_, newValue ->
            val time = Integer.parseInt(newValue as String)
            if (time > 0)
            {
                RadioSleeper.instance.setSleep(context!!, isForce = true, forceDuration = time.toLong())
                isSleeping?.isChecked = true
                true
            } else {
                false
            }
        }
    }
}