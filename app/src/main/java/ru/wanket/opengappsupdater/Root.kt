package ru.wanket.opengappsupdater

import ru.wanket.opengappsupdater.console.RootConsole

object Root {
    fun checkRoot(): Boolean {
        val console = RootConsole()
        return console.exec("su -c true") == 0
    }
}