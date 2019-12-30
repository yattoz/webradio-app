package fr.forum_thalie.tsumugi.playerstore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import fr.forum_thalie.tsumugi.*

class PlayerStore {

    val isPlaying: MutableLiveData<Boolean> = MutableLiveData()
    val isServiceStarted: MutableLiveData<Boolean> = MutableLiveData()
    val volume: MutableLiveData<Int> = MutableLiveData()
    val playbackState: MutableLiveData<Int> = MutableLiveData()
    val currentTime: MutableLiveData<Long> = MutableLiveData()
    val streamerPicture: MutableLiveData<Bitmap> = MutableLiveData()
    val streamerName: MutableLiveData<String> = MutableLiveData()
    val currentSong : Song = Song()
    val currentSongBackup: Song = Song()
    val lp : ArrayList<Song> = ArrayList()
    val queue : ArrayList<Song> = ArrayList()
    val isQueueUpdated: MutableLiveData<Boolean> = MutableLiveData()
    val isLpUpdated: MutableLiveData<Boolean> = MutableLiveData()
    val isMuted : MutableLiveData<Boolean> = MutableLiveData()
    val listenersCount: MutableLiveData<Int> = MutableLiveData()
    var latencyCompensator : Long = 0
    var isInitialized: Boolean = false

    init {
        playbackState.value = PlaybackStateCompat.STATE_STOPPED
        isPlaying.value = false
        isServiceStarted.value = false
        streamerName.value = ""
        volume.value = preferenceStore.getInt("volume", 100)
        currentTime.value = System.currentTimeMillis()
        isQueueUpdated.value = false
        isLpUpdated.value = false
        isMuted.value = false
        currentSong.title.value = noConnectionValue
        currentSongBackup.title.value = noConnectionValue
        listenersCount.value = 0
    }

    // ##################################################
    // ############## QUEUE / LP FUNCTIONS ##############
    // ##################################################

    fun updateLp() {
        // note : lp is empty at initialization. This check was needed when we used the R/a/dio API.
        //if (lp.isNotEmpty()){
            val n = Song()
            n.copy(currentSongBackup)
            if (n.title.value != noConnectionValue && n.title.value != streamDownValue)
                lp.add(0, n)
            currentSongBackup.copy(currentSong)
            isLpUpdated.value = true
            //[REMOVE LOG CALLS]//[REMOVE LOG CALLS]Log.d(tag, playerStoreTag +  lp.toString())
        //}
    }


    // ##################################################
    // ############## PICTURE FUNCTIONS #################
    // ##################################################

    fun initPicture(c: Context) {
        streamerPicture.value = BitmapFactory.decodeResource(c.resources,
            R.drawable.logo_roundsquare
        )
    }

    private val playerStoreTag = "====PlayerStore===="
    companion object {
        val instance by lazy {
            PlayerStore()
        }
    }
}

