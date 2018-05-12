package ru.wanket.opengappsupdater

import android.support.constraint.ConstraintLayout
import ru.wanket.opengappsupdater.gapps.GAppsInfo

class Application: android.app.Application() {
    var mainView: ConstraintLayout? = null
    var downloadId = -1
    lateinit var gAppsInfo: GAppsInfo
    lateinit var gAppsNotFound: String
    lateinit var settings: Settings
}