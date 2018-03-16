package ru.wanket.opengappsupdater.console

interface IConsole {
    fun exec(command: String): Process
}
