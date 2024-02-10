package fr.forum_thalie.tsumugi.playerstore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import fr.forum_thalie.tsumugi.*
import fr.forum_thalie.tsumugi.planning.Planning
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.coroutineContext

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
    lateinit var pictureUrl: String
    lateinit var defaultPicture: Bitmap
    lateinit var updateQueue: (parameter: Any?) -> Unit

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
        currentSong.setTitleArtist(noConnectionValue)
        currentSongBackup.setTitleArtist(noConnectionValue)
        listenersCount.value = 0

        updateQueue =
        {
            val result = JSONObject(it as String)
            if (result.has("playing_next")) {
                val resMain = result
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
                if (resMain.has("playing_next")) {
                    val queueJSON =
                        resMain.getJSONObject("playing_next")
                    val t = extractSong(queueJSON)
                    if (queue.isNotEmpty() && (t == queue.last() || t == currentSong) && isQueueUpdated.value == false)
                    {
                        //[REMOVE LOG CALLS]Log.d(tag, playerStoreTag +  "Song already in there: $t\nQueue:$queue")
                        Async(sleepScrape, updateQueue)
                    } else {
                        if (queue.isNotEmpty())
                            queue.remove(queue.first())
                        queue.add(queue.size, t)
                        //[REMOVE LOG CALLS]Log.d(tag, playerStoreTag +  "added last queue song: $t")
                        isQueueUpdated.value = true
                    }
                }
            }
        }

    }

    fun initUrl(c: Context)
    {
        urlToScrape = c.getString(R.string.API_URL)
    }

    private fun getTimestamp(s: String) : Long
    {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss z", Locale.getDefault())
        try {
            val t: Date? = dateFormat.parse("$s ${Planning.instance.timeZone.getDisplayName(Planning.instance.timeZone.useDaylightTime(), TimeZone.SHORT)}")
            //[REMOVE LOG CALLS]Log.d(tag, "date: $s -> $t")
            return t!!.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }


    // ##################################################
    // ################# API FUNCTIONS ##################
    // ##################################################

    private fun updateApi(res: JSONObject, isCompensatingLatency : Boolean = true, isID3TagChanged: Boolean = false) {
        // If we're not in PLAYING state, update title / artist metadata. If we're playing, the ICY will take care of that.
        //[REMOVE LOG CALLS]Log.d(tag, "${playerStoreTag} CALLING UPDATEAPI, isID3TagChanged = $isID3TagChanged")


        val s = extractSong(res.getJSONObject("now_playing"))
        if (playbackState.value != PlaybackStateCompat.STATE_PLAYING || currentSong.title.value.isNullOrEmpty()
            || currentSong.title.value == noConnectionValue)
        {
            currentSong.setTitleArtist("${s.artist.value} - ${s.title.value}")
        }

        val starts = s.startTime.value
        val ends = s.stopTime.value

        if (currentSong.startTime.value != starts)
            currentSong.startTime.value = starts //!! + 1000*(res.getJSONObject("now_playing").getInt("elapsed"))

        currentSong.stopTime.value = ends
        currentSong.id = s.id


        val apiTime: Long = currentSong.startTime.value!! + 1000*(res.getJSONObject("now_playing").getLong("elapsed"))
        // I noticed that the server has a big (3 to 9 seconds !!) offset for current time.
        // we can measure it when the player is playing, to compensate it and have our progress bar perfectly timed
        // latencyCompensator is set to null when beginPlaying() (we can't measure it at the moment we start playing, since we're in the middle of a song),
        // at this moment, we set it to 0. Then, next time the updateApi is called when we're playing, we measure the latency and we set out latencyComparator.
        if(isCompensatingLatency)
        {
            latencyCompensator = apiTime - System.currentTimeMillis() //(currentSong.startTime.value!!)
            Log.d(tag, "latency compensator set to ${(latencyCompensator).toFloat() / 1000} s")
        }
        currentTime.value = apiTime - (latencyCompensator)

        val listeners = res.getJSONObject("listeners").getInt("current")
        listenersCount.value = listeners



        val newPictureUrl = res.getJSONObject("now_playing").getJSONObject("song").getString("art").replace("http://", "https://")
        fetchPicture(newPictureUrl)

        //[REMOVE LOG CALLS]
        Log.d(tag, playerStoreTag +  "store updated:\n\t\tsong:${currentSong.title.value}, id=${currentSong.id}\n\t\tstart=${currentSong.startTime.value}, apiTime=${apiTime}, stop=${currentSong.stopTime.value}")
    }

    private val scrape : (Any?) -> String =
        {
            URL(urlToScrape).readText()
        }
    private val sleepScrape: (Any?) -> String = {
        /* we can maximize our chances to retrieve the last queued song by specifically waiting for the number of seconds we measure between ICY metadata and API change.
         we add 2 seconds just to get a higher probability that the API has correctly updated. (the latency compensator can have a jitter of 1 second usually)
         If, against all odds, the API hasn't updated yet, we will retry in the same amount of seconds. So we'll have the data anyway.
        This way to fetch at the most probable time is a good compromise between fetch speed and fetch frequency
        We don't fetch too often, and we start to fetch at the most *probable* time.
        If there's no latencyCompensator measured yet, we only wait for 3 seconds.
        If the song is the same, it will be called again. 3 seconds is a good compromise between speed and frequency:
        it might be called twice, rarely 3 times, and it's only the 2 first songs ; after these, the latencyCompensator is set to fetch at the most probable time.
         */
        val sleepTime: Long = if (latencyCompensator > 0) latencyCompensator + 1000 else 3000
        Thread.sleep(sleepTime) // we wait a bit (10s) for the API to get updated on R/a/dio side!
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
            if (result.has("now_playing"))
            {
                updateApi(result)
                updateQueue(it)
                currentSongBackup.copy(currentSong)

                // isQueueUpdated.value = true
                // isLpUpdated.value = true
            }
            isInitialized = true
        }
        Async(scrape, post)
    }

    fun fetchApi(isCompensatingLatency: Boolean = true, isID3TagChanged: Boolean = false) {
        lateinit var post: (parameter: Any?) -> Unit

        val sleepScrape: (Any?) -> String = {
            val sleepTime: Long = 2000 //ms
            Thread.sleep(sleepTime) // we wait a bit (2s) for the API to get updated on Azuracast side
            URL(urlToScrape).readText()
        }

        fun postFun(it: String): Unit {
            val result = JSONObject(it as String)
            if (!result.isNull("now_playing"))
            {
                val newId: String = result.getJSONObject("now_playing").getJSONObject("song").getString("id")
                if (currentSong.id == newId && isID3TagChanged)
                {
                    Log.wtf(tag, "$playerStoreTag - ID ${result.getJSONObject("now_playing").getJSONObject("song").getString("text")} wasn't updated yet. isID3TagChanged = $isID3TagChanged")
                    // re-schedule a fetch?
                    // updateApi(result, isCompensatingLatency, isID3TagChanged)
                    Async(sleepScrape, post)
                } else {
                    updateApi(result, isCompensatingLatency, isID3TagChanged)
                    if (currentSong != currentSongBackup && currentSongBackup.id != "") // if id == "", it means that backup song was the empty song loaded at boot
                    {
                        updateLp()
                        updateQueue(it)
                        Log.d(tag, "updated queue/lp while player not playing\ncurrent=${PlayerStore.instance.currentSong}\nbackup=${PlayerStore.instance.currentSongBackup}")
                    }

                }
            }
        }

        /*  The goal is to pass the result to a function that will process it (postFun).
            The magic trick is, under circumstances, the last queue song might not have been updated yet when we fetch it.
            So if this is detected ==> if (t == queue.last() )
            Then the function re-schedule an Async(sleepScrape, post).
            To do that, the "post" must be defined BEFORE the function, but the function must be defined BEFORE the "post" value.
            So I declare "post" as lateinit var, define the function, then define the "post" that calls the function. IT SHOULD WORK.
         */
        post = {
            val result = (it as String)
            postFun(result)
        }

        /*
        if (isID3TagChanged) {  // update the bar right away to make it look like starting the song.
            currentSong.startTime.value = System.currentTimeMillis()
            currentTime.value = System.currentTimeMillis()
        }
         */
        Async(scrape, post)
    }

    private fun extractSong(songJSON: JSONObject) : Song {
        val song = Song(_id = songJSON.getJSONObject("song").getString("id"))
        song.setTitleArtist(songJSON.getJSONObject("song").getString("text"))
        song.startTime.value = 1000*(songJSON.getLong("played_at"))  // getTimestamp(songJSON.getString("starts"))
        song.stopTime.value = 1000*(songJSON.getLong("played_at") + songJSON.getLong("duration"))  // getTimestamp(songJSON.getString("ends"))
        song.type.value = 0 // only used for R/a/dio
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
            if (n.title.value != noConnectionValue && n.title.value != streamDownValue && n.id != "")
                lp.add(0, n)
            currentSongBackup.copy(currentSong)
            isLpUpdated.value = true
            //[REMOVE LOG CALLS]Log.d(tag, playerStoreTag +  lp.toString())
        //}
    }


    fun fetchQueue()
    {
        isQueueUpdated.value = false
        Async(sleepScrape, updateQueue)
    }


    // ##################################################
    // ############## PICTURE FUNCTIONS #################
    // ##################################################


    fun fetchPicture(fileUrl: String)
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
        Log.d(tag, fileUrl)
        if (fileUrl == "" || fileUrl == "https://azuracast.mahoro-net.org/static/img/generic_song.jpg")
        {
            streamerPicture.value = defaultPicture
        } else {
            Async(scrape, post)
        }
    }

    fun initPicture(c: Context) {
        defaultPicture =  BitmapFactory.decodeResource(c.resources,
            R.drawable.logo_roundsquare
        )
        pictureUrl = ""
        streamerPicture.value = defaultPicture
    }

    private val playerStoreTag = "====PlayerStore===="
    companion object {
        val instance by lazy {
            PlayerStore()
        }
    }
}

