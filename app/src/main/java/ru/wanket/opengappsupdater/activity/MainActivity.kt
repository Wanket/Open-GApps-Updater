package ru.wanket.opengappsupdater.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cyanogenmod.updater.utils.MD5
import com.downloader.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import ru.wanket.opengappsupdater.background.update.GAppsRequestsReceiver
import ru.wanket.opengappsupdater.gapps.GAppsInfo
import ru.wanket.opengappsupdater.console.RootConsole
import ru.wanket.opengappsupdater.network.GitHubGApps
import java.io.File
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import ru.wanket.opengappsupdater.R
import ru.wanket.opengappsupdater.Settings
import ru.wanket.opengappsupdater.Toast

class MainActivity : PermissionActivity() {

    companion object {
        const val FIRST_LAUNCH_ACTION = "ru.wanket.opengappsupdater.android.action.FIRST_LAUNCH"

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
        }
        get() {
            return downloadProgressBar.visibility
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setProperties()
        getSettings()
        PRDownloader.initialize(applicationContext)
        setPermissions()
        updateGAppsInfoOnUI()
        setupListeners()
        setupBackgroundTasks()
    }

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
        if (settings.isFirstLaunch) {

            val filter = IntentFilter(FIRST_LAUNCH_ACTION)
            registerReceiver(GAppsRequestsReceiver(), filter)
            val intent = Intent(FIRST_LAUNCH_ACTION)
            sendBroadcast(intent)

            settings.isFirstLaunch = false
        }
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
    //EndMenu

    //UIListeners
    private fun onCheckUpdateButtonClick() {
        GitHubGApps(this, gAppsInfo.arch).getInfoGApps(
                Response.Listener { response -> onResponseCheckUpdate(response) },
                Response.ErrorListener {})
    }

    private fun onResponseCheckUpdate(response: String) {
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
    }

    private fun onDownloadButtonClick() {
        val destination = "/${Environment.getExternalStorageDirectory().path}/Open GApps Updater/Downloads"
        val url = generateDownloadLink(gAppsInfo.arch, lastVersionTextView.text.toString(), gAppsInfo.platform, gAppsInfo.type)

        downloadId = PRDownloader.download(url, destination, "update.zip")
                .build()
                .setOnProgressListener { onProgressDownload(it) }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        this@MainActivity.onDownloadComplete()
                    }

                    override fun onError(error: Error) {}
                })

        downloadUIProgressVisible = Button.VISIBLE
        downloadProgressBar.progress = 0
        progressTextView.text = getString(R.string.start_download_mbytes)
    }

    @SuppressLint("SetTextI18n")
    private fun onProgressDownload(progress: Progress) {
        val mBytes = 1024 * 1024 / 10
        val current = progress.currentBytes / mBytes / 10.0
        val total = progress.totalBytes / mBytes / 10.0
        progressTextView.text = "$current/$total ${getString(R.string.mbytes)}"
        downloadProgressBar.progress = (progress.currentBytes * 100 / progress.totalBytes).toInt()
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
        val url = "${generateDownloadLink(gAppsInfo.arch, lastVersionTextView.text.toString(), gAppsInfo.platform, gAppsInfo.type)}.md5"
        val queue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response -> onCheckMD5(response) },
                Response.ErrorListener { })

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
        if (MD5.checkMD5(response.split(" ")[0], File(path))) {
            rootConsole.apply {
                exec("echo 'boot-recovery ' > /cache/recovery/command")
                exec("echo '--update_package=/sdcard/Open GApps Updater/Downloads/update.zip' >> /cache/recovery/command\n")
                exec("reboot recovery")
            }
        } else {
            Toast.show(applicationContext, getString(R.string.md5_check_error))
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
        }

        PRDownloader.pause(downloadId)
        pauseButton.text = getString(R.string.resume)
    }
    //EndUIListeners
}
