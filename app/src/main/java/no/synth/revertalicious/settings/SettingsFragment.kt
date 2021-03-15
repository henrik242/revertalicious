package no.synth.revertalicious.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import no.synth.revertalicious.R
import no.synth.revertalicious.auth.AuthenticationMethod
import no.synth.revertalicious.auth.AuthenticationMethod.Companion.nb_NO
import no.synth.revertalicious.settings.Settings.Companion.AUTH_METHOD
import no.synth.revertalicious.settings.Settings.Companion.PASSWORD
import no.synth.revertalicious.settings.Settings.Companion.PRIVATE_KEY
import no.synth.revertalicious.settings.Settings.Companion.REPOSITORY
import no.synth.revertalicious.settings.Settings.Companion.USERNAME

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val authMethod: ListPreference? = findPreference(AUTH_METHOD)
        val repository: EditTextPreference? = findPreference(REPOSITORY)
        val username: EditTextPreference? = findPreference(USERNAME)
        val password: EditTextPreference? = findPreference(PASSWORD)
        val privateKey: EditTextPreference? = findPreference(PRIVATE_KEY)

        repository?.apply { summary = text }
        authMethod?.apply {
            summary = value
            password?.apply { isVisible = AuthenticationMethod.parse(authMethod.value) == AuthenticationMethod.password }
            privateKey?.apply { isVisible = AuthenticationMethod.parse(authMethod.value) == AuthenticationMethod.pubkey }
        }
        username?.apply { summary = text }

        repository?.setOnPreferenceChangeListener { preference, value ->
            preference.summary = value.toString()
            true
        }

        authMethod?.setOnPreferenceChangeListener { preference, value ->
            preference.summary = value.toString().capitalize(nb_NO)
            password?.apply { isVisible = AuthenticationMethod.parse(value.toString()) == AuthenticationMethod.password }
            privateKey?.apply { isVisible = AuthenticationMethod.parse(value.toString()) == AuthenticationMethod.pubkey }
            true
        }

        username?.setOnPreferenceChangeListener { preference, value ->
            preference.summary = value.toString()
            true
        }
    }
}
