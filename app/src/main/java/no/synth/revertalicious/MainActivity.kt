package no.synth.revertalicious

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import no.synth.revertalicious.GitTask.Companion.AuthenticationMethod

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val settingsAuthMethod = AuthenticationMethod.parse(preferences.getString(SettingsActivity.AUTH_METHOD, null))
        val settingsRepoUrl = preferences.getString(SettingsActivity.RESPOSITORY, null) ?: ""
        val settingsPassword = preferences.getString(SettingsActivity.PASSWORD, null)
        val settingsPrivateKey = preferences.getString(SettingsActivity.PRIVATE_KEY, null)

        Toast.makeText(
            this,
            "Auth: $settingsAuthMethod Repo: $settingsRepoUrl Password: $settingsPassword Key: ${settingsPrivateKey != null}",
            Toast.LENGTH_LONG
        ).show()

        revert.setOnClickListener { _ ->
            GitTask(settingsRepoUrl, settingsPassword, settingsPrivateKey, settingsAuthMethod,this).execute()
        }
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
