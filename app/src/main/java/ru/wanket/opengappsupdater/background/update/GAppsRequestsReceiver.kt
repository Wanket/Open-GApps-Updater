package ru.wanket.opengappsupdater.background.update

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import ru.wanket.opengappsupdater.activity.MainActivity
import ru.wanket.opengappsupdater.Settings

class GAppsRequestsReceiver : BroadcastReceiver() {
    private var CHECK_UPDATE_JOB_ID = 1

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MainActivity.FIRST_LAUNCH_ACTION-> scheduleJob(context)
            else -> throw IllegalArgumentException("Unknown action.")
        }
    }

    private fun scheduleJob(context: Context) {
        val settings = Settings(context)

        JobInfo.Builder(CHECK_UPDATE_JOB_ID, ComponentName(context, GAppsJobService::class.java)).apply {
            setPeriodic(settings.checkUpdateTime)
            setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            setPersisted(true)
            //setOverrideDeadline(1) //use for fast debug
        }.let {
            (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(it.build())
        }
    }
}
