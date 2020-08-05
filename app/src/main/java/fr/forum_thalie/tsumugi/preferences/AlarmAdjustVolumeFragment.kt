package fr.forum_thalie.tsumugi.preferences

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import fr.forum_thalie.tsumugi.Actions
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.RadioService
import fr.forum_thalie.tsumugi.playerstore.PlayerStore
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.max
import kotlin.math.min


class AlarmAdjustVolumeFragment : PreferenceFragmentCompat() {
    override fun onAttach(context: Context) {
        (activity as AppCompatActivity).supportActionBar?.title = context.getString(R.string.test_alarm_volume)
        super.onAttach(context)
    }

    // get previous state: if it's playing, we'll resume playing as multimedia; if it was stopped, we'll stop
    private var isPlayingMultimedia: Boolean = PlayerStore.instance.isPlaying.value ?: false

    override fun onStop() {
        if (isPlayingMultimedia)
        {
            actionOnService(Actions.PLAY)
        } else {
            actionOnService(Actions.PAUSE)
        }
        PlayerStore.instance.volume.value = PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt("volume", 100)
        super.onStop()
    }

    override fun onResume() {

        isPlayingMultimedia = PlayerStore.instance.isPlaying.value ?: false
        // start as alarm
        actionOnService(Actions.PLAY_OR_FALLBACK)
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun <T> debounce(delayMs: Long = 500L,
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

        val adjustAlarmVolume: (Int) -> Unit = debounce<Int>(50, GlobalScope.coroutineContext) {
            android.util.Log.d(tag, "button $it pushed")
            val keyCode = it
            val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.apply {
                val currentVolume = this.getStreamVolume(AudioManager.STREAM_ALARM)
                val minVolume = 0 // audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM) <- require API28+
                val maxVolume = this.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    this.setStreamVolume(AudioManager.STREAM_ALARM, max(currentVolume - 1, minVolume), AudioManager.FLAG_SHOW_UI)

                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                    this.setStreamVolume(AudioManager.STREAM_ALARM, min(currentVolume + 1, maxVolume), AudioManager.FLAG_SHOW_UI)
                } else {

                }
            }

        }
        view.isFocusableInTouchMode = true;
        view.requestFocus();
        view.setOnKeyListener { _, i, event ->
            if (i == KeyEvent.KEYCODE_VOLUME_DOWN || i == KeyEvent.KEYCODE_VOLUME_UP) {
                adjustAlarmVolume(i)
                true
            } else {
                false
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.alarm_adjust_volume_preferences, rootKey)

        val alarmVolumeSeekBar = findPreference<SeekBarPreference>("alarmVolume")
        alarmVolumeSeekBar?.apply {
            min = 0
            max = 100
            updatesContinuously = true
            setOnPreferenceChangeListener { _, newValue ->
                actionOnService(Actions.VOLUME, newValue as Int)
                true
            }
        }

    }


    private fun actionOnService(a: Actions, v: Int = 0)
    {
        val i = Intent(requireContext(), RadioService::class.java)
        i.putExtra("action", a.name)
        i.putExtra("value", v)
        //[REMOVE LOG CALLS]Log.d(tag, "Sending intent ${a.name}")
        requireContext().startService(i)
    }

}