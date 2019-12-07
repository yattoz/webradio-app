package io.r_a_d.radio2

import android.os.Bundle
import io.r_a_d.radio2.preferences.*


class ParametersActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI Launch
        setTheme(R.style.AppTheme_Parameters)
        setContentView(R.layout.activity_parameters)

        // my_child_toolbar is defined in the layout file
        setSupportActionBar(findViewById(R.id.toolbar))

        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val extra = if (savedInstanceState == null) {
            intent.extras?.getString("action")
        } else {
            savedInstanceState.getSerializable("action") as String
        }

        val fragmentToLoad = when(extra) {
            ActionOpenParam.ALARM.name -> AlarmFragment()
            ActionOpenParam.SLEEP.name -> SleepFragment()
            ActionOpenParam.CUSTOMIZE.name -> CustomizeFragment()
            ActionOpenParam.STREAMER_NOTIFICATION_SERVICE.name -> StreamerNotifServiceFragment()
            else -> MainPreferenceFragment()
        }


        supportFragmentManager
            .beginTransaction()
            .replace(R.id.parameters_host_container, fragmentToLoad)
            .commit()
    }
}