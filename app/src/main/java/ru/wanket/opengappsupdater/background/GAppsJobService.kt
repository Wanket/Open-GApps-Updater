package ru.wanket.opengappsupdater.background

import android.app.job.JobParameters
import android.app.job.JobService

class GAppsJobService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        GAppsIntentService.startActionCheckUpdate(applicationContext)
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }
}
