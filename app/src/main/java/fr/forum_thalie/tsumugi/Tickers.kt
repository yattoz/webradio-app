package fr.forum_thalie.tsumugi

import android.support.v4.media.session.PlaybackStateCompat
import fr.forum_thalie.tsumugi.playerstore.PlayerStore
import java.util.*

class ApiFetchTick  : TimerTask() {
    override fun run() {
        if (PlayerStore.instance.playbackState.value == PlaybackStateCompat.STATE_STOPPED)
        {
            PlayerStore.instance.fetchApi()
        }
    }
}

class Tick  : TimerTask() {
    override fun run() {
        PlayerStore.instance.currentTime.postValue(PlayerStore.instance.currentTime.value!! + 500)
    }
}

