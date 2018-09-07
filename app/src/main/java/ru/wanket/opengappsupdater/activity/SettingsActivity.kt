package ru.wanket.opengappsupdater.activity

import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.PreferenceFragmentCompat
import de.mrapp.android.preference.activity.NavigationListener
import de.mrapp.android.preference.activity.NavigationPreference
import de.mrapp.android.preference.activity.PreferenceActivity
import ru.wanket.opengappsupdater.R
import ru.wanket.opengappsupdater.Settings



class SettingsActivity : PreferenceActivity() {

    private lateinit var settings: Settings

    private lateinit var autoCheckUpdateCheckBox: SwitchPreference
    private lateinit var externalDownloadCheckBox: SwitchPreference

    override fun onCreateNavigation(fragment: PreferenceFragmentCompat) {
        super.onCreateNavigation(fragment)
        fragment.addPreferencesFromResource(R.xml.fragment_settings)

        settings = Settings(this)

        setupProperties(fragment)
        setupOnClickListeners()
        setupUI()
    }

    private fun setupProperties(fragment: PreferenceFragmentCompat) {
        autoCheckUpdateCheckBox = fragment.findPreference("autoCheckUpdateCheckBox") as SwitchPreference
        externalDownloadCheckBox = fragment.findPreference("externalDownloadCheckBox") as SwitchPreference
    }

    private fun setupOnClickListeners() {
        autoCheckUpdateCheckBox.setOnPreferenceClickListener {
            preference ->  onAutoCheckUpdateCheckBoxClicked(preference as SwitchPreference)
            true
        }

        externalDownloadCheckBox.setOnPreferenceClickListener {
            preference ->  onExternalDownloadCheckBoxClicked(preference as SwitchPreference)
            true
        }
    }

    private fun setupUI() {
        autoCheckUpdateCheckBox.isChecked = settings.autoCheckUpdate
        externalDownloadCheckBox.isChecked = settings.externalDownload
    }

    private fun onAutoCheckUpdateCheckBoxClicked(autoCheckUpdateCheckBox: SwitchPreference) {
        settings.autoCheckUpdate = autoCheckUpdateCheckBox.isChecked
    }

    private fun onExternalDownloadCheckBoxClicked(externalDownloadCheckBox: SwitchPreference) {
        settings.externalDownload = externalDownloadCheckBox.isChecked
    }
}
