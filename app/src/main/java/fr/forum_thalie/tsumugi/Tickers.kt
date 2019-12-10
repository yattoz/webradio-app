package fr.forum_thalie.tsumugi

import fr.forum_thalie.tsumugi.playerstore.PlayerStore
import java.util.*

class Tick  : TimerTask() {
    override fun run() {
        PlayerStore.instance.currentTime.postValue(PlayerStore.instance.currentTime.value!! + 500)
    }
}

