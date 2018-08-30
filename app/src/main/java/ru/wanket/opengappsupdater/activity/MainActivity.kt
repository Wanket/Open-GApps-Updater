package ru.wanket.opengappsupdater.activity

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.*
import android.widget.Button
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cyanogenmod.updater.utils.MD5
import com.downloader.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import ru.wanket.opengappsupdater.*
import ru.wanket.opengappsupdater.R
import ru.wanket.opengappsupdater.background.update.GAppsJobService
import ru.wanket.opengappsupdater.console.RootConsole
import ru.wanket.opengappsupdater.gapps.GAppsInfo
import ru.wanket.opengappsupdater.network.GitHubGApps
import java.io.File

class MainActivity : PermissionActivity() {

    companion object {
        private const val checkUpdateJobID = 1
        private const val mBytes = 1024 * 1024 / 10

        private fun generateDownloadLink(arch: CharSequence, version: CharSequence, androidVersion: CharSequence, type: CharSequence): String {
            return "https://github.com/opengapps/$arch/releases/download/$version/open_gapps-$arch-$androidVersion-$type-$version.zip"
        }
    }

    private val rootConsole = RootConsole()
    private var downloadId = -1
    private lateinit var gAppsInfo: GAppsInfo
    private lateinit var gAppsNotFound: String
    private lateinit var settings: Settings

    private var downloadUIProgressVisible: Int
        set(value) {
            downloadProgressBar.visibility = value
            pauseButton.visibility = value
            cancelButton.visibility = value
            progressTextView.visibility = value

            if (value == Button.INVISIBLE) {
                pauseButton.text = getString(R.string.pause)
            }
        }
        get() {
            return downloadProgressBar.visibility
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (setupSavedState()) return

        setContentView(R.layout.activity_main)

        heatingUIHash()
        setProperties()
        getSettings()
        PRDownloader.initialize(applicationContext)
        updateGAppsInfoOnUI()
        setupListeners()
        setupBackgroundTasks()
    }

    //SaveState
    private fun setupSavedState(): Boolean {
        val app = Application.checkApp(application, "setupSavedState")
        try {
            if (app.mainView != null) {
                setContentView(app.mainView)
                downloadId = app.downloadId
                gAppsInfo = app.gAppsInfo
                gAppsNotFound = app.gAppsNotFound
                settings = app.settings

                javaClass.getDeclaredField("_${'$'}_findViewCache").set(this, app.findViewCache)

                return true
            }
        } catch (e: kotlin.UninitializedPropertyAccessException) {
            Log.w("setupSavedState", "Error while load saved ${e.message?.split(" ")?.get(2)} state.")
        } catch (e: Exception) {
            Log.e("setupSavedState", "Error while load saved state program. ${e.message}")
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        saveState()
    }

    private fun saveState() {
        try {
            (mainView.parent as ViewGroup).removeView(mainView)

            val app = Application.checkApp(application, "saveState")
            app.mainView = mainView
            app.downloadId = downloadId
            app.gAppsInfo = gAppsInfo
            app.gAppsNotFound = gAppsNotFound
            app.settings = settings

            app.findViewCache = javaClass.getDeclaredField("_${'$'}_findViewCache").get(this)
        } catch (e: kotlin.UninitializedPropertyAccessException) {
            Log.w("saveState", "Error while save ${e.message?.split(" ")?.get(2)} state.")
        } catch (e: Exception) {
            Log.e("saveState", "Error while save state program. ${e.message}")
        }
    }

    private fun heatingUIHash() {
        downloadUIProgressVisible = downloadUIProgressVisible
        downloadButton.visibility = downloadButton.visibility
        tvlv.visibility = tvlv.visibility
        lastVersionTextView.visibility = lastVersionTextView.visibility
        installButton.visibility = installButton.visibility
    }
    //EndSaveState

    //onCreateSetups
    private fun setProperties() {
        settings = Settings(this)
        gAppsNotFound = getString(R.string.gapps_not_found)
    }

    private fun getSettings() {
        if (settings.lastVersion != -1) {
            lastVersionTextView.text = settings.lastVersion.toString()
            installButton.visibility = Button.VISIBLE
            lastVersionTextView.visibility = TextView.VISIBLE
            tvlv.visibility = TextView.VISIBLE
        }
    }

    private fun updateGAppsInfoOnUI() {
        try {
            gAppsInfo = GAppsInfo.getCurrentGAppsInfo()

            archTextView.text = gAppsInfo.arch
            platformVersionTextView.text = gAppsInfo.platform
            typeTextView.text = gAppsInfo.type
            currentVersionTextView.text = gAppsInfo.version.toString()

        } catch (e: Exception) {

            archTextView.text = gAppsNotFound
            platformVersionTextView.text = gAppsNotFound
            typeTextView.text = gAppsNotFound
            currentVersionTextView.text = gAppsNotFound

            checkUpdateButton.visibility = Button.INVISIBLE
            tvlv.visibility = Button.INVISIBLE
            lastVersionTextView.visibility = Button.INVISIBLE
        }
    }

    private fun setupListeners() {
        checkUpdateButton.setOnClickListener { onCheckUpdateButtonClick() }
        downloadButton.setOnClickListener { onDownloadButtonClick() }
        installButton.setOnClickListener { onInstallButtonClick() }
        pauseButton.setOnClickListener { onPauseButtonClick() }
        cancelButton.setOnClickListener { onCancelButtonClick() }
    }

    private fun setupBackgroundTasks() {
        val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.allPendingJobs.find {
            if (it.id == checkUpdateJobID) {
                return
            } else {
                return@find false
            }
        }

        JobInfo.Builder(checkUpdateJobID, ComponentName(this, GAppsJobService::class.java)).apply {
            setPeriodic(settings.checkUpdateTime)
            setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            setPersisted(true)
            //setOverrideDeadline(1) //use for fast debug
        }.let { scheduler.schedule(it.build()) }
    }
    //EndOnCreateSetups

    //Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val item = menu.findItem(R.id.checkUpdateItem)
        item.isChecked = settings.autoCheckUpdate
        return true
    }

