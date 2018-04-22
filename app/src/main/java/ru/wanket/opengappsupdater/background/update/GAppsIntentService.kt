package ru.wanket.opengappsupdater.background.update

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.android.volley.Response
import org.json.JSONObject
import ru.wanket.opengappsupdater.R
import ru.wanket.opengappsupdater.gapps.GAppsInfo
import ru.wanket.opengappsupdater.network.GitHubGApps
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.app.PendingIntent
import ru.wanket.opengappsupdater.MainActivity
import ru.wanket.opengappsupdater.Settings


class GAppsIntentService : IntentService("GAppsIntentService") {
    companion object {
        private val ACTION_CHECK_UPDATE = "ru.wanket.opengappsupdater.action.ACTION_CHECK_UPDATE"
        private val CHANNEL_ID = "UPDATE_CHANNEL"
        private val UpdateNotificationID = 0

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
        GitHubGApps(this).getInfoGApps(
                Response.Listener { response ->
                    checkUpdate(response)
                },
                Response.ErrorListener {})
    }

    private fun checkUpdate(response: String) {
        if (!Settings(applicationContext).autoCheckUpdate) {
            return
        }

        val json = JSONObject(response)
        val version = json.getInt("tag_name")
        val gAppsInfo = GAppsInfo.getCurrentGAppsInfo()

        if (version > gAppsInfo.version) {
            setChannelId()
            NotificationCompat.Builder(this, CHANNEL_ID).apply {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

                setAutoCancel(true)
                setContentIntent(pendingIntent)
                setSmallIcon(R.mipmap.ic_launcher)
                setContentText("${getString(R.string.update_aviable)} $version")
            }.let {
                NotificationManagerCompat.from(this).notify(UpdateNotificationID, it.build())
            }
        }
    }

    private fun setChannelId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = "Updates"
            val importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = "Check Update"
            // Register the channel with the system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
