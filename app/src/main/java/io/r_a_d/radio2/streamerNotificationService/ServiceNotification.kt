package io.r_a_d.radio2.streamerNotificationService

import android.content.Context
import io.r_a_d.radio2.BaseNotification
import java.util.*

class ServiceNotification(
    notificationChannelId: String,
    notificationChannel : Int,
    notificationId: Int,
    notificationImportance: Int

) : BaseNotification
    (
    notificationChannelId,
    notificationChannel,
    notificationId,
    notificationImportance
){
    
    override fun create(c: Context)
    {
        super.create(c)
        update()
    }

    fun update()
    {
        val date = Date()   // given date
        val calendar = Calendar.getInstance() // creates a new calendar instance
        calendar.time = date   // assigns calendar to given date
        val hours = calendar.get(Calendar.HOUR_OF_DAY) // gets hour in 24h format
        //val hours_american = calendar.get(Calendar.HOUR)        // gets hour in 12h format
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)

        builder.setContentTitle("Never miss a stream! Current: ${WorkerStore.instance.streamerName.value}")
        builder.setContentText("Last update: ${hours}:${minutes}:${seconds}")
        notification = builder.build()
    }
}