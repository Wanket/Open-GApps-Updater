package ru.wanket.opengappsupdater

import android.util.Log as aLog

object Log {

    fun e(tag: String, msg: String?) {
        aLog.e(tag, msg ?: "null")
    }

    fun w(tag: String, msg: String?) {
        aLog.w(tag, msg ?: "null")
    }

    fun d(tag: String, msg: String?) {
        aLog.d(tag, msg ?: "null")
    }

    fun i(tag: String, msg: String?) {
        aLog.i(tag, msg ?: "null")
    }

    fun v(tag: String, msg: String?) {
        aLog.v(tag, msg ?: "null")
    }

    fun wtf(tag: String, msg: String?) {
        aLog.wtf(tag, msg ?: "null")
    }
}