package ru.wanket.opengappsupdater.background.download

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.util.Log
import ru.wanket.opengappsupdater.Settings
import java.util.concurrent.atomic.AtomicBoolean


class Downloader(private val context: Context) {
    companion object {
        private fun generateDownloadLink(arch: CharSequence, version: CharSequence, androidVersion: CharSequence, type: CharSequence): String {
            return "https://github.com/opengapps/$arch/releases/download/$version/open_gapps-$arch-$androidVersion-$type-$version.zip"
        }

        private var revert = AtomicBoolean(false)
        private var downloadId = -1L
    }

    var version: Int = -1

    fun download(destination: Uri, arch: String, version: String, platform: String, type: String) {
        DownloadManager.Request(Uri.parse(generateDownloadLink(arch, version, platform, type))).apply {
            setDescription("Downloading GApps")
            setTitle("Downloading GApps")
            setDestinationUri(destination)
        }.let {
            synchronized(downloadId) {
                if (downloadId != -1L) {
                    AlertDialog.Builder(context).apply {
                        setMessage("Cancel current download and start new?")

                        setPositiveButton("YES", { _, _ ->
                            (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).remove(downloadId)
                            downloadId = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(it)
                            revert.set(true)
                        })

                        setNegativeButton("NO", { _, _ -> })
                    }.create().show()

                    return
                }
                downloadId = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(it)
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        this.version = version.toInt()
    }

    private val onComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            synchronized(downloadId) {
                Log.d("Downloader", "onReceive()")
                if (revert.get()) {
                    revert.set(false)
                    return
                }

                if (downloadId == -1L) { return }

                val settings = Settings(context)
                settings.downloadInfo.isDownloaded = true
                settings.downloadInfo.version = version
                downloadId = -1

            }
        }
    }
}
