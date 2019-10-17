package no.synth.revertalicious

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import no.synth.revertalicious.settings.Settings
import no.synth.revertalicious.settings.Settings.Companion.PASSWORD
import no.synth.revertalicious.settings.Settings.Companion.PRIVATE_KEY
import no.synth.revertalicious.settings.Settings.Companion.REPOSITORY
import no.synth.revertalicious.settings.Settings.Companion.USERNAME
import no.synth.revertalicious.settings.SettingsActivity
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private var settings: Settings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        settings = Settings(this)

        revert.setOnClickListener {
            settings?.let { verifyRevert(it) }
        }
    }

    private fun verifyRevert(settings: Settings) {
        AlertDialog.Builder(this)
            .setTitle("Reverting!")
            .setMessage("Do you really want to revert the last commit?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                performRevert(settings)
            }
            .setNegativeButton(android.R.string.no, null)
            .setOnKeyListener(object : DialogInterface.OnKeyListener {
                override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
                    return if (event?.action == ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                        performRevert(settings)
                        dialog?.dismiss()
                        true
                    } else {
                        false
                    }
                }
            })
            .show()
    }

    private fun performRevert(settings: Settings) {
        GitTask(
            settings.value(REPOSITORY) ?: "",
            settings.value(USERNAME),
            settings.value(PASSWORD),
            settings.value(PRIVATE_KEY),
            settings.authenticationMethod(),
            WeakReference(this@MainActivity)
        ).execute()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_ENTER -> {
            settings?.let { verifyRevert(it) }
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
}
