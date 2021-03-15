package no.synth.revertalicious

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*
import no.synth.revertalicious.databinding.ActivityMainBinding
import no.synth.revertalicious.settings.Settings
import no.synth.revertalicious.settings.SettingsActivity
import java.lang.ref.WeakReference

class MainActivity : Activity() {

    private var gitTask: GitTask? = null
    private lateinit var binding: ActivityMainBinding
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setActionBar(binding.toolbar)

        disableRevertButton(this, R.string.sync_waiting)

        refreshGitTask()

        binding.revert.setOnClickListener {
            verifyRevert()
        }
    }

    fun refreshGitTask() {
        disableRevertButton(this, R.string.sync_waiting)

        if (gitTask == null) {
            gitTask = GitTask(Settings(this), WeakReference(this))
        }
        var success: Boolean
        scope.launch {
            withContext(Dispatchers.IO) {
                success = gitTask?.cloneOrOpen() ?: false
            }
            if (success) {
                enableRevertButton(this@MainActivity)
            } else {
                disableRevertButton(this@MainActivity, R.string.missing_config)
            }
        }
    }

    private fun verifyRevert() {
        disableRevertButton(this)

        AlertDialog.Builder(this)
            .setTitle("Reverting!")
            .setMessage("Do you really want to revert the last commit?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                performRevert()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                enableRevertButton(this)
            }
            .setOnKeyListener { dialog, keyCode, event ->
                if (event?.action == ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    performRevert()
                    dialog?.dismiss()
                    true
                } else {
                    false
                }
            }
            .show()
    }

    private fun performRevert() {
        scope.launch {
            disableRevertButton(this@MainActivity)
            withContext(Dispatchers.IO) {
                gitTask?.executeRevert()
            }
            enableRevertButton(this@MainActivity)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_ENTER -> {
            gitTask?.let { verifyRevert() }
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

            val filter = PorterDuffColorFilter(Color.argb(50, 0, 0, 0), PorterDuff.Mode.MULTIPLY)
            activity.findViewById<ImageView>(R.id.revert).apply {
                background.colorFilter = filter
                colorFilter = filter
                isEnabled = false
            }
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
