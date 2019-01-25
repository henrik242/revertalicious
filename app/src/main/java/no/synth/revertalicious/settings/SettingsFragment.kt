package no.synth.revertalicious.settings

import android.os.Bundle
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import no.synth.revertalicious.R
import no.synth.revertalicious.auth.AuthenticationMethod
import no.synth.revertalicious.settings.Settings.Companion.AUTH_METHOD
import no.synth.revertalicious.settings.Settings.Companion.PASSWORD
import no.synth.revertalicious.settings.Settings.Companion.PRIVATE_KEY
import no.synth.revertalicious.settings.Settings.Companion.REPOSITORY
import no.synth.revertalicious.settings.Settings.Companion.USERNAME

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val authMethod = findPreference(AUTH_METHOD) as ListPreference
        val repository = findPreference(REPOSITORY) as EditTextPreference
        val username = findPreference(USERNAME) as EditTextPreference
        val password = findPreference(PASSWORD) as EditTextPreference
        val privateKey = findPreference(PRIVATE_KEY) as EditTextPreference

        repository.summary = repository.text
        authMethod.summary = authMethod.value
        username.summary = username.text
        password.isVisible = AuthenticationMethod.parse(authMethod.value) == AuthenticationMethod.password
        privateKey.isVisible = AuthenticationMethod.parse(authMethod.value) == AuthenticationMethod.pubkey

        repository.setOnPreferenceChangeListener { preference, value ->
            preference.summary = value.toString()
            true
        }

        authMethod.setOnPreferenceChangeListener { preference, value ->
            preference.summary = value.toString().capitalize()
            password.isVisible = AuthenticationMethod.parse(value.toString()) == AuthenticationMethod.password
            privateKey.isVisible = AuthenticationMethod.parse(value.toString()) == AuthenticationMethod.pubkey
            true
        }

        username.setOnPreferenceChangeListener { preference, value ->
            preference.summary = value.toString()
            true
        }
    }
}
