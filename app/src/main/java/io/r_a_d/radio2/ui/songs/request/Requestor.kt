package io.r_a_d.radio2.ui.songs.request

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.r_a_d.radio2.ActionOnError
import io.r_a_d.radio2.Async
import io.r_a_d.radio2.playerstore.Song
import io.r_a_d.radio2.preferenceStore
import io.r_a_d.radio2.tag
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.CookieHandler
import java.net.CookieManager
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.regex.Pattern

import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * Requests a song via the website's API
 *
 * We scrape the website for a CSRF token and POST it to /request/ endpoint with
 * the song id
 *
 * Created by Kethsar on 1/2/2017.
 * Converted to Kotlin and adapted by Yattoz on 05 Nov. 2019
 */

class Requestor {
    var addRequestMeta: String = ""
    private val cookieManager: CookieManager = CookieManager()
    private val requestUrl = "https://r-a-d.io/request/%1\$d"
    private val searchUrl = "https://r-a-d.io/api/search/%1s?page=%2\$d"
    private val favoritesUrl = "https://r-a-d.io/faves/%1s?dl=true"
    private val songThresholdStep = 50
    private var songThreshold = songThresholdStep
    private var localQuery = ""

    private var token: String? = null
    val snackBarText : MutableLiveData<String?> = MutableLiveData()
    private var responseArray : ArrayList<RequestResponse> = ArrayList()
    val requestSongArray : ArrayList<Song> = ArrayList()
    val favoritesSongArray : ArrayList<Song> = ArrayList()
    val isRequestResultUpdated : MutableLiveData<Boolean> = MutableLiveData()
    val isFavoritesUpdated : MutableLiveData<Boolean> = MutableLiveData()
    var isLoadMoreVisible: Boolean = false


    init {
        snackBarText.value = ""
        isRequestResultUpdated.value = false
        isFavoritesUpdated.value = false
        isLoadMoreVisible = false
    }

    fun initFavorites(userName : String? = preferenceStore.getString("userName", null)){
        Log.d(tag, "initializing favorites")
        favoritesSongArray.clear()
        if (userName == null)
        {
            // Display is done by default in the XML.
            Log.d(tag, "no user name set for favorites")
            isFavoritesUpdated.value = true
            return
        }
        val favoritesUserUrl = String.format(Locale.getDefault(), favoritesUrl, userName)
        val scrapeFavorites : (Any?) -> JSONArray = {
            JSONArray(URL(favoritesUserUrl).readText())
        }
        val postFavorites :  (Any?) -> Unit = {
            val res = it as JSONArray
            for (i in 0 until (res).length())
            {
                val item = res.getJSONObject(i)
                val artistTitle = item.getString("meta")
                val id : Int? = if (item.isNull("tracks_id"))
                    null
                else
                    item.getInt("tracks_id")

                val lastRequested : Int? = if (item.isNull("lastrequested")) null else item.getInt("lastrequested")
                val lastPlayed : Int? = if (item.isNull("lastplayed")) null else item.getInt("lastplayed")
                val requestCount : Int? = if (item.isNull("requestcount")) null else item.getInt("requestcount")
                val isRequestable = (coolDown(lastPlayed, lastRequested, requestCount) < 0)
                //Log.d(tag, "val : $id")
                favoritesSongArray.add(Song(artistTitle, id ?: 0, isRequestable))
            }
            Log.d(tag, "favorites : $favoritesSongArray")
            isFavoritesUpdated.value = true
        }
        Async(scrapeFavorites, postFavorites, ActionOnError.NOTIFY)
    }

    fun search(query: String)
    {
        responseArray.clear()
        requestSongArray.clear()
        localQuery = query
        searchPage(query, 1) // the searchPage function is recursive to get all pages.
    }

