package ru.wanket.opengappsupdater.console

open class Console {
    private val runtime = Runtime.getRuntime()

    open fun exec(command: String): Int {
        return runtime.exec(command).waitFor()
    }
}