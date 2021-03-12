package no.synth.revertalicious

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import no.synth.revertalicious.settings.Settings
import no.synth.revertalicious.settings.Settings.Companion.PASSWORD
import no.synth.revertalicious.settings.Settings.Companion.PRIVATE_KEY
import no.synth.revertalicious.settings.Settings.Companion.REPOSITORY
import no.synth.revertalicious.settings.Settings.Companion.USERNAME
import no.synth.revertalicious.settings.SettingsActivity
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private var gitTask: GitTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val settings = Settings(this)

        gitTask = GitTask(
            settings.value(REPOSITORY) ?: "",
            settings.value(USERNAME),
            settings.value(PASSWORD),
            settings.value(PRIVATE_KEY),
            settings.authenticationMethod(),
            WeakReference(this@MainActivity)
        ).also { task ->

            lifecycleScope.launch {
                disableRevertButton(this@MainActivity, R.string.sync_waiting)
                withContext(Dispatchers.IO) {
                    task.cloneOrOpen()
                }
                enableRevertButton(this@MainActivity)
            }

            revert.setOnClickListener {
                verifyRevert(task)
            }
        }
    }

    private fun verifyRevert(task: GitTask) {
        disableRevertButton(this)

        AlertDialog.Builder(this)
            .setTitle("Reverting!")
            .setMessage("Do you really want to revert the last commit?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                performRevert(task)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                enableRevertButton(this)
            }
            .setOnKeyListener { dialog, keyCode, event ->
                if (event?.action == ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    performRevert(task)
                    dialog?.dismiss()
                    true
                } else {
                    false
                }
            }
            .show()
    }

    private fun performRevert(task: GitTask) {
        lifecycleScope.launch {
            disableRevertButton(this@MainActivity)
            withContext(Dispatchers.IO) {
                task.executeRevert()
            }
            enableRevertButton(this@MainActivity)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_ENTER -> {
            gitTask?.let { verifyRevert(it) }
            true
        }
        else -> super.onKeyUp(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun disableRevertButton(activity: Activity, textRes: Int = R.string.revert_waiting) {
            activity.findViewById<TextView>(R.id.main_description).setText(textRes)

            val blend = Color.argb(50, 0, 0, 0)
            activity.findViewById<ImageView>(R.id.revert).apply {
                background.setColorFilter(blend, PorterDuff.Mode.MULTIPLY)
                setColorFilter(blend, PorterDuff.Mode.MULTIPLY)
                isEnabled = false
            }
//            val blend = BlendModeColorFilter(Color.argb(50, 0, 0, 0), BlendMode.MULTIPLY)
//            activity.findViewById<ImageView>(R.id.revert).apply {
//                background.colorFilter = blend
//                colorFilter = blend
//                isEnabled = false
//            }
        }

        fun enableRevertButton(activity: Activity) {
            activity.findViewById<TextView>(R.id.main_description).setText(R.string.description)

            activity.findViewById<ImageView>(R.id.revert).apply {
                background.clearColorFilter()
                clearColorFilter()
                isEnabled = true
            }
        }
    }
}
