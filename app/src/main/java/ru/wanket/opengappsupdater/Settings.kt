package ru.wanket.opengappsupdater

import android.content.Context
import android.preference.PreferenceManager

class Settings(private val context: Context) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val dayInSeconds = 86400L // day = 86400 sec

    var checkUpdateTime: Long
    get() {
        return preferences.getLong("checkUpdateTime", 10/*dayInSeconds*/)
    }
    set(value) {
        preferences.edit().putLong("checkUpdateTime", value).apply()
    }

    var isFirstLaunch: Boolean
    get() {
        return preferences.getBoolean("isFirstLaunch", false)
    }
    set(value) {
        preferences.edit().putBoolean("isFirstLaunch", value).apply()
    }

    var downloadInfo: DownloadInfo
    get() {
        return DownloadInfo(
                preferences.getInt("downloadInfo.version", -1),
                preferences.getBoolean("downloadInfo.isDownloaded", false))
    }
    set(value) {
        preferences.edit().putInt("downloadInfo.version", value.version).apply()
        preferences.edit().putBoolean("downloadInfo.isDownloaded", value.isDownloaded).apply()
    }
}
