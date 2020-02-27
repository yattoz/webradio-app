package fr.forum_thalie.tsumugi.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import fr.forum_thalie.tsumugi.*

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

    private fun defineIntents(c: Context)
    {
        sleepIntent = Intent(c, RadioService::class.java).let { intent ->
            intent.putExtra("action", Actions.KILL.name)
            PendingIntent.getService(c, 99, intent, 0)
        }
        fadeOutIntent = Intent(c, RadioService::class.java).let { intent ->
            intent.putExtra("action", Actions.FADE_OUT.name)
            PendingIntent.getService(c, 98, intent, 0)
        }
    }

    fun setSleep(c: Context, isForce: Boolean = false, forceDuration: Long? = null)
    {
        defineIntents(c)
        // don't do anything if the preference is set to FALSE, of course.
        if (!PreferenceManager.getDefaultSharedPreferences(c).getBoolean("isSleeping", false) && !isForce)
            return

        val minutes: Long = forceDuration ?: Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(c).getString("sleepDuration", "1") ?: "1").toLong()

        val alarmManager = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val currentMillis = System.currentTimeMillis()
        if (minutes > 0)
        {
            AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, currentMillis + (minutes * 60 * 1000),  sleepIntent)

            AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, currentMillis + (minutes * 60 * 1000) - (1 * 60 * 1000), fadeOutIntent)
            sleepAtMillis.value = System.currentTimeMillis() + (minutes * 60 * 1000) - 1 // this -1 allows to round the division for display at the right integer
            //[REMOVE LOG CALLS]Log.d(tag, "set sleep to $minutes minutes")
        }
    }


    fun cancelSleep(c: Context, isClosing: Boolean = false)
    {
        defineIntents(c)
        val alarmManager = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sleepIntent)
        alarmManager.cancel(fadeOutIntent)

        if (!isClosing)
        {
            val cancelFadeOutIntent = Intent(c, RadioService::class.java).putExtra("action", Actions.CANCEL_FADE_OUT.name)
            c.startService(cancelFadeOutIntent)
        }

        //[REMOVE LOG CALLS]Log.d(tag, "cancelled sleep")
        sleepAtMillis.value = null
    }
}