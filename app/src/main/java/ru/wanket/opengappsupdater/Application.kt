package ru.wanket.opengappsupdater

import android.support.constraint.ConstraintLayout
import ru.wanket.opengappsupdater.gapps.GAppsInfo

class Application: android.app.Application() {
    companion object {
        fun checkApp(app: android.app.Application, funNameForError: String): Application {
            return if (app is Application) app else {
                Log.e(funNameForError, "application is not instance of Application class")
                Application()
            }
        }
    }

    var mainView: ConstraintLayout? = null
    var downloadId = -1
    var isRoot = false
    lateinit var gAppsInfo: GAppsInfo
    lateinit var gAppsNotFound: String
    lateinit var settings: Settings
    var findViewCache: Any? = null
}
