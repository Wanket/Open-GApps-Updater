package ru.wanket.opengappsupdater.console

import com.jaredrummler.android.shell.Shell

class RootConsole {
    fun exec(command: String): Int {
        return Shell.SU.run(command).exitCode
    }
}
