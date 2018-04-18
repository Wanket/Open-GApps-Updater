package ru.wanket.opengappsupdater.console

class RootConsole : Console() {
    override fun exec(command: String): Int {
        return super.exec("su -c $command")
    }
}
