package io.r_a_d.radio2.streamerNotificationService

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import io.r_a_d.radio2.R
import io.r_a_d.radio2.tag

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