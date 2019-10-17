package no.synth.revertalicious.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import no.synth.revertalicious.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings_fragment,
                SettingsFragment()
            )
            .commit()
    }
}
