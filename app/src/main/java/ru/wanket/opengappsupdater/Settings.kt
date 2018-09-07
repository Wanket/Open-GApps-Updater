package ru.wanket.opengappsupdater

import android.content.Context
import android.preference.PreferenceManager

class Settings(context: Context) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var checkUpdateTime: Int
        get() {
            return preferences.getInt("checkUpdateTime", 1)
        }
        set(value) {
            preferences.edit().putInt("checkUpdateTime", value).apply()
        }

    var lastVersion: Int
        get() {
            return preferences.getInt("lastVersion", -1)
        }
        set(value) {
            preferences.edit().putInt("lastVersion", value).apply()
        }

    var autoCheckUpdate: Boolean
        get() {
            return preferences.getBoolean("autoCheckUpdate", false)
        }
        set(value) {
            preferences.edit().putBoolean("autoCheckUpdate", value).apply()
        }

    var externalDownload: Boolean
        get() {
            return preferences.getBoolean("externalDownload", false)
        }
        set(value) {
            preferences.edit().putBoolean("externalDownload", value).apply()
        }
}
