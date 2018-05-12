package ru.wanket.opengappsupdater

import android.content.Context
import android.preference.PreferenceManager
import java.util.concurrent.TimeUnit

class Settings(context: Context) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val dayTime = if (BuildConfig.DEBUG) TimeUnit.MINUTES.toMillis(15) else TimeUnit.DAYS.toMillis(1)

    var checkUpdateTime: Long
        get() {
            return preferences.getLong("checkUpdateTime", dayTime)
        }
        set(value) {
            preferences.edit().putLong("checkUpdateTime", value).apply()
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
}
