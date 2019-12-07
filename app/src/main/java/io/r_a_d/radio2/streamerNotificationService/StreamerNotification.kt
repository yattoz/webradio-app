package io.r_a_d.radio2.streamerNotificationService

import android.content.Context
import io.r_a_d.radio2.BaseNotification
import java.util.*

class StreamerNotification(
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
    override fun create(c: Context) {
        super.create(c)
        val date = Date()   // given date
        val calendar = Calendar.getInstance() // creates a new calendar instance
        calendar.time = date   // assigns calendar to given date
        val hours = calendar.get(Calendar.HOUR_OF_DAY) // gets hour in 24h format
        //val hours_american = calendar.get(Calendar.HOUR)        // gets hour in 12h format
        val minutes = calendar.get(Calendar.MINUTE)       // gets month number, NOTE this is zero based!

        builder.setContentTitle("${WorkerStore.instance.streamerName.value} started streaming!")
        builder.setContentText("Started at ${hours}:${if (minutes < 10) "0" else ""}${minutes}")
        builder.setAutoCancel(true)
        super.show()
    }

}