package ru.wanket.opengappsupdater.console

class RootConsole : IConsole {

    private val runtime = Runtime.getRuntime()

    override fun exec(command: String): Process {
        return runtime.exec(command)
    }
}
