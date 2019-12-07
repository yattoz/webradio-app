package fr.forum_thalie.tsumugi.streamerNotificationService

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import fr.forum_thalie.tsumugi.R
import fr.forum_thalie.tsumugi.tag

class WorkerStore {
    companion object {
        val instance = WorkerStore()
    }

    val streamerName = MutableLiveData<String>()
    var isServiceStarted : Boolean = false
    var tickerPeriod : Long = 45 // seconds

    init {
        tickerPeriod = 45
        streamerName.value = ""
        isServiceStarted = false
    }

    fun init(c: Context)
    {
        tickerPeriod = 45
        streamerName.value = ""
        val tickerPeriod = 60 *
                (if (PreferenceManager.getDefaultSharedPreferences(c).contains("streamerMonitorPeriodPref"))
                    Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(c).getString("streamerMonitorPeriodPref", "15")!!).toLong()
                else
                    15
                        )
        instance.tickerPeriod = tickerPeriod
        Log.d(tag, "tickerPeriod = $tickerPeriod")

        with(PreferenceManager.getDefaultSharedPreferences(c).edit()){
            remove("streamerName")
            commit() // I commit on main thread to be sure it's been updated before continuing.
        }
    }

}