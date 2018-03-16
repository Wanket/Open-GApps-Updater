package ru.wanket.opengappsupdater

import ru.wanket.opengappsupdater.console.RootConsole

class Root(private val rootConsole: RootConsole) {

    fun checkRoot(): Boolean {
        val process = rootConsole.exec("su -c true")
        return process.exitValue() == 0
    }

    fun setupRoot() {
        rootConsole.exec("su")
    }
}