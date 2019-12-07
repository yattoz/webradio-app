package io.r_a_d.radio2.streamerNotificationService


import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import io.r_a_d.radio2.Actions
import io.r_a_d.radio2.R
import io.r_a_d.radio2.tag
import java.util.*

class StreamerMonitorService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null     // no binding allowed nor needed
    }
    private val streamerNameObserver: Observer<String> = Observer {
        val previousStreamer: String
        if (PreferenceManager.getDefaultSharedPreferences(this).contains("streamerName"))
        {
            previousStreamer = PreferenceManager.getDefaultSharedPreferences(this).getString("streamerName", "") ?: ""
            /* 3 conditions:
                - the streamer changed from previously
                - there is a previous non-empty streamer (at least second time running it)
                - the current streamer is non-empty (this can happen at Activity start where init() is called)
             */
            if (previousStreamer != it && previousStreamer != "" && it != "")
            {
                // notify
                val newStreamer = StreamerNotification(
                    notificationChannelId = this.getString(R.string.streamerNotificationChannelId),
                    notificationChannel = R.string.streamerNotificationChannel,
                    notificationId = 3,
                    notificationImportance = NotificationCompat.PRIORITY_DEFAULT
                )
                newStreamer.create(this)
                newStreamer.show()
            }
        }

        with(PreferenceManager.getDefaultSharedPreferences(this).edit()){
            putString("streamerName", it)
            commit()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val streamerMonitorNotification = ServiceNotification(
            notificationChannelId = this.getString(R.string.streamerServiceChannelId),
            notificationChannel = R.string.streamerServiceChannel,
            notificationId = 2,
            notificationImportance = NotificationCompat.PRIORITY_LOW
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            streamerMonitorNotification.create(this)
            streamerMonitorNotification.update()
            streamerMonitorNotification.show()
            startForeground(2, streamerMonitorNotification.notification)
        }

        WorkerStore.instance.tickerPeriod = 60 *
                (if (PreferenceManager.getDefaultSharedPreferences(this).contains("streamerMonitorPeriodPref"))
                    Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("streamerMonitorPeriodPref", "15")!!).toLong()
                else
                    15)
        Log.d(tag, "tickerPeriod = ${WorkerStore.instance.tickerPeriod}")

        with(PreferenceManager.getDefaultSharedPreferences(this).edit()){
            remove("streamerName")
            commit() // I commit on main thread to be sure it's been updated before continuing.
        }
        WorkerStore.instance.streamerName.observeForever(streamerNameObserver)
        WorkerStore.instance.isServiceStarted = true
        startNextAlarmStreamer(this)
        Log.d(tag, "streamerMonitor created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isNotifyingForNewStreamer = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("newStreamerNotification", false)

        // it's probably redundant but it shouldn't hurt
        if (!isNotifyingForNewStreamer || !WorkerStore.instance.isServiceStarted)
        {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent?.getStringExtra("action")) {
            Actions.NOTIFY.name -> {
                val date = Date()   // given date
                val calendar = Calendar.getInstance() // creates a new calendar instance
                calendar.time = date   // assigns calendar to given date
                val hours = calendar.get(Calendar.HOUR_OF_DAY) // gets hour in 24h format
                //val hours_american = calendar.get(Calendar.HOUR)        // gets hour in 12h format
                val minutes = calendar.get(Calendar.MINUTE)       // gets month number, NOTE this is zero based!

                Log.d(tag, "Fetched streamer name at ${hours}:${if (minutes < 10) "0" else ""}${minutes}")
                fetchStreamer(this)
                startNextAlarmStreamer(this) // schedule next alarm
                return START_STICKY
            }
            Actions.KILL.name -> {
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
        //super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        WorkerStore.instance.streamerName.removeObserver(streamerNameObserver)
        WorkerStore.instance.isServiceStarted = false
        super.onDestroy()
    }
}