    fun onAutoCheckUpdateClick(item: MenuItem) {
        item.isChecked = !item.isChecked
        settings.autoCheckUpdate = item.isChecked
    }

    fun onAboutItemClick(item: MenuItem) {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }
    //EndMenu

    //UIListeners
    private fun onCheckUpdateButtonClick() {
        GitHubGApps(this, gAppsInfo.arch).getInfoGApps(
                Response.Listener { response -> onResponseCheckUpdate(response) },
                Response.ErrorListener {
                    Log.e("onCheckUpdateButtonClk", it.message)
                    Toast.show(this, getString(R.string.network_error))
                })
    }

    private fun onResponseCheckUpdate(response: String) {
        try {
            val json = JSONObject(response)
            val version = json.getInt("tag_name")

            if (version <= gAppsInfo.version) {
                Toast.show(this, getString(R.string.update_not_required))
                return
            }

            lastVersionTextView.text = version.toString()
            lastVersionTextView.visibility = TextView.VISIBLE
            tvlv.visibility = TextView.VISIBLE
            downloadButton.visibility = Button.VISIBLE

        } catch (e: Exception) {
            Log.e("onResponseCheckUpdate", e.message)
            Toast.show(this, getString(R.string.error_parse_json))
        }
    }

    private fun onDownloadButtonClick() {
        if (PRDownloader.getStatus(downloadId) != Status.UNKNOWN) {
            return
        }

        val handler = Handler()
        handler.post(object : Runnable {

            private var progress: Progress? = null

            private val frameTime = (2000 / (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate).toLong() // Обновляем каждый второй кадр

            init {
                val destination = "${Environment.getExternalStorageDirectory().path}/Open GApps Updater/Downloads"
                val url = generateDownloadLink(gAppsInfo.arch, lastVersionTextView.text.toString(), gAppsInfo.platform, gAppsInfo.type)

                downloadId = PRDownloader.download(url, destination, "update.zip")
                        .build()
                        .setOnProgressListener { progress = it }
                        .start(object : OnDownloadListener {
                            override fun onDownloadComplete() {
                                this@MainActivity.onDownloadComplete()
                            }

                            override fun onError(error: Error) {
                                Log.e("onDownloadError", if (error.isConnectionError) "Connection Error" else "Server Error")
                                Toast.show(applicationContext, if (error.isConnectionError) getString(R.string.connection_error) else getString(R.string.server_error))
                                downloadUIProgressVisible = View.INVISIBLE
                            }
                        })

                downloadUIProgressVisible = Button.VISIBLE
                downloadProgressBar.progress = 0
                progressTextView.text = getString(R.string.start_download_mbytes)
            }

            @SuppressLint("SetTextI18n")
            override fun run() {
                val lProgress = progress

                if (lProgress != null) {
                    val current = lProgress.currentBytes / mBytes / 10.0
                    val total = lProgress.totalBytes / mBytes / 10.0
                    progressTextView.text = "$current / $total ${getString(R.string.mbytes)}"
                    downloadProgressBar.progress = (lProgress.currentBytes * 100 / lProgress.totalBytes).toInt()
                }

                if (PRDownloader.getStatus(downloadId) != Status.UNKNOWN) {
                    handler.postDelayed(this, frameTime)
                }
            }
        })
    }

    fun onDownloadComplete() {
        installButton.visibility = Button.VISIBLE
        downloadUIProgressVisible = Button.INVISIBLE

        settings.lastVersion = lastVersionTextView.text.toString().toInt()
    }

    private fun onInstallButtonClick() {
        checkMD5()
    }

    private fun checkMD5() {
        val url = try {
            "${generateDownloadLink(gAppsInfo.arch, lastVersionTextView.text.toString(), gAppsInfo.platform, gAppsInfo.type)}.md5"
        } catch (e: UninitializedPropertyAccessException) {
            Log.w("checkMD5", "Error while load saved ${e.message?.split(" ")?.get(2)} state.")
            if (setupSavedState()) {
                "${generateDownloadLink(gAppsInfo.arch, lastVersionTextView.text.toString(), gAppsInfo.platform, gAppsInfo.type)}.md5"
            } else {
                Log.e("checkMD5", "Error while generate download link in checkMD5")
                Toast.show(this, getString(R.string.error_check_md5_ErrorInSetupSavedState))
                return@checkMD5
            }
        }

        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response -> onCheckMD5(response) },
                Response.ErrorListener {
                    Log.e("checkMD5", it.message)
                    Toast.show(this, getString(R.string.network_error))
                })

