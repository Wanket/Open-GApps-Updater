package ru.wanket.opengappsupdater

import ru.wanket.opengappsupdater.console.RootConsole

class Root(private val rootConsole: RootConsole) {
    private var rootActive = false

    fun checkRoot(): Boolean {
        val process = rootConsole.exec("su -c true")
        return process.waitFor() == 0
    }

    fun setupRoot() {
        if (!rootActive) {
            rootConsole.exec("su")
            rootActive = true
        }
    }
}