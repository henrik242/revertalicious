package no.synth.revertalicious

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_fragment, SettingsFragment())
            .commit()
    }

    companion object {
        const val RESPOSITORY = "repository"
        const val AUTH_METHOD = "auth_method"
        const val PASSWORD = "password"
        const val PRIVATE_KEY = "private_key"
        const val NOTIFICATIONS = "notifications"
    }
}

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}