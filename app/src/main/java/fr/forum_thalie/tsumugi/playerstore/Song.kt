package fr.forum_thalie.tsumugi.playerstore

import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import fr.forum_thalie.tsumugi.noConnectionValue

class Song(artistTitle: String = "", _id : Int = 0) {

    // TODO : remove MutableLiveData, use a MutableLiveData Boolean on PlayerStore instead
    val title: MutableLiveData<String> = MutableLiveData()
    val artist: MutableLiveData<String> = MutableLiveData()
    val type: MutableLiveData<Int> = MutableLiveData()
    val startTime: MutableLiveData<Long> = MutableLiveData()
    val stopTime: MutableLiveData<Long> = MutableLiveData()
    var id: Int? = _id

    init {
        setTitleArtist(artistTitle)
        type.value = 0
        startTime.value =  System.currentTimeMillis()
        stopTime.value = System.currentTimeMillis() + 1000
    }

    override fun toString() : String {
        return "id=$id | ${artist.value} - ${title.value} | type=${type.value} | times ${startTime.value} - ${stopTime.value}\n"
    }

    fun setTitleArtist(dataHtml: String)
    {
        val data = HtmlCompat.fromHtml(dataHtml, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        val hyphenPos = data.indexOf(" - ")
        try {
            if (hyphenPos < 0)
                throw ArrayIndexOutOfBoundsException()
            if (artist.value != data.substring(0, hyphenPos))
                artist.value = data.substring(0, hyphenPos)
            if (title.value != data.substring(hyphenPos + 3))
                title.value = data.substring(hyphenPos + 3)
        } catch (e: Exception) {
            if (artist.value != "")
                artist.value = ""
            if (title.value != data)
                title.value = data
            // else : do nothing
        }
    }

    override fun equals(other: Any?) : Boolean
    {
        val song: Song = other as Song
        return this.title.value == song.title.value && this.artist.value == song.artist.value
    }

    fun copy(song: Song) {
        this.setTitleArtist(song.artist.value + " - " + song.title.value)
        this.startTime.value = song.startTime.value
        this.stopTime.value = song.stopTime.value
        this.type.value = song.type.value
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + stopTime.hashCode()
        result = 31 * result + (id ?: 0)
        return result
    }
}