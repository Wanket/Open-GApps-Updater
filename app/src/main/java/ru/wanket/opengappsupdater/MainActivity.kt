package ru.wanket.opengappsupdater

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import ru.wanket.opengappsupdater.console.RootConsole

class MainActivity : AppCompatActivity() {

    private val rootConsole = RootConsole()
    private val root = Root(rootConsole)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (root.checkRoot()) {
            root.setupRoot()
            toast(getString(R.string.root_found))
        } else {
            toast(getString(R.string.root_not_found))
            finish()
        }
    }

    private fun toast(message: String) = Toast(this).apply {
        setText(message)
        show()
    }
}
