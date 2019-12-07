package io.r_a_d.radio2.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import io.r_a_d.radio2.*

class RadioSleeper {

    companion object {
        val instance by lazy {
            RadioSleeper()
        }
    }

    val sleepAtMillis: MutableLiveData<Long?> = MutableLiveData()

    init
    {
        // the companion object is lazy, and is invoked by a Ticker, so a background thread.
        // we MUST use postValue to set it correctly.
        sleepAtMillis.postValue(null)
    }

    private lateinit var sleepIntent: PendingIntent
    private lateinit var fadeOutIntent: PendingIntent

    fun setSleep(c: Context, isForce: Boolean = false, forceDuration: Long? = null)
    {
        // don't do anything if the preference is set to FALSE, of course.
        if (!PreferenceManager.getDefaultSharedPreferences(c).getBoolean("isSleeping", false) && !isForce)
            return

        val minutes: Long = forceDuration ?: Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(c).getString("sleepDuration", "1") ?: "1").toLong()

        val alarmManager = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        sleepIntent = Intent(c, RadioService::class.java).let { intent ->
            intent.putExtra("action", Actions.KILL.name)
            PendingIntent.getService(c, 99, intent, 0)
        }

        val currentMillis = System.currentTimeMillis()
        if (minutes > 0)
        {
            AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, currentMillis + (minutes * 60 * 1000),  sleepIntent)
            fadeOutIntent = Intent(c, RadioService::class.java).let { intent ->
                intent.putExtra("action", Actions.FADE_OUT.name)
                PendingIntent.getService(c, 98, intent, 0)
            }
            AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, currentMillis + (minutes * 60 * 1000) - (1 * 60 * 1000), fadeOutIntent)
            sleepAtMillis.value = System.currentTimeMillis() + (minutes * 60 * 1000) - 1 // this -1 allows to round the division for display at the right integer
            Log.d(tag, "set sleep to $minutes minutes")
        }
    }


    fun cancelSleep(c: Context)
    {
        if (::sleepIntent.isInitialized)
        {
            val alarmManager = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(sleepIntent)
            alarmManager.cancel(fadeOutIntent)

            val cancelFadeOutIntent = Intent(c, RadioService::class.java).putExtra("action", Actions.CANCEL_FADE_OUT.name)
            c.startService(cancelFadeOutIntent)

            Log.d(tag, "cancelled sleep")
        }
        sleepAtMillis.value = null
    }
}