package io.r_a_d.radio2

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.setupActionBarWithNavController
import android.content.Intent
import android.util.Log
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import io.r_a_d.radio2.playerstore.PlayerStore

import java.util.Timer
import android.view.MenuItem
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import io.r_a_d.radio2.alarm.RadioAlarm
import io.r_a_d.radio2.streamerNotificationService.WorkerStore
import io.r_a_d.radio2.streamerNotificationService.startStreamerMonitor
import io.r_a_d.radio2.ui.songs.request.Requestor


/* Log to file import
import android.os.Environment
import java.io.File
import java.io.IOException
*/

class MainActivity : BaseActivity() {

    private val clockTicker: Timer = Timer()
    private var currentNavController: LiveData<NavController>? = null
    private var isTimerStarted = false

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)

        //val navGraphIds = listOf(R.navigation.home, R.navigation.list, R.navigation.form)
        val navGraphIds = listOf(R.navigation.navigation_nowplaying, R.navigation.navigation_songs,
            R.navigation.navigation_news, R.navigation.navigation_chat)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
        })
        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    // #####################################
    // ######### LIFECYCLE CALLBACKS #######
    // #####################################

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu, this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_refresh -> {
                PlayerStore.instance.queue.clear()
                PlayerStore.instance.lp.clear()
                PlayerStore.instance.initApi()
                Requestor.instance.initFavorites()
                val s = Snackbar.make(findViewById(R.id.nav_host_container), "Refreshing data..." as CharSequence, Snackbar.LENGTH_LONG)
                s.show()
                true
            }
            R.id.action_settings -> {
                val i = Intent(this, ParametersActivity::class.java)
                startActivity(i)
                true
            }
            R.id.action_sleep -> {
                val i = Intent(this, ParametersActivity::class.java)
                i.putExtra("action", ActionOpenParam.SLEEP.name) // TODO change value with Actions.something
                startActivity(i)
                true
            }
            R.id.action_alarm -> {
                val i = Intent(this, ParametersActivity::class.java)
                i.putExtra("action", ActionOpenParam.ALARM.name) // TODO change value with Actions.something
                startActivity(i)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState ?: Bundle())
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WorkerStore.instance.init(this)
        startStreamerMonitor(this) // this checks the preferenceStore before actually starting a service, don't worry.

        RadioAlarm.instance.cancelAlarm(c = this)
        RadioAlarm.instance.setNextAlarm(c = this) // this checks the preferenceStore before actually setting an alarm, don't worry.

        // initialize programmatically accessible colors
        colorBlue = ResourcesCompat.getColor(resources, R.color.bluereq, null)
        colorWhited = ResourcesCompat.getColor(resources, R.color.whited, null)
        colorGreenList = (ResourcesCompat.getColorStateList(resources, R.color.button_green, null))
        colorRedList = (ResourcesCompat.getColorStateList(resources, R.color.button_red, null))
        colorGreenListCompat = (ResourcesCompat.getColorStateList(resources, R.color.button_green_compat, null))

        PlayerStore.instance.initApi() // the service will call the initApi on defining the streamerName Observer too, but it's better to initialize the API as soon as the user opens the activity.

        // Post-UI Launch
        if (PlayerStore.instance.isInitialized)
        {
            Log.d(tag, "skipped initialization")
        } else {
            // if the service is not started, start it in STOP mode.
            // It's not a dummy action : with STOP mode, the player does not buffer audio (and does not use data connection without the user's consent).
            // this is useful since the service must be started to register bluetooth devices buttons.
            // (in case someone opens the app then pushes the PLAY button from their bluetooth device)
            if(!PlayerStore.instance.isServiceStarted.value!!)
                actionOnService(Actions.STOP)

            // initialize some API data
            PlayerStore.instance.initPicture(this)
            PlayerStore.instance.streamerName.value = "" // initializing the streamer name will trigger an initApi from the observer in the Service.

            // initialize the favorites
            Requestor.instance.initFavorites()
        }

        if (!isTimerStarted)
        {
            // timers
            // the clockTicker is used to update the UI. It's OK if it dies when the app loses focus.
            // the timer is declared after access to PlayerStore so that PlayerStore is already initialized:
            // Otherwise it makes the PlayerStore call its init{} block from a background thread --> crash
            clockTicker.schedule(
                Tick(),
                0,
                500
            )
            isTimerStarted = true
        }

        // initialize the UI
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
        attachKeyboardListeners()

        val toolbar : Toolbar = findViewById(R.id.toolbar)

        // before setting up the bottom bar, we must declare the top bar that will be used by the bottom bar to display titles.
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState
    }

    override fun onDestroy() {
        clockTicker.cancel()
        super.onDestroy()
    }

    // ####################################
    // ####### SERVICE PLAY / PAUSE #######
    // ####################################


    private fun actionOnService(a: Actions, v: Int = 0)
    {
            val i = Intent(this, RadioService::class.java)
            i.putExtra("action", a.name)
            i.putExtra("value", v)
            Log.d(tag, "Sending intent ${a.name}")
            startService(i)
    }


    // ####################################
    // ###### SERVICE BINDER MANAGER ######
    // ####################################

    // NO BINDERS, only intents. That's the magic.
    // Avoid code duplication, keep a single entry point to modify the service, and manage the service independently
    // (no coupling between service and activity, as it should be ! Cause the notification makes changes too.)


    /*
    // ####################################
    // ####### LOGGING TO FILE ############
    // ####################################

    // Checks if external storage is available for read and write
    private val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    // Checks if external storage is available to at least read
    private val isExternalStorageReadable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }


    private fun logToFile()
    {

        // Logging
        when {
            isExternalStorageWritable -> {

                val appDirectory = Environment.getExternalStorageDirectory()
                // File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/MyPersonalAppFolder")
                val logDirectory = File("$appDirectory/log")
                val logFile = File(logDirectory, "logcat" + System.currentTimeMillis() + ".txt")
                Log.d(
                    tag,
                    "appDirectory : $appDirectory, logDirectory : $logDirectory, logFile : $logFile"
                )

                // create app folder
                if (!appDirectory.exists()) {
                    appDirectory.mkdir()
                    Log.d(tag, "$appDirectory created")
                }

                // create log folder
                if (!logDirectory.exists()) {
                    logDirectory.mkdir()
                    Log.d(tag, "$logDirectory created")
                }

                // clear the previous logcat and then write the new one to the file
                try {
                    Runtime.getRuntime().exec("logcat -c")
                    Runtime.getRuntime().exec("logcat -v time -f $logFile *:E $tag:V ")
                    Log.d(tag, "logcat started")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            isExternalStorageReadable -> {
                // only readable
            }
            else -> {
                // not accessible
            }
        }
    }

     */

}
