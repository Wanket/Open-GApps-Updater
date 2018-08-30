package ru.wanket.opengappsupdater.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import ru.wanket.opengappsupdater.*

abstract class PermissionActivity : AppCompatActivity() {

    companion object {
        const val WRITE_STORAGE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPermissions()
    }

    private fun setPermissions() {
        val app = Application.checkApp(application, "setPermissions")

        if (!app.isRoot) {
            updateRoot()
        }

        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_STORAGE -> if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.show(this, getString(R.string.no_perm_rw_storage))
                finish()
            }
        }
    }

    private fun updateRoot() {
        val app = Application.checkApp(application, "updateRoot")
        app.isRoot = Root.checkRoot()
        if (!app.isRoot) {
            Toast.show(this, getString(R.string.root_not_found))
            finish()
            return
        }

        Toast.show(this, getString(R.string.root_found))
    }
}
