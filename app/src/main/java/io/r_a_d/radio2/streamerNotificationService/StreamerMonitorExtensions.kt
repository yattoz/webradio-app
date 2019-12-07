package io.r_a_d.radio2.streamerNotificationService

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import io.r_a_d.radio2.*
import io.r_a_d.radio2.alarm.RadioAlarm
import io.r_a_d.radio2.playerstore.PlayerStore
import org.json.JSONObject
import java.net.URL

fun startNextAlarmStreamer(c: Context){
    // the notification works with an alarm re-scheduled at fixed rate.
    // if the service stopped, the alarm is not re-scheduled.
    if (WorkerStore.instance.isServiceStarted)
    {
        val alarmIntent = Intent(c, StreamerMonitorService::class.java).let { intent ->
            intent.putExtra("action", Actions.NOTIFY.name)
            PendingIntent.getService(c, 0, intent, 0)
        }

        val alarmMgr = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> alarmMgr.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + WorkerStore.instance.tickerPeriod * 1000,
                alarmIntent
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> alarmMgr.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + WorkerStore.instance.tickerPeriod * 1000,
                alarmIntent
            )
            else -> alarmMgr.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + WorkerStore.instance.tickerPeriod * 1000,
                alarmIntent
            )
        }
    } else {
        Log.d(tag, "alarm called while service is dead - skipped.")
    }
}

fun stopStreamerMonitor(context: Context)
{
    val intent = Intent(context, StreamerMonitorService::class.java)
    intent.putExtra("action", Actions.KILL.name)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startService(intent)
    } else {
        context.startService(intent)
    }

    Log.i(tag, "Service stopped")
}

fun startStreamerMonitor(context: Context, force: Boolean = false)
{
    if (!force)
    {
        val isNotifyingForNewStreamer = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("newStreamerNotification", false)
        if (!isNotifyingForNewStreamer)
                return
    }

    val intent = Intent(context, StreamerMonitorService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }

    Log.i(tag, "Service started on boot")
}

fun fetchStreamer(applicationContext: Context) {
    val urlToScrape = "https://r-a-d.io/api"
    val scrape : (Any?) -> String =
        {
            URL(urlToScrape).readText()
        }
    val post: (parameter: Any?) -> Unit = {
        val result = JSONObject(it as String)
        if (!result.isNull("main"))
        {
            val name = result.getJSONObject("main").getJSONObject("dj").getString("djname")
            WorkerStore.instance.streamerName.value = name
        }
    }

    // notify
    val t = ServiceNotification(
        notificationChannelId = applicationContext.getString(R.string.streamerServiceChannelId),
        notificationChannel = R.string.streamerServiceChannel,
        notificationId = 2,
        notificationImportance = NotificationCompat.PRIORITY_LOW
    )
    t.create(applicationContext)
    t.show()

    try{
        Async(scrape, post)
        Log.d(tag, "enqueue next work in ${WorkerStore.instance.tickerPeriod} seconds")
    } catch (e: Exception) {
    }
}