        queue.add(stringRequest)
    }

    private fun onCheckMD5(response: String) {
        val path = "${Environment.getExternalStorageDirectory().path}/Open GApps Updater/Downloads/update.zip"
        Toast.show(this, getString(R.string.check_md5_and_reboot))

        class AsyncMD5 : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?) {
                asyncCheckMD5(response, path)
            }
        }

        AsyncMD5().execute()
    }

    private fun asyncCheckMD5(response: String, path: String) {
        try {
            if (MD5.checkMD5(response.split(" ")[0], File(path))) {
                rootConsole.apply {
                    exec("echo 'boot-recovery ' > /cache/recovery/command")
                    exec("echo '--update_package=/sdcard/Open GApps Updater/Downloads/update.zip' >> /cache/recovery/command\n")
                    exec("reboot recovery")
                }
            } else {
                Toast.onNonUIThreadShow(this, getString(R.string.md5_check_error))
            }

        } catch (e: Exception) {
            Log.e("asyncCheckMD5", e.message)
            Toast.onNonUIThreadShow(this, getString(R.string.error_check_md5))
        }
    }

    private fun onCancelButtonClick() {
        PRDownloader.cancel(downloadId)
        downloadUIProgressVisible = Button.INVISIBLE
    }

    private fun onPauseButtonClick() {
        if (PRDownloader.getStatus(downloadId) == Status.PAUSED) {
            PRDownloader.resume(downloadId)
            pauseButton.text = getString(R.string.pause)
        } else {
            PRDownloader.pause(downloadId)
            pauseButton.text = getString(R.string.resume)
        }
    }
    //EndUIListeners
}
