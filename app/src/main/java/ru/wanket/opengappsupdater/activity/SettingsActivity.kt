package ru.wanket.opengappsupdater.activity

import android.content.DialogInterface
import android.support.v14.preference.SwitchPreference
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceFragmentCompat
import android.widget.NumberPicker
import de.mrapp.android.preference.activity.NavigationPreference
import de.mrapp.android.preference.activity.PreferenceActivity
import ru.wanket.opengappsupdater.R
import ru.wanket.opengappsupdater.Settings


class SettingsActivity : PreferenceActivity() {

    private lateinit var settings: Settings

    private lateinit var autoCheckUpdateCheckBox: SwitchPreference
    private lateinit var externalDownloadCheckBox: SwitchPreference
    private lateinit var updatePeriodNavigationPreference : NavigationPreference

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
        updatePeriodNavigationPreference = fragment.findPreference("updatePeriodNavigationPreference") as NavigationPreference
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

        updatePeriodNavigationPreference.setOnPreferenceClickListener {
            buildNumberSelector().show()
            true
        }
    }

    private fun setupUI() {
        autoCheckUpdateCheckBox.isChecked = settings.autoCheckUpdate
        externalDownloadCheckBox.isChecked = settings.externalDownload
        updatePeriodNavigationPreference.summary = settings.checkUpdateTime.toString()
    }

    private fun onAutoCheckUpdateCheckBoxClicked(autoCheckUpdateCheckBox: SwitchPreference) {
        settings.autoCheckUpdate = autoCheckUpdateCheckBox.isChecked
    }

    private fun onExternalDownloadCheckBoxClicked(externalDownloadCheckBox: SwitchPreference) {
        settings.externalDownload = externalDownloadCheckBox.isChecked
    }

    private fun buildNumberSelector(): AlertDialog {
        return AlertDialog.Builder(this).apply {

            val numberPicker = NumberPicker(this@SettingsActivity).apply {
                maxValue = 366
                minValue = 1
                value = settings.checkUpdateTime
            }
            setView(numberPicker)

            setTitle(getString(R.string.update_period))
            setCancelable(true)
            setPositiveButton("OK") { _: DialogInterface, _: Int ->
                settings.checkUpdateTime = numberPicker.value
                setupUI()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.create()
    }
}
