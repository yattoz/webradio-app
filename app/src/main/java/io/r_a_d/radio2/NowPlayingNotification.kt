package io.r_a_d.radio2

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import io.r_a_d.radio2.playerstore.PlayerStore
import io.r_a_d.radio2.BootBroadcastReceiver

class NowPlayingNotification(
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

    // ########################################
    // ###### NOW PLAYING NOTIFICATION ########
    // ########################################

    lateinit var mediaStyle: androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle

    fun create(c: Context, m: MediaSessionCompat) {
        super.create(c)

        // got it right
        val delIntent = Intent(c, RadioService::class.java)
        delIntent.putExtra("action", Actions.KILL.name)
        val deleteIntent = PendingIntent.getService(c, 0, delIntent, PendingIntent.FLAG_NO_CREATE)
        builder.setDeleteIntent(deleteIntent)

        mediaStyle = androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle().also {
            it.setMediaSession(m.sessionToken)
            it.setShowActionsInCompactView(0) // index 0 = show actions 0 and 1 (show action #0 (play/pause))
            it.setCancelButtonIntent(deleteIntent)
        }
        builder.setStyle(mediaStyle)
        update(c)
    }

    fun update(c: Context, isUpdatingNotificationButton: Boolean = false, isRinging: Boolean = false) {

        if (isUpdatingNotificationButton)
            builder.mActions.clear()

        // Title : Title of notification (usu. songArtist is first)
        // Text : Text of the notification (usu. songTitle is second)
        builder.setContentTitle(PlayerStore.instance.currentSong.artist.value)
        builder.setContentText(PlayerStore.instance.currentSong.title.value)
        // As subText, we show when the player is stopped. This is a friendly reminder that the metadata won't get updated.
        // Maybe later we could replace it by a nice progressBar? Would it be interesting to have one here? I don't know.
        if (PlayerStore.instance.playbackState.value == PlaybackStateCompat.STATE_STOPPED) {
            builder.setSubText("Stopped")
            builder.setShowWhen(false)
        }
        else {
            builder.setSubText(null)
            builder.setShowWhen(true)
        }

        if (builder.mActions.isEmpty()) {
            val intent = Intent(c, RadioService::class.java)
            val playPauseAction: NotificationCompat.Action

            playPauseAction = if (PlayerStore.instance.playbackState.value == PlaybackStateCompat.STATE_PLAYING) {
                intent.putExtra("action", Actions.PAUSE.name)
                val pendingButtonIntent = PendingIntent.getService(c, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                NotificationCompat.Action.Builder(R.drawable.ic_pause, "Pause", pendingButtonIntent).build()
            } else {
                intent.putExtra("action", Actions.PLAY.name)
                val pendingButtonIntent = PendingIntent.getService(c, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                NotificationCompat.Action.Builder(R.drawable.ic_play,"Play", pendingButtonIntent).build()
            }
            builder.addAction(playPauseAction)
            val intent2 = Intent(c, RadioService::class.java)
            intent2.putExtra("action", Actions.KILL.name)
            val pendingButtonIntent = PendingIntent.getService(c, 2, intent2, PendingIntent.FLAG_UPDATE_CURRENT)
            val stopAction = NotificationCompat.Action.Builder(R.drawable.ic_stop,"Stop", pendingButtonIntent).build()
            builder.addAction(stopAction)

            if (isRinging) {
                val snoozeString = preferenceStore.getString("snoozeDuration", "10") ?: "10"
                val snoozeMinutes = if (snoozeString == c.getString(R.string.disable)) 0  else Integer.parseInt(snoozeString)

                val snoozeIntent = Intent(c, RadioService::class.java)
                snoozeIntent.putExtra("action", Actions.SNOOZE.name)
                val pendingSnoozeIntent = PendingIntent.getService(c, 5, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                val snoozeAction = NotificationCompat.Action.Builder(R.drawable.ic_alarm, "Snooze ($snoozeMinutes min.)", pendingSnoozeIntent ).build()
                if (snoozeMinutes > 0)
                    builder.addAction(snoozeAction)
                builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            } else {
                builder.setStyle(mediaStyle)
            }
        }
        builder.setLargeIcon(PlayerStore.instance.streamerPicture.value)

        super.show()
    }
}