    private fun searchPage(query: String, pageNumber : Int)
    {
        val searchURL = String.format(Locale.getDefault(), searchUrl, query, pageNumber)
        val scrape : (Any?) -> JSONObject = {
            val res = URL(searchURL).readText()
            val json = JSONObject(res)
            json
        }
        val post : (Any?) -> Unit = {
            val response = RequestResponse(it as JSONObject)

            responseArray.add(response)
            for (i in 0 until response.songs.size)
            {
                requestSongArray.add(response.songs[i])
            }
            isRequestResultUpdated.value = true
            if (requestSongArray.size >= songThreshold)
            {
                isLoadMoreVisible = true

            } else {
                if (response.currentPage < response.lastPage)
                    searchPage(query, pageNumber + 1) // recursive call to get the next page
                else
                    finishSearch()
            }

        }
        Async(scrape, post, ActionOnError.NOTIFY)
    }

    private fun finishSearch()
    {
        isLoadMoreVisible = false
    }

    fun reset()
    {
        requestSongArray.clear()
        responseArray.clear()
        isRequestResultUpdated.value = false
        songThreshold = songThresholdStep
    }

    fun loadMore()
    {
        songThreshold += songThresholdStep
        searchPage(localQuery, responseArray.last().currentPage + 1)
    }


    /**
     * Scrape the website for the CSRF token required for requesting
     * scrapeToken and postToken are the two lambas run by the Async() class.
     */

    private val scrapeToken : (Any?) -> Any? = {
        val radioSearchUrl = "https://r-a-d.io/search"
        var searchURL: URL? = null
        var retVal: String? = null
        var reader: BufferedReader? = null

        CookieHandler.setDefault(cookieManager) // it[0] ??

        try {
            searchURL = URL(radioSearchUrl)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        try {
            reader = BufferedReader(InputStreamReader(searchURL!!.openStream(), "UTF-8"))
            var line: String?
            line = reader.readLine()
            while (line != null)
            {
                line = line.trim { it <= ' ' }
                val p = Pattern.compile("value=\"(\\w+)\"")
                val m = p.matcher(line)

                if (line.startsWith("<form")) {
                    if (m.find()) {
                        retVal = m.group(1)
                        break
                    }
                }
                line = reader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (reader != null) try {
                reader.close()
            } catch (ignored: IOException) {
            }

        }
        retVal
    }

    private val postToken : (Any?) -> (Unit) = {
        token = it as String?
    }

    /**
     * Request the song with the CSRF token that was scraped
     */
    private val requestSong: (Any?) -> Any? = {
        val reqString = it as String
        var response = ""

        try {
            val reqURL = URL(reqString)
            val conn = reqURL.openConnection() as HttpsURLConnection
            val tokenObject = JSONObject()

            tokenObject.put("_token", token)
            val requestBytes = tokenObject.toString().toByteArray()

            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.doInput = true
            conn.setChunkedStreamingMode(0)
            conn.setRequestProperty("Content-Type", "application/json")

            val os = conn.outputStream
            os.write(requestBytes)

            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                var line: String?
                val br = BufferedReader(InputStreamReader(
                    conn.inputStream))
                line = br.readLine()
                while (line != null) {
                    response += line
                    line = br.readLine()
                }
            } else {
                response += ""
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }

        response
    }

    private val postSong  : (Any?) -> (Unit) = {
        val response = JSONObject(it as String)
        val key = response.names()!!.get(0) as String
        val value = response.getString(key)

        snackBarText.postValue(addRequestMeta + value)
    }


    fun request(songID: Int?) {
        val requestSongUrl = String.format(requestUrl, songID!!)
        if (token == null) {
            Async(scrapeToken, postToken, ActionOnError.NOTIFY)
        }
        Async(requestSong, postSong, ActionOnError.NOTIFY, requestSongUrl)
    }

    fun raF() : Song {
        // request a random favorite song. HELL YEAH
        val requestableSongArray = ArrayList<Song>()
        for (i in 0 until favoritesSongArray.size)
        {
            if (favoritesSongArray[i].isRequestable && (favoritesSongArray[i].id ?: 0) > 0)
                requestableSongArray.add(favoritesSongArray[i])
        }
        return if (requestableSongArray.isNotEmpty()) {
            val songNbr =  Random(System.currentTimeMillis()).nextInt(1, requestableSongArray.size)
            requestableSongArray[songNbr]
        } else {
            Song("No song requestable - ")
        }
    }

    companion object {
        val instance by lazy {
            Requestor()
        }
    }

}
