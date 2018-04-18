package ru.wanket.opengappsupdater.background

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log

class GAppsIntentService : IntentService("GAppsIntentService") {
    companion object {
        private val ACTION_CHECK_UPDATE = "ru.wanket.opengappsupdater.action.ACTION_CHECK_UPDATE"

        fun startActionCheckUpdate(context: Context) {
            val intent = Intent(context, GAppsIntentService::class.java)
            intent.action = ACTION_CHECK_UPDATE
            context.startService(intent)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_CHECK_UPDATE == action) {
                handleActionCheckUpdate()
            }
        }
    }

    private fun handleActionCheckUpdate() {
        //TODO Основная логика проверки обновлений здесь
        Log.d("handleActionCheckUpdate", "handleActionCheckUpdate()")
    }
}
