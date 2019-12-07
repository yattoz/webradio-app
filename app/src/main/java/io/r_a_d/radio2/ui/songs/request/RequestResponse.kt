package io.r_a_d.radio2.ui.songs.request

import io.r_a_d.radio2.playerstore.Song
import org.json.JSONObject

class RequestResponse(jsonResponse: JSONObject) {
    //val total: Int = jsonResponse.getInt("total")
    //val perPage: Int = jsonResponse.getInt("per_page") // should stay 20 but in any case...
    val currentPage: Int = jsonResponse.getInt("current_page")
    val lastPage: Int = jsonResponse.getInt("last_page")
    //val fromNbr: Int = jsonResponse.getInt("from")
    //val toNbr: Int = jsonResponse.getInt("to")
    var songs : ArrayList<Song> = ArrayList()

    init {
        val songList = jsonResponse.getJSONArray("data")
        for (i in 0 until songList.length())
        {

            val title = (songList[i] as JSONObject).getString("title")
            val artist = (songList[i] as JSONObject).getString("artist")
            val id = (songList[i] as JSONObject).getInt("id")

            val s = Song("", id)
            s.title.value = title
            s.artist.value = artist
            s.isRequestable = (songList[i] as JSONObject).getBoolean("requestable")
            // TODO add the time before being requestable.
            songs.add(s)
        }
    }

    override fun toString(): String {
        var s = ""
        for (i in 0 until songs.size)
        {
            s += (songs[i].artist.value + " - " + songs[i].title.value + " | ")
        }
        return s
    }

}