package no.synth.revertalicious.settings

import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import android.widget.Toast
import no.synth.revertalicious.MainActivity
import no.synth.revertalicious.R
import no.synth.revertalicious.auth.AuthenticationMethod
import no.synth.revertalicious.auth.AuthenticationMethod.pubkey

class Settings(context: MainActivity) {

    private var preferences: SharedPreferences

    init {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        preferences.registerOnSharedPreferenceChangeListener { _, _ ->
            context.runOnUiThread {
                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun authenticationMethod(): AuthenticationMethod =
        preferences.getString(AUTH_METHOD, null)?.let { AuthenticationMethod.parse(it) } ?: pubkey

    fun notifications(): Boolean = preferences.getBoolean(NOTIFICATIONS, false)

    fun value(value: String): String? = preferences.getString(value, null)

    companion object {
        const val RESPOSITORY = "repository"
        const val AUTH_METHOD = "auth_method"
        const val USERNAME = "username"
        const val PASSWORD = "password"
        const val PRIVATE_KEY = "private_key"
        const val NOTIFICATIONS = "notifications"
    }
}