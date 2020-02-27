package fr.forum_thalie.tsumugi.ui.nowplaying

import android.widget.SeekBar
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.forum_thalie.tsumugi.playerstore.PlayerStore
import fr.forum_thalie.tsumugi.preferenceStore

class NowPlayingViewModel: ViewModel() {

    /* Note : ViewModels do not have any kind of data persistence, which is a bit of a shame.
       Data persistence is currently in beta, and poorly documented (some pages don't even match!)
       For the moment, we will store data related to playback state in PlayerStore.
    */
    var screenRatio: Int = 100



    var seekBarChangeListener: SeekBar.OnSeekBarChangeListener =
        object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // updated continuously as the user slides the thumb
                PlayerStore.instance.volume.value = progress
                preferenceStore.edit {
                    putInt("volume", progress)
                    commit()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // called when the user first touches the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // called after the user finishes moving the SeekBar
            }
        }
}
