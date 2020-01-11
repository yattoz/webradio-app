package fr.forum_thalie.tsumugi.playerstore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import fr.forum_thalie.tsumugi.*
import org.json.JSONObject
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlayerStore {

    private lateinit var urlToScrape: String
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
    var isStreamDown: Boolean = false

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

    fun initUrl(c: Context)
    {
        urlToScrape = c.getString(R.string.API_URL)
    }

    private fun getTimestamp(s: String) : Long
    {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        try {
            val t: Date? = dateFormat.parse(s)
            return t!!.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }


    // ##################################################
    // ################# API FUNCTIONS ##################
    // ##################################################

    private fun updateApi(res: JSONObject, isCompensatingLatency : Boolean = false) {
        // If we're not in PLAYING state, update title / artist metadata. If we're playing, the ICY will take care of that.

        val resMain = res.getJSONObject("tracks").getJSONObject("current")
        if (playbackState.value != PlaybackStateCompat.STATE_PLAYING || currentSong.title.value.isNullOrEmpty()
            || currentSong.title.value == noConnectionValue)
            currentSong.setTitleArtist(resMain.getString("name"))

        val starts = getTimestamp(resMain.getString("starts"))
        val ends = getTimestamp(resMain.getString("ends"))

        if (currentSong.startTime.value != starts)
            currentSong.startTime.value = starts

        currentSong.stopTime.value = ends

        // I noticed that the server has a big (3 to 9 seconds !!) offset for current time.
        // we can measure it when the player is playing, to compensate it and have our progress bar perfectly timed
        // latencyCompensator is set to null when beginPlaying() (we can't measure it at the moment we start playing, since we're in the middle of a song),
        // at this moment, we set it to 0. Then, next time the updateApi is called when we're playing, we measure the latency and we set out latencyComparator.
        if(isCompensatingLatency)
        {
            latencyCompensator = getTimestamp(res.getJSONObject("station").getString("schedulerTime")) - (currentSong.startTime.value ?: getTimestamp(res.getJSONObject("station").getString("schedulerTime")))
            Log.d(tag, playerStoreTag +  "latency compensator set to ${(latencyCompensator).toFloat()/1000} s")
        }
        currentTime.value = getTimestamp(res.getJSONObject("station").getString("schedulerTime")) - (latencyCompensator)

        /*
        val listeners = resMain.getInt("listeners")
        listenersCount.value = listeners
        Log.d(tag, playerStoreTag +  "store updated")
         */
    }

    private val scrape : (Any?) -> String =
        {
            URL(urlToScrape).readText()
        }

    /* initApi is called :
        - at startup
        - when a streamer changes.
        the idea is to fetch the queue when a streamer changes (potentially Hanyuu), and at startup.
        The Last Played is only fetched if it's empty (so, only at startup), not when a streamer changes.
     */
    fun initApi()
    {
        val post : (parameter: Any?) -> Unit = {
            val result = JSONObject(it as String)
            if (result.has("tracks"))
            {
                updateApi(result)
                currentSongBackup.copy(currentSong)

                isQueueUpdated.value = true

                isLpUpdated.value = true
            }
            isInitialized = true
        }
        Async(scrape, post)
    }

    fun fetchApi(isCompensatingLatency: Boolean = false) {
        val post: (parameter: Any?) -> Unit = {
            val result = JSONObject(it as String)
            if (!result.isNull("tracks"))
            {
                updateApi(result, isCompensatingLatency)
            }
        }
        Async(scrape, post)
    }

    private fun extractSong(songJSON: JSONObject) : Song {
        val song = Song()
        song.setTitleArtist(songJSON.getString("meta"))
        song.startTime.value = songJSON.getLong("timestamp")
        song.stopTime.value = song.startTime.value
        song.type.value = songJSON.getInt("type")
        return song
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
            Log.d(tag, playerStoreTag +  lp.toString())
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

