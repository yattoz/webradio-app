package io.r_a_d.radio2.playerstore

import androidx.lifecycle.MutableLiveData

class Song(artistTitle: String = "", _id : Int = 0, _isRequestable : Boolean = false) {

    // TODO : remove MutableLiveData, use a MutableLiveData Boolean on PlayerStore instead
    val title: MutableLiveData<String> = MutableLiveData()
    val artist: MutableLiveData<String> = MutableLiveData()
    val type: MutableLiveData<Int> = MutableLiveData()
    val startTime: MutableLiveData<Long> = MutableLiveData()
    val stopTime: MutableLiveData<Long> = MutableLiveData()
    var id: Int? = _id
    var isRequestable : Boolean = _isRequestable

    init {
        setTitleArtist(artistTitle)
        type.value = 0
        startTime.value =  System.currentTimeMillis()
        stopTime.value = System.currentTimeMillis() + 1000
    }

    override fun toString() : String {
        return "id=$id | ${artist.value} - ${title.value} | type=${type.value} | times ${startTime.value} - ${stopTime.value}\n"
    }

    fun setTitleArtist(data: String)
    {
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
        }
    }

    override fun equals(other: Any?) : Boolean
    {
        val song: Song = other as Song
        return this.title.value == song.title.value && this.artist.value == song.artist.value
    }

    fun copy(song: Song) {
        this.title.value = song.title.value
        this.artist.value = song.artist.value
        this.startTime.value = song.startTime.value
        this.stopTime.value = song.stopTime.value
        this.type.value = song.type.value
    }
}