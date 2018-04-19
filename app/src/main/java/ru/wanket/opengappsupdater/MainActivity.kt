package ru.wanket.opengappsupdater

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import ru.wanket.opengappsupdater.background.GAppsRequestsReceiver
import ru.wanket.opengappsupdater.gapps.GAppsInfo
import ru.wanket.opengappsupdater.console.RootConsole
import ru.wanket.opengappsupdater.network.GitHubGApps

class MainActivity : AppCompatActivity() {
    companion object {
        private const val gAppsNotFound = "GAppsNotFound"
        private const val networkError = "Network not working"
        const val FIRST_LAUNCH_ACTION = "ru.wanket.opengappsupdater.android.action.FIRST_LAUNCH"

        private fun generateDownloadLink(arch: CharSequence, version: CharSequence, androidVersion: CharSequence, type: CharSequence): String {
            return "https://github.com/opengapps/$arch/releases/download/$version/open_gapps-$arch-$androidVersion-$type-$version.zip"
        }
    }

    private val rootConsole = RootConsole()
    private var downloadId = -1L
    private lateinit var downloadManager: DownloadManager
    private lateinit var gAppsInfo: GAppsInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPermissions()
        updateGAppsInfoOnUI()
        setupListeners()
        setupBackgroundTasks()

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }

    private fun setupBackgroundTasks() {
        val settings = Settings(this)
        if (true/*!settings.isFirstLaunch*/) {

            val filter = IntentFilter(FIRST_LAUNCH_ACTION)
            registerReceiver(GAppsRequestsReceiver(), filter)
            val intent = Intent(FIRST_LAUNCH_ACTION)
            sendBroadcast(intent)

            settings.isFirstLaunch = true
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
                    toast("No permission to write to storage, exit from program")
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
            Log.w(gAppsNotFound, e)

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
    }

    private fun onCheckUpdateButtonClick() {
        GitHubGApps(this).getInfoGApps(
                Response.Listener { response ->
                    onResponseCheckUpdate(response)
                },
                Response.ErrorListener {
                    Log.w(MainActivity.networkError, it)
                    toast(MainActivity.networkError)
                })
    }

    private fun onDownloadButtonClick() {
        synchronized(downloadId) {
            if (downloadId != -1L) {
                return
            }
        }

        installButton.visibility = Button.INVISIBLE

        val url = generateDownloadLink(gAppsInfo.arch, lastVersionTextView.text, gAppsInfo.platform, gAppsInfo.type)
        DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("Open GApps")
            setDescription("Download Open GApps")
            setDestinationUri(Uri.parse("file://${Environment.getExternalStorageDirectory().path}/Open GApps Updater/Downloads/update.zip"))
        }.let { downloadId = downloadManager.enqueue(it) }

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun onInstallButtonClick() {
        rootConsole.exec("su -c echo 'boot-recovery ' > /cache/recovery/command")
        rootConsole.exec("su -c echo '--update_package=/sdcard/Open GApps Updater/Downloads/update.zip' >> /cache/recovery/command\n")
        rootConsole.exec("su -c reboot recovery")
    }
    //EndUIListeners

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            installButton.visibility = Button.VISIBLE
            synchronized(downloadId) {
                downloadId = -1L
            }
        }
    }

    private fun toast(message: CharSequence) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun onResponseCheckUpdate(response: String) {
        val json = JSONObject(response)
        val version = json.getInt("tag_name")

        if (version <= gAppsInfo.version) {
            toast(getString(R.string.update_not_required))
            return
        }

        lastVersionTextView.text = version.toString()
        downloadButton.visibility = Button.VISIBLE
    }
}

