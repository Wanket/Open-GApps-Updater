package ru.wanket.opengappsupdater.gapps

import java.io.File
import java.util.*

data class GAppsInfo(val arch: String, val platform: String, val type: String, val version: Int) {
    companion object {

        private const val path = "/system/etc/g.prop"

        private const val arch = "ro.addon.arch"
        private const val platform = "ro.addon.platform"
        private const val type = "ro.addon.open_type"
        private const val version = "ro.addon.open_version"

        fun getCurrentGAppsInfo(): GAppsInfo {
            val prop = Properties()
            prop.load(File(path).inputStream())

            return GAppsInfo(prop.getProperty(arch), prop.getProperty(platform),
                    prop.getProperty(type), prop.getProperty(version).toInt())
        }
    }
}
