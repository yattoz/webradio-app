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
        val s = extractSong(resMain)
        if (playbackState.value != PlaybackStateCompat.STATE_PLAYING || currentSong.title.value.isNullOrEmpty()
            || currentSong.title.value == noConnectionValue)
            currentSong.setTitleArtist("${s.artist.value} - ${s.title.value}")

        val starts = s.startTime.value
        val ends = s.stopTime.value

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
            Log.d(tag, "latency compensator set to ${(latencyCompensator).toFloat()/1000} s")
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
        song.setTitleArtist(songJSON.getString("name"))
        song.startTime.value = getTimestamp(songJSON.getString("starts"))
        song.stopTime.value = getTimestamp(songJSON.getString("ends"))
        song.type.value = 0 // only used for R/a/dio
        return song
    }


    // ##################################################
    // ############## QUEUE / LP FUNCTIONS ##############
    // ##################################################

    fun updateQueue() {
        if (queue.isNotEmpty()) {
            queue.remove(queue.first())
            Log.d(tag, queue.toString())
            fetchLastRequest()
            isQueueUpdated.value = true
        } else if (isInitialized) {
            fetchLastRequest()
        } else {
            Log.d(tag,  "queue is empty! fetching anyway !!")
            fetchLastRequest()
        }
    }

    fun updateLp() {
        // note : lp is empty at initialization. This check was needed when we used the R/a/dio API.
        //if (lp.isNotEmpty()){
            val n = Song()
            n.copy(currentSongBackup)
            if (n.title.value != noConnectionValue && n.title.value != streamDownValue)
                lp.add(0, n)
            currentSongBackup.copy(currentSong)
            isLpUpdated.value = true
            Log.d(tag, lp.toString())
        //}
    }


    private fun fetchLastRequest()
    {
        val sleepScrape: (Any?) -> String = {
            /* we can maximize our chances to retrieve the last queued song by specifically waiting for the number of seconds we measure between ICY metadata and API change.
             we add 2 seconds just to get a higher probability that the API has correctly updated. (the latency compensator can have a jitter of 1 second usually)
             If, against all odds, the API hasn't updated yet, we will retry in the same amount of seconds. So we'll have the data anyway.
            This way to fetch at the most probable time is a good compromise between fetch speed and fetch frequency
            We don't fetch too often, and we start to fetch at the most *probable* time.
            If there's no latencyCompensator measured yet, we only wait for 3 seconds.
            If the song is the same, it will be called again. 3 seconds is a good compromise between speed and frequency:
            it might be called twice, rarely 3 times, and it's only the 2 first songs ; after these, the latencyCompensator is set to fetch at the most probable time.
             */
            val sleepTime: Long = if (latencyCompensator > 0) latencyCompensator + 2000 else 3000
            Thread.sleep(sleepTime) // we wait a bit (10s) for the API to get updated on R/a/dio side!
            URL(urlToScrape).readText()
        }

        lateinit var post: (parameter: Any?) -> Unit

        fun postFun(result: JSONObject)
        {
            if (result.has("tracks")) {
                val resMain = result.getJSONObject("tracks")
                /*
                if ((resMain.has("isafkstream") && !resMain.getBoolean("isafkstream")) &&
                    queue.isNotEmpty())
                {
                    queue.clear() //we're not requesting anything anymore.
                    isQueueUpdated.value = true
                } else if (resMain.has("isafkstream") && resMain.getBoolean("isafkstream") &&
                    queue.isEmpty())
                {
                    initApi()
                } else
                */
                if (resMain.has("next") /*&& queue.isNotEmpty()*/) {
                    val queueJSON =
                        resMain.getJSONObject("next")
                    val t = extractSong(queueJSON)
                    if (queue.isNotEmpty() && t == queue.last())
                    {
                        Log.d(tag, playerStoreTag +  "Song already in there: $t")
                        Async(sleepScrape, post)
                    } else {
                        queue.add(queue.size, t)
                        Log.d(tag, playerStoreTag +  "added last queue song: $t")
                        isQueueUpdated.value = true
                    }
                }
            }
        }

        post = {
            val result = JSONObject(it as String)
            /*  The goal is to pass the result to a function that will process it (postFun).
                The magic trick is, under circumstances, the last queue song might not have been updated yet when we fetch it.
                So if this is detected ==> if (t == queue.last() )
                Then the function re-schedule an Async(sleepScrape, post).
                To do that, the "post" must be defined BEFORE the function, but the function must be defined BEFORE the "post" value.
                So I declare "post" as lateinit var, define the function, then define the "post" that calls the function. IT SHOULD WORK.
             */
            postFun(result)
        }

        Async(sleepScrape, post)
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

