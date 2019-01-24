package no.synth.revertalicious.settings

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import no.synth.revertalicious.R
import no.synth.revertalicious.auth.AuthenticationMethod
import no.synth.revertalicious.auth.AuthenticationMethod.pubkey

class Settings(context: Context) {

    private var preferences: SharedPreferences

    init {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun authenticationMethod(): AuthenticationMethod =
        preferences.getString(AUTH_METHOD, null)?.let { AuthenticationMethod.parse(it) } ?: pubkey

    fun notifications(): Boolean = preferences.getBoolean(NOTIFICATIONS, false)

    fun value(value: String): String? = preferences.getString(value, null)

    companion object {
        const val RESPOSITORY = "repository"
        const val AUTH_METHOD = "auth_method"
        const val PASSWORD = "password"
        const val PRIVATE_KEY = "private_key"
        const val NOTIFICATIONS = "notifications"
    }
}