package ru.wanket.opengappsupdater

import android.content.Context
import android.preference.PreferenceManager
import java.util.concurrent.TimeUnit

class Settings(context: Context) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val dayTime = TimeUnit.DAYS.toMillis(1)

    var checkUpdateTime: Long
        get() {
            return preferences.getLong("checkUpdateTime", dayTime)
        }
        set(value) {
            preferences.edit().putLong("checkUpdateTime", value).apply()
        }

    var isFirstLaunch: Boolean
        get() {
            return preferences.getBoolean("isFirstLaunch", true)
        }
        set(value) {
            preferences.edit().putBoolean("isFirstLaunch", value).apply()
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
            return preferences.getBoolean("autoCheckUpdate", true)
        }
        set(value) {
            preferences.edit().putBoolean("autoCheckUpdate", value).apply()
        }
}
