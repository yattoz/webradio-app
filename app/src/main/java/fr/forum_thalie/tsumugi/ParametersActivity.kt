package fr.forum_thalie.tsumugi

import android.os.Bundle
import android.view.MenuItem
import fr.forum_thalie.tsumugi.preferences.*


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
            else -> MainPreferenceFragment()
        }


        supportFragmentManager
            .beginTransaction()
            .replace(R.id.parameters_host_container, fragmentToLoad)
            .commit()
    }

    // Make the Up button function as back instead of always bringing us to the main activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}