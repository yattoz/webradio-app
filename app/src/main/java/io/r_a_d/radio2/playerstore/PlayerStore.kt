package io.r_a_d.radio2.playerstore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.r_a_d.radio2.*
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL


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
    private val urlToScrape = "https://r-a-d.io/api"
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
        listenersCount.value = 0
    }

    // ##################################################
    // ################# API FUNCTIONS ##################
    // ##################################################

    private fun updateApi(resMain: JSONObject, isCompensatingLatency : Boolean = false) {
        // If we're not in PLAYING state, update title / artist metadata. If we're playing, the ICY will take care of that.
        if (playbackState.value != PlaybackStateCompat.STATE_PLAYING || currentSong.title.value.isNullOrEmpty()
            || currentSong.title.value == noConnectionValue)
            currentSong.setTitleArtist(resMain.getString("np"))

        // only update the value if the song has changed. This avoids to trigger observers when they shouldn't be triggered
        if (currentSong.startTime.value != resMain.getLong("start_time")*1000)
            currentSong.startTime.value = resMain.getLong("start_time")*1000

        // I noticed that the server has a big (3 to 9 seconds !!) offset for current time.
        // we can measure it when the player is playing, to compensate it and have our progress bar perfectly timed
        // latencyCompensator is set to null when beginPlaying() (we can't measure it at the moment we start playing, since we're in the middle of a song),
        // at this moment, we set it to 0. Then, next time the updateApi is called when we're playing, we measure the latency and we set out latencyComparator.
        if(isCompensatingLatency)
        {
            latencyCompensator = resMain.getLong("current")*1000 - (currentSong.startTime.value ?: resMain.getLong("current")*1000)
            Log.d(tag, playerStoreTag +  "latency compensator set to ${(latencyCompensator).toFloat()/1000} s")
        }
        currentSong.stopTime.value = resMain.getLong("end_time")*1000
        currentTime.value = (resMain.getLong("current"))*1000 - (latencyCompensator)

        val newStreamer = resMain.getJSONObject("dj").getString("djname")
        if (newStreamer != streamerName.value)
        {
            val streamerPictureUrl =
                "${urlToScrape}/dj-image/${resMain.getJSONObject("dj").getString("djimage")}"
            fetchPicture(streamerPictureUrl)
            streamerName.value = newStreamer
        }
        val listeners = resMain.getInt("listeners")
        listenersCount.value = listeners
        Log.d(tag, playerStoreTag +  "store updated")
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
            if (result.has("main"))
            {
                val resMain = result.getJSONObject("main")
                updateApi(resMain)
                currentSongBackup.copy(currentSong)
                queue.clear()
                if (resMain.has("queue") && resMain.getBoolean("isafkstream"))
                {
                    val queueJSON =
                        resMain.getJSONArray("queue")
                    for (i in 0 until queueJSON.length())
                    {
                        val t = extractSong(queueJSON[i] as JSONObject)
                        if (t != currentSong) // if the API is too slow and didn't remove the first song from queue...
                            queue.add(queue.size, t)
                    }
                }
                isQueueUpdated.value = true
                Log.d(tag, playerStoreTag +  queue.toString())

                if (resMain.has("lp"))
                {
                    val queueJSON =
                        resMain.getJSONArray("lp")
                    // if my stack is empty, I fill it entirely (startup)
                    if (lp.isEmpty())
                    {
                        for (i in 0 until queueJSON.length())
                            lp.add(lp.size, extractSong(queueJSON[i] as JSONObject))
                    }
                }
                Log.d(tag, playerStoreTag +  lp.toString())
                isLpUpdated.value = true
            }
            isInitialized = true
        }
        Async(scrape, post)
    }

    fun fetchApi(isCompensatingLatency: Boolean = false) {
        val post: (parameter: Any?) -> Unit = {
            val result = JSONObject(it as String)
            if (!result.isNull("main"))
            {
                val res = result.getJSONObject("main")
                updateApi(res, isCompensatingLatency)
            }
        }
        Async(scrape, post)
    }

    // ##################################################
    // ############## QUEUE / LP FUNCTIONS ##############
    // ##################################################

    fun updateLp() {
        // note : lp must never be empty. There should always be some songs "last played".
        // if not, then the function has been called before initialization. No need to do anything.
        if (lp.isNotEmpty()){
            val n = Song()
            n.copy(currentSongBackup)
            lp.add(0, n)
            currentSongBackup.copy(currentSong)
            isLpUpdated.value = true
            Log.d(tag, playerStoreTag +  lp.toString())
        }
    }

    fun updateQueue() {
        if (queue.isNotEmpty()) {
            queue.remove(queue.first())
            Log.d(tag, playerStoreTag + queue.toString())
            fetchLastRequest()
            isQueueUpdated.value = true
        } else if (isInitialized) {
            fetchLastRequest()
        } else {
            Log.d(tag, playerStoreTag +  "queue is empty!")
        }
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
            if (result.has("main")) {
                val resMain = result.getJSONObject("main")
                if ((resMain.has("isafkstream") && !resMain.getBoolean("isafkstream")) &&
                    queue.isNotEmpty())
                {
                    queue.clear() //we're not requesting anything anymore.
                    isQueueUpdated.value = true
                } else if (resMain.has("isafkstream") && resMain.getBoolean("isafkstream") &&
                    queue.isEmpty())
                {
                    initApi()
                } else if (resMain.has("queue") && queue.isNotEmpty()) {
                    val queueJSON =
                        resMain.getJSONArray("queue")
                    val t = extractSong(queueJSON[4] as JSONObject)
                    if (t == queue.last())
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

    private fun extractSong(songJSON: JSONObject) : Song {
        val song = Song()
        song.setTitleArtist(songJSON.getString("meta"))
        song.startTime.value = songJSON.getLong("timestamp")
        song.stopTime.value = song.startTime.value
        song.type.value = songJSON.getInt("type")
        return song
    }

    // ##################################################
    // ############## PICTURE FUNCTIONS #################
    // ##################################################

    private fun fetchPicture(fileUrl: String)
    {
        val scrape: (Any?) -> Bitmap? = {
            var k: InputStream? = null
            var pic: Bitmap? = null
            try {
                k = URL(fileUrl).content as InputStream
                val options = BitmapFactory.Options()
                options.inSampleSize = 1
                // this makes 1/2 of origin image size from width and height.
                // it alleviates the memory for API16-API19 especially
                pic = BitmapFactory.decodeStream(k, null, options)
                k.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                k?.close()
            }
            pic
        }
        val post : (parameter: Any?) -> Unit = {
            streamerPicture.postValue(it as Bitmap?)
        }
        Async(scrape, post)
    }

    fun initPicture(c: Context) {
        streamerPicture.value = BitmapFactory.decodeResource(c.resources,
            R.drawable.actionbar_logo
        )
    }

    private val playerStoreTag = "====PlayerStore===="
    companion object {
        val instance by lazy {
            PlayerStore()
        }
    }
}

