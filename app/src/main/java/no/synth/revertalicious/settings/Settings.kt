package no.synth.revertalicious.settings

import android.content.SharedPreferences
import android.widget.Toast
import androidx.preference.PreferenceManager
import no.synth.revertalicious.MainActivity
import no.synth.revertalicious.R
import no.synth.revertalicious.auth.AuthenticationMethod
import no.synth.revertalicious.auth.AuthenticationMethod.pubkey

open class Settings(context: MainActivity) {

    private var preferences: SharedPreferences

    init {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        preferences.registerOnSharedPreferenceChangeListener { _, _ ->
            context.runOnUiThread {
                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                context.refreshGitTask()
            }
        }
    }

    open fun authenticationMethod(): AuthenticationMethod =
        preferences.getString(AUTH_METHOD, null)?.let { AuthenticationMethod.parse(it) } ?: pubkey

    open fun notifications(): Boolean = preferences.getBoolean(NOTIFICATIONS, false)

    open fun value(value: String): String? = preferences.getString(value, null)

    companion object {
        const val REPOSITORY = "repository"
        const val AUTH_METHOD = "auth_method"
        const val USERNAME = "username"
        const val PASSWORD = "password"
        const val PRIVATE_KEY = "private_key"
        const val NOTIFICATIONS = "notifications"
    }
}