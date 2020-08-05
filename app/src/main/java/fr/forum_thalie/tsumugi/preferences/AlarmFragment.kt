package fr.forum_thalie.tsumugi.preferences

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.*
import fr.forum_thalie.tsumugi.*
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.alarm.RadioAlarm
import fr.forum_thalie.tsumugi.playerstore.PlayerStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.max
import kotlin.math.min


class AlarmFragment : PreferenceFragmentCompat() {

    override fun onAttach(context: Context) {
        (activity as AppCompatActivity).supportActionBar?.title = context.getString(R.string.setupAlarmClock)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.alarm_preferences, rootKey)

        val timeSet = findPreference<Preference>("timeSet")
        val isWakingUp = findPreference<SwitchPreferenceCompat>("isWakingUp")
        val alarmDays = findPreference<MultiSelectListPreference>("alarmDays")
        val snoozeDuration = findPreference<ListPreference>("snoozeDuration")

        alarmDays?.entries = weekdaysArray
        alarmDays?.entryValues = weekdaysArray
        alarmDays?.setDefaultValue(weekdaysArray)

        fun updateIsWakingUpSummary(preference: SwitchPreferenceCompat?, newValue: Boolean? = true,  forceTime: Int? = null, forceDays: Set<String>? = null)
        {
            val dateLong = RadioAlarm.instance.findNextAlarmTime(context!!, forceTime, forceDays)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dateLong
            if (newValue == true && calendar.timeInMillis > 0)
            {
                val fullWeekOrdered = weekdaysSundayFirst
                preference?.summary = getString(R.string.next_alarm) + " ${fullWeekOrdered[calendar.get(Calendar.DAY_OF_WEEK) - 1]} ${if (calendar.get(Calendar.HOUR_OF_DAY) < 10) "0" else ""}${calendar.get(Calendar.HOUR_OF_DAY)}:${if (calendar.get(Calendar.MINUTE) < 10) "0" else ""}${calendar.get(Calendar.MINUTE)}"
            } else if (newValue == true)
                preference?.summary = getString(R.string.select_one_day)
            else
            {
                preference?.summary = getString(R.string.no_alarm_set)
            }
        }


        val hourOfDayDefault = 7
        val minuteDefault = 0
        if (!PreferenceManager.getDefaultSharedPreferences(context!!).contains("alarmTimeFromMidnight"))
        {
            PreferenceManager.getDefaultSharedPreferences(context!!).edit {
                putInt("alarmTimeFromMidnight", (60*hourOfDayDefault+minuteDefault))
                commit()
            }
        }
        val time = PreferenceManager.getDefaultSharedPreferences(context!!).getInt("alarmTimeFromMidnight", (60*hourOfDayDefault+minuteDefault))
        val hourOfDay = time / 60
        val minute = time % 60
        timeSet?.summary = "${if (hourOfDay < 10) "0" else ""}$hourOfDay:${if (minute < 10) "0" else ""}$minute"


        timeSet?.setOnPreferenceClickListener {
            val timePickerOnTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                PreferenceManager.getDefaultSharedPreferences(context!!).edit {
                    putInt("alarmTimeFromMidnight", (60*hourOfDay+minute))
                    commit()
                }
                timeSet.summary = "${if (hourOfDay < 10) "0" else ""}$hourOfDay:${if (minute < 10) "0" else ""}$minute"
                RadioAlarm.instance.cancelAlarm(context!!)
                RadioAlarm.instance.setNextAlarm(context!!, isForce = true, forceTime = hourOfDay*60+minute)
                updateIsWakingUpSummary(isWakingUp, isWakingUp?.isChecked, forceTime = hourOfDay*60+minute)
            }
            val timeNew = PreferenceManager.getDefaultSharedPreferences(context!!).getInt("alarmTimeFromMidnight", 7*60)
            val hourOfDayNew = timeNew / 60
            val minuteNew = timeNew % 60
            val timePicker = TimePickerDialog(context, timePickerOnTimeSetListener, hourOfDayNew, minuteNew, true)

