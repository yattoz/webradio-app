package io.r_a_d.radio2.ui.nowplaying

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.r_a_d.radio2.*
import io.r_a_d.radio2.alarm.RadioSleeper
import io.r_a_d.radio2.playerstore.PlayerStore
import io.r_a_d.radio2.playerstore.Song


class NowPlayingFragment : Fragment() {

    private lateinit var root: View
    private lateinit var nowPlayingViewModel: NowPlayingViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        nowPlayingViewModel = ViewModelProviders.of(this).get(NowPlayingViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_nowplaying, container, false)

        // View bindings to the ViewModel
        val songTitleText: TextView = root.findViewById(R.id.text_song_title)
        val songArtistText: TextView = root.findViewById(R.id.text_song_artist)
        val seekBarVolume: SeekBar = root.findViewById(R.id.seek_bar_volume)
        val volumeText: TextView = root.findViewById(R.id.volume_text)
        val progressBar: ProgressBar = root.findViewById(R.id.progressBar)
        val streamerPictureImageView: ImageView = root.findViewById(R.id.streamerPicture)
        val streamerNameText : TextView = root.findViewById(R.id.streamerName)
        val songTitleNextText: TextView = root.findViewById(R.id.text_song_title_next)
        val songArtistNextText: TextView = root.findViewById(R.id.text_song_artist_next)
        val volumeIconImage : ImageView = root.findViewById(R.id.volume_icon)
        val listenersText : TextView = root.findViewById(R.id.listenersCount)


        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            streamerNameText,8, 20, 2, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            listenersText,8, 16, 2, TypedValue.COMPLEX_UNIT_SP)
        /*
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            songTitleText,4, 24, 2, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            songArtistText,4, 24, 2, TypedValue.COMPLEX_UNIT_SP)
         */

        PlayerStore.instance.currentSong.title.observe(viewLifecycleOwner, Observer {
                songTitleText.text = it
        })

        PlayerStore.instance.currentSong.artist.observe(viewLifecycleOwner, Observer {
                songArtistText.text = it
        })

        PlayerStore.instance.playbackState.observe(viewLifecycleOwner, Observer {
            syncPlayPauseButtonImage(root)
        })

        // trick : I can't observe the queue because it's an ArrayDeque that doesn't trigger any change...
        // so I observe a dedicated Mutable that gets set when the queue is updated.
        PlayerStore.instance.isQueueUpdated.observe(viewLifecycleOwner, Observer {
            val t = if (PlayerStore.instance.queue.size > 0) PlayerStore.instance.queue[0] else Song("No queue - ") // (it.peekFirst != null ? it.peekFirst : Song() )
            songTitleNextText.text = t.title.value
            songArtistNextText.text = t.artist.value
        })

        fun volumeIcon(it: Int)
        {
            volumeText.text = "$it%"
            when {
                it > 66 -> volumeIconImage.setImageResource(R.drawable.ic_volume_high)
                it in 33..66 -> volumeIconImage.setImageResource(R.drawable.ic_volume_medium)
                it in 0..33 -> volumeIconImage.setImageResource(R.drawable.ic_volume_low)
                else -> volumeIconImage.setImageResource(R.drawable.ic_volume_off)
            }
        }

        PlayerStore.instance.volume.observe(viewLifecycleOwner, Observer {
            volumeIcon(it)
            seekBarVolume.progress = it // this updates the seekbar if it's set by something else when going to sleep.
        })

        PlayerStore.instance.isMuted.observe(viewLifecycleOwner, Observer {
            if (it)
                volumeIconImage.setImageResource(R.drawable.ic_volume_off)
            else
                volumeIcon(PlayerStore.instance.volume.value!!)
        })


        PlayerStore.instance.streamerPicture.observe(viewLifecycleOwner, Observer { pic ->
            streamerPictureImageView.setImageBitmap(pic)
        })

        PlayerStore.instance.streamerName.observe(viewLifecycleOwner, Observer {
            streamerNameText.text = it
        })

        PlayerStore.instance.listenersCount.observe(viewLifecycleOwner, Observer {
            listenersText.text = "${getString(R.string.listeners)}: $it"
        })

        // fuck it, do it on main thread
        PlayerStore.instance.currentTime.observe(viewLifecycleOwner, Observer {
            val dd = (PlayerStore.instance.currentTime.value!! - PlayerStore.instance.currentSong.startTime.value!!).toInt()
            progressBar.progress = dd
        })

        PlayerStore.instance.currentSong.stopTime.observe(viewLifecycleOwner, Observer {
            val dd = (PlayerStore.instance.currentSong.stopTime.value!! - PlayerStore.instance.currentSong.startTime.value!!).toInt()
            progressBar.max = dd
        })

        PlayerStore.instance.currentSong.stopTime.observe(viewLifecycleOwner, Observer {
            val t : TextView= root.findViewById(R.id.endTime)
            val minutes: String = ((PlayerStore.instance.currentSong.stopTime.value!! - PlayerStore.instance.currentSong.startTime.value!!)/60/1000).toString()
            val seconds: String = ((PlayerStore.instance.currentSong.stopTime.value!! - PlayerStore.instance.currentSong.startTime.value!!)/1000%60).toString()
            t.text = "$minutes:${if (seconds.toInt() < 10) "0" else ""}$seconds"
        })

        PlayerStore.instance.currentTime.observe(viewLifecycleOwner, Observer {
            val t : TextView= root.findViewById(R.id.currentTime)
            val minutes: String = ((PlayerStore.instance.currentTime.value!! - PlayerStore.instance.currentSong.startTime.value!!)/60/1000).toString()
            val seconds: String = ((PlayerStore.instance.currentTime.value!! - PlayerStore.instance.currentSong.startTime.value!!)/1000%60).toString()
            t.text = "$minutes:${if (seconds.toInt() < 10) "0" else ""}$seconds"

            val sleepInfoText = root.findViewById<TextView>(R.id.sleepInfo)
            val sleepAtMillis = RadioSleeper.instance.sleepAtMillis.value
            if (sleepAtMillis != null)
            {
                val duration = ((sleepAtMillis - System.currentTimeMillis()).toFloat() / (60f * 1000f) + 1).toInt() // I put 1 + it because the division rounds to the lower integer. I'd like to display the round up, like it's usually done.
                sleepInfoText.text = "Will close in $duration minute${if (duration > 1) "s" else ""}"
                sleepInfoText.visibility = View.VISIBLE
            } else {
                sleepInfoText.visibility = View.GONE
            }
        })


        seekBarVolume.setOnSeekBarChangeListener(nowPlayingViewModel.seekBarChangeListener)
        seekBarVolume.progress = PlayerStore.instance.volume.value!!
        progressBar.max = 1000
        progressBar.progress = 0

        syncPlayPauseButtonImage(root)

        // initialize the value for isPlaying when displaying the fragment
        PlayerStore.instance.isPlaying.value = PlayerStore.instance.playbackState.value == PlaybackStateCompat.STATE_PLAYING

        val button: ImageButton = root.findViewById(R.id.play_pause)
        button.setOnClickListener{
            PlayerStore.instance.isPlaying.value = PlayerStore.instance.playbackState.value == PlaybackStateCompat.STATE_STOPPED
        }

        /*
            /* TODO : disabled volumeIconImage click listener, it creates weird behaviors when switching fragments.
                in particular, the mute state isn't retained when switching fragments, and it creates visual error
                (displaying the mute icon when it's not muted).
                So for the moment it's safer to disable it altogether.
             */
        volumeIconImage.setOnClickListener{
            PlayerStore.instance.isMuted.value = !PlayerStore.instance.isMuted.value!!
        }

         */

        val setClipboardListener: View.OnLongClickListener = View.OnLongClickListener {
            val text = PlayerStore.instance.currentSong.artist.value + " - " + PlayerStore.instance.currentSong.title.value
            val clipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
            val snackBarLength = if (preferenceStore.getBoolean("snackbarPersistent", true))
                Snackbar.LENGTH_INDEFINITE
            else Snackbar.LENGTH_LONG
            val snackBar = Snackbar.make(it, "", snackBarLength)

            if (snackBarLength == Snackbar.LENGTH_INDEFINITE)
                snackBar.setAction("OK") { snackBar.dismiss() }

            snackBar.behavior = BaseTransientBottomBar.Behavior().apply {
                setSwipeDirection(BaseTransientBottomBar.Behavior.SWIPE_DIRECTION_ANY)
            }
            snackBar.setText(getString(R.string.song_to_clipboard))
            snackBar.show()
            true
        }

        songTitleText.setOnLongClickListener(setClipboardListener)
        songArtistText.setOnLongClickListener(setClipboardListener)

        if (preferenceStore.getBoolean("splitLayout", true))
            root.addOnLayoutChangeListener(splitLayoutListener)

        return root
    }

    private val splitLayoutListener : View.OnLayoutChangeListener = View.OnLayoutChangeListener { _: View, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int, _: Int ->

        val isSplitLayout = preferenceStore.getBoolean("splitLayout", true)

        val viewHeight = (root.rootView?.height ?: 1)
        val viewWidth = (root.rootView?.width ?: 1)

        val newRatio = if (viewWidth > 0)
            (viewHeight*100)/viewWidth
        else
            100

        if (isSplitLayout && nowPlayingViewModel.screenRatio != newRatio) {
            onOrientation()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        onOrientation()
        super.onViewStateRestored(savedInstanceState)
    }

    private fun onOrientation() {
        val viewHeight = (root.rootView?.height ?: 1)
        val viewWidth = (root.rootView?.width ?: 1)

        val isSplitLayout = preferenceStore.getBoolean("splitLayout", true)

        // modify layout to adapt for portrait/landscape
        val isLandscape = viewHeight.toDouble()/viewWidth.toDouble() < 1
        val parentLayout = root.findViewById<ConstraintLayout>(R.id.parentNowPlaying)
        val constraintSet = ConstraintSet()
        constraintSet.clone(parentLayout)

        if (isLandscape && isSplitLayout)
        {
            constraintSet.connect(R.id.layoutBlock1, ConstraintSet.BOTTOM, R.id.parentNowPlaying, ConstraintSet.BOTTOM)
            constraintSet.connect(R.id.layoutBlock1, ConstraintSet.END, R.id.splitHorizontalLayout, ConstraintSet.END)
            constraintSet.connect(R.id.layoutBlock2, ConstraintSet.TOP, R.id.parentNowPlaying, ConstraintSet.TOP)
            constraintSet.connect(R.id.layoutBlock2, ConstraintSet.START, R.id.splitHorizontalLayout, ConstraintSet.END)
            constraintSet.setMargin(R.id.layoutBlock1, ConstraintSet.END, 16)
            constraintSet.setMargin(R.id.layoutBlock2, ConstraintSet.START, 16)
        } else {
            constraintSet.connect(R.id.layoutBlock1, ConstraintSet.BOTTOM, R.id.splitVerticalLayout, ConstraintSet.BOTTOM)
            constraintSet.connect(R.id.layoutBlock1, ConstraintSet.END, R.id.parentNowPlaying, ConstraintSet.END)
            constraintSet.connect(R.id.layoutBlock2, ConstraintSet.TOP, R.id.splitVerticalLayout, ConstraintSet.BOTTOM)
            constraintSet.connect(R.id.layoutBlock2, ConstraintSet.START, R.id.parentNowPlaying, ConstraintSet.START)
            constraintSet.setMargin(R.id.layoutBlock1, ConstraintSet.END, 0)
            constraintSet.setMargin(R.id.layoutBlock2, ConstraintSet.START, 0)
        }
        constraintSet.applyTo(parentLayout)

        // note : we have to COMPARE numbers that are FRACTIONS. And everyone knows that we should NEVER compare DOUBLES because of the imprecision at the end.
        // So instead, I multiply the result by 100 (to give 2 significant numbers), and do an INTEGER DIVISION. This is the right way to compare ratios.
        nowPlayingViewModel.screenRatio = if (viewWidth > 0)
                (viewHeight*100)/viewWidth
        else
            100
        Log.d(tag, "orientation set")
    }

    override fun onResume() {
        super.onResume()
        onOrientation()
    }

    private fun syncPlayPauseButtonImage(v: View)
    {
        val img = v.findViewById<ImageButton>(R.id.play_pause)

        if (PlayerStore.instance.playbackState.value == PlaybackStateCompat.STATE_STOPPED) {
            img.setImageResource(R.drawable.exo_controls_play)
        } else {
            img.setImageResource(R.drawable.exo_controls_pause)
        }
    }
}
