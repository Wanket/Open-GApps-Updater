package ru.wanket.opengappsupdater

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
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
import android.view.View
import android.widget.*


class MainActivity : AppCompatActivity() {
    companion object {
        const val FIRST_LAUNCH_ACTION = "ru.wanket.opengappsupdater.android.action.FIRST_LAUNCH"

        private fun generateDownloadLink(arch: CharSequence, version: CharSequence, androidVersion: CharSequence, type: CharSequence): String {
            return "https://github.com/opengapps/$arch/releases/download/$version/open_gapps-$arch-$androidVersion-$type-$version.zip"
        }
    }

    private val rootConsole = RootConsole()
    private lateinit var gAppsInfo: GAppsInfo
    private var downloadId = -1
    private lateinit var gAppsNotFound: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getSettings()
        setProperties()
        PRDownloader.initialize(applicationContext)
        getPermissions()
        updateGAppsInfoOnUI()
        setupListeners()
        setupBackgroundTasks()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val item = menu.findItem(R.id.checkUpdateItem)
        item.isChecked = Settings(this).autoCheckUpdate
        return true
    }

    fun onAutoCheckUpdateClick(item: MenuItem) {
        item.isChecked = !item.isChecked
        val settings = Settings(this)
        settings.autoCheckUpdate = item.isChecked
    }

    private fun getSettings() {
        val settings = Settings(this)
        if (settings.lastVersion != -1) {
            installButton.visibility = Button.VISIBLE
            lastVersionTextView.text = settings.lastVersion.toString()
            lastVersionTextView.visibility = TextView.VISIBLE
            tvlv.visibility = TextView.VISIBLE
        }
    }

    private fun setProperties() {
        gAppsNotFound = getString(R.string.gapps_not_found)
    }

    private fun setupBackgroundTasks() {
        val settings = Settings(this)
        if (settings.isFirstLaunch) {

            val filter = IntentFilter(FIRST_LAUNCH_ACTION)
            registerReceiver(GAppsRequestsReceiver(), filter)
            val intent = Intent(FIRST_LAUNCH_ACTION)
            sendBroadcast(intent)

            settings.isFirstLaunch = false
        }
    }

    //Permissions
    private fun getPermissions() {
        updateRoot()

        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    toast(getString(R.string.no_perm_rw_storage))
                    finish()
                }
            }
        }
    }

    private fun updateRoot() {
        if (!Root.checkRoot()) {
            toast(getString(R.string.root_not_found))
            finish()
        }

        toast(getString(R.string.root_found))
    }
    //EndPermissions

    //GApps
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
    //EndGApps

    //UIListeners
    private fun setupListeners() {
        checkUpdateButton.setOnClickListener { onCheckUpdateButtonClick() }
        downloadButton.setOnClickListener { onDownloadButtonClick() }
        installButton.setOnClickListener { onInstallButtonClick() }
        pauseButton.setOnClickListener {
            if (PRDownloader.getStatus(downloadId) == Status.PAUSED) {
                PRDownloader.resume(downloadId)
                pauseButton.text = getString(R.string.pause)
            }

            PRDownloader.pause(downloadId)
            pauseButton.text = getString(R.string.resume)
        }
        cancelButton.setOnClickListener {
            PRDownloader.cancel(downloadId)
            downloadProgressBar.visibility = Button.INVISIBLE
            pauseButton.visibility = Button.INVISIBLE
            cancelButton.visibility = Button.INVISIBLE
            progressTextView.visibility = Button.INVISIBLE
        }
    }

    private fun onCheckUpdateButtonClick() {
        GitHubGApps(this).getInfoGApps(
                Response.Listener { response ->
                    onResponseCheckUpdate(response)
                },
                Response.ErrorListener {})
    }

    private fun onDownloadButtonClick() {
        val destination = "/${Environment.getExternalStorageDirectory().path}/Open GApps Updater/Downloads"
        val url = generateDownloadLink(gAppsInfo.arch, lastVersionTextView.text.toString(), gAppsInfo.platform, gAppsInfo.type)

        downloadId = PRDownloader.download(url, destination, "update.zip")
                .build()
                .setOnStartOrResumeListener { }
                .setOnPauseListener { }
                .setOnCancelListener { }
                .setOnProgressListener {
                    val mBytes = 1024 * 1024 / 10
                    val current = it.currentBytes / mBytes / 10.0
                    val total = it.totalBytes / mBytes / 10.0
                    progressTextView.text = "$current/$total ${getString(R.string.mbytes)}"
                    downloadProgressBar.progress = (it.currentBytes * 100 / it.totalBytes).toInt()
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        installButton.visibility = Button.VISIBLE
                        downloadProgressBar.visibility = Button.INVISIBLE
                        pauseButton.visibility = Button.INVISIBLE
                        cancelButton.visibility = Button.INVISIBLE
                        progressTextView.visibility = Button.INVISIBLE

                        val settings = Settings(applicationContext)
                        settings.lastVersion = lastVersionTextView.text.toString().toInt()
                    }

                    override fun onError(error: Error) {}
                })

        downloadProgressBar.progress = 0
        progressTextView.text = getString(R.string.start_download_mbytes)
        downloadProgressBar.visibility = ProgressBar.VISIBLE
        pauseButton.visibility = Button.VISIBLE
        cancelButton.visibility = Button.VISIBLE
    }

    private fun onInstallButtonClick() {
        checkMD5()
    }

    private fun checkMD5() {
        val url = "${generateDownloadLink(gAppsInfo.arch, lastVersionTextView.text.toString(), gAppsInfo.platform, gAppsInfo.type)}.md5"
        val queue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    onCheckMD5(response)
                },
                Response.ErrorListener { })

        queue.add(stringRequest)
    }

    private fun onCheckMD5(response: String) {
        val path = "${Environment.getExternalStorageDirectory().path}/Open GApps Updater/Downloads/update.zip"
        toast(getString(R.string.check_md5_and_reboot))
        if (MD5.checkMD5(response.split(" ")[0], File(path))) {
            rootConsole.apply {
                exec("echo 'boot-recovery ' > /cache/recovery/command")
                exec("echo '--update_package=/sdcard/Open GApps Updater/Downloads/update.zip' >> /cache/recovery/command\n")
                exec("reboot recovery")
            }
        } else {
            toast(getString(R.string.md5_check_error))
        }
    }
    //EndUIListeners

    private fun toast(message: CharSequence) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun onResponseCheckUpdate(response: String) {
        val json = JSONObject(response)
        val version = json.getInt("tag_name")

        if (false/*version <= gAppsInfo.version*/) {
            toast(getString(R.string.update_not_required))
            return
        }

        lastVersionTextView.text = version.toString()
        lastVersionTextView.visibility = TextView.VISIBLE
        tvlv.visibility = TextView.VISIBLE
        downloadButton.visibility = Button.VISIBLE
    }
}