            timePicker.show()
            true
        }

        fun updateDays(preference : MultiSelectListPreference?, newValue : Set<String>?)
        {
            val fullWeek = weekdays.toSet()
            val workingWeek = weekdays.toSet().minusElement(weekdays.last()).minusElement(weekdays.elementAt(weekdays.size - 2))
            val listOfDays = when (newValue) {
                fullWeek -> context!!.getString(R.string.every_day)
                workingWeek -> context!!.getString(R.string.working_days)
                else -> {
                    // build ORDERED LIST of days... I don't know why the original one is in shambles!!
                    val fullWeekOrdered = weekdays
                    val selectedDays = ArrayList<String>()
                    for (item in fullWeekOrdered) {
                        if (newValue!!.contains(item))
                            selectedDays.add(item)
                    }
                    "$selectedDays".drop(1).dropLast(1) // dropping '[' and ']'
                }
            }
            preference?.summary = listOfDays
        }

        updateDays(alarmDays, PreferenceManager.getDefaultSharedPreferences(context).getStringSet("alarmDays", setOf()))
        alarmDays?.setOnPreferenceChangeListener { preference, newValue ->
            @Suppress("UNCHECKED_CAST")
            updateDays(preference as MultiSelectListPreference, newValue as Set<String>)
            RadioAlarm.instance.cancelAlarm(context!!)
            RadioAlarm.instance.setNextAlarm(context!!, isForce = true, forceDays = newValue)
            updateIsWakingUpSummary(isWakingUp, isWakingUp?.isChecked, forceDays = newValue)
            true
        }

        updateIsWakingUpSummary(isWakingUp, isWakingUp?.isChecked)

        isWakingUp?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean)
                RadioAlarm.instance.setNextAlarm(context!!, isForce = true)
            else
                RadioAlarm.instance.cancelAlarm(context!!)
            timeSet?.isEnabled = newValue
            alarmDays?.isEnabled = newValue
            snoozeDuration?.isEnabled = newValue
            updateIsWakingUpSummary(isWakingUp, newValue)
            true
        }


        val testAlarmVolume: Preference? = findPreference("testAlarmVolume")
        testAlarmVolume!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {

            // get previous state: if it's playing, we'll resume playing as multimedia; if it was stopped, we'll stop
            val isPlayingMultimedia: Boolean = PlayerStore.instance.isPlaying.value ?: false

            val builder1 = AlertDialog.Builder(requireContext())
            builder1.setTitle(R.string.test_alarm_volume)
            builder1.setMessage(R.string.popupTestAlarmVolume)
            builder1.setCancelable(false)
            builder1.setPositiveButton(
                getString(R.string.finished)
            ) { dialog, _ ->
                // put the radio back to media sound, or off if it was off.
                if (isPlayingMultimedia)
                {
                    actionOnService(Actions.PLAY)
                } else {
                    actionOnService(Actions.STOP)
                }
                dialog.cancel()
            }

            val adjustAlarmVolume: (Int) -> Unit = debounce<Int>(50, coroutineContext) {
                val keyCode = it

                val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                val minVolume = 0 // audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM) <- require API28+
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                Log.d(tag, "current, max = $currentVolume, $maxVolume")
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, max(currentVolume - 1, minVolume), AudioManager.FLAG_SHOW_UI)

                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, min(currentVolume + 1, maxVolume), AudioManager.FLAG_SHOW_UI)
                }

            }

            builder1.setOnKeyListener { dialogInterface, i, event ->
                adjustAlarmVolume(i)
                true
            }

            val alert11 = builder1.create()

            // start as alarm
            actionOnService(Actions.PLAY_OR_FALLBACK)

            alert11.show()

            true
        }

        alarmDays?.isEnabled = isWakingUp?.isChecked ?: false
        timeSet?.isEnabled = isWakingUp?.isChecked ?: false
        snoozeDuration?.isEnabled = isWakingUp?.isChecked ?: false

    }

    private fun actionOnService (a: Actions) {
        // start as alarm
        val i = Intent(requireContext(), RadioService::class.java)
        i.putExtra("action", a.name)
        requireContext().startService(i)
    }

    private fun <T> debounce(delayMs: Long = 500L,
                             coroutineContext: CoroutineContext,
                             f: (T) -> Unit): (T) -> Unit {
        var debounceJob: Job? = null
        return { param: T ->
            if (debounceJob?.isCompleted != false) {
                debounceJob = CoroutineScope(coroutineContext).launch {
                    delay(delayMs)
                    f(param)
                }
            }
        }
    }


}