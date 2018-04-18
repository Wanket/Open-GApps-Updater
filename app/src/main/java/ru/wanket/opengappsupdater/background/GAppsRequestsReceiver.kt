package ru.wanket.opengappsupdater.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.job.JobScheduler
import android.app.job.JobInfo
import android.content.ComponentName
import android.util.Log
import ru.wanket.opengappsupdater.Settings
import java.util.concurrent.TimeUnit


class GAppsRequestsReceiver : BroadcastReceiver() {

    private var sJobId = 1

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_BOOT_COMPLETED -> scheduleJob(context)
            else -> throw IllegalArgumentException("Unknown action.")
        }
    }

    private fun scheduleJob(context: Context) {
        val settings = Settings(context)

        JobInfo.Builder(sJobId++, ComponentName(context, GAppsJobService::class.java)).apply {
            setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
            setRequiresDeviceIdle(false)
            setRequiresCharging(false)
            setBackoffCriteria(TimeUnit.SECONDS.toMillis(settings.checkUpdateTime), JobInfo.BACKOFF_POLICY_LINEAR)
            setPeriodic(settings.checkUpdateTime)
        }.let {
            (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(it.build())
        }
    }
}
