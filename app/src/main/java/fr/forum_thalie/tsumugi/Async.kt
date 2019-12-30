package fr.forum_thalie.tsumugi

import android.os.AsyncTask
import android.util.Log
import fr.forum_thalie.tsumugi.playerstore.PlayerStore

class Async(val handler: (Any?) -> Any?, val post: (Any?) -> Unit = {},
            private val actionOnError: ActionOnError = ActionOnError.RESET, private val parameters: Any? = null) :
    AsyncTask<Any, Void, Any>() {

    init {
        try {
            execute()
        } catch (e: Exception)
        {
            //[REMOVE LOG CALLS]//[REMOVE LOG CALLS]//[REMOVE LOG CALLS]Log.d(tag,e.toString())
        }
    }

    private fun onException(e: java.lang.Exception) {
        when(actionOnError)
        {
            ActionOnError.RESET -> resetPlayerStateOnNetworkError()
            ActionOnError.NOTIFY -> return
        }
        e.printStackTrace()
    }

    private fun resetPlayerStateOnNetworkError() {
        var storeReset = false

        // checking isInitialized avoids setting streamerName multiple times, so it avoids a callback loop.
        if (PlayerStore.instance.isInitialized)
        {
            PlayerStore.instance.currentSong.artist.postValue("")
            PlayerStore.instance.isInitialized = false
            PlayerStore.instance.streamerName.postValue("")
            PlayerStore.instance.queue.clear()
            PlayerStore.instance.lp.clear()
            PlayerStore.instance.isQueueUpdated.postValue(true)
            PlayerStore.instance.isLpUpdated.postValue(true)
            // safe-update for the title avoids callback loop too.
            if (PlayerStore.instance.currentSong.title.value != noConnectionValue)
                PlayerStore.instance.currentSong.title.postValue(noConnectionValue)
            storeReset = true
        }


        //[REMOVE LOG CALLS]//[REMOVE LOG CALLS]Log.d(tag, "fallback for no network. Store reset : $storeReset")
    }

    override fun doInBackground(vararg params: Any?): Any? {
        try {
            return handler(parameters)
        } catch (e: Exception) {
            //[REMOVE LOG CALLS]//[REMOVE LOG CALLS]Log.d(tag,e.toString())
            onException(e)
        }
        return null
    }

    override fun onPostExecute(result: Any?) {
        try {
            post(result)
        } catch (e: Exception) {
            //[REMOVE LOG CALLS]//[REMOVE LOG CALLS]Log.d(tag,e.toString())
            onException(e)
        }
    }
}