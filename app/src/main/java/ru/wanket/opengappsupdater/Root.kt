package ru.wanket.opengappsupdater

import com.jaredrummler.android.shell.Shell

object Root {
    fun checkRoot(): Boolean {
        return Shell.SU.available()
    }
}
