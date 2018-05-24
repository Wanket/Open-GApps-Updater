package ru.wanket.opengappsupdater.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import mehdi.sakout.aboutpage.Element
import ru.wanket.opengappsupdater.BuildConfig
import ru.wanket.opengappsupdater.R
import ru.wanket.opengappsupdater.about.AboutPage

class AboutActivity : AppCompatActivity() {
    companion object {
        const val gitHub = "Wanket/Open-GApps-Updater"
        //const val forPDA = "http://4pda.ru/forum/index.php?showtopic=XXX" //TODO: добавить после создания топика
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val description = getString(R.string.description)
        val items = listOf(
                "${getString(R.string.version_name)}: ${BuildConfig.VERSION_NAME}",
                "${getString(R.string.version_code)}: ${BuildConfig.VERSION_CODE}")

        AboutPage(this).apply {
            title = getString(R.string.about)
            setImage(R.mipmap.ic_launcher_round)
            setDescription(description)

            addPlayStore("ru.wanket.opengappsupdater")
            //add4PDA(forPDA)
            addGitHub(gitHub)
            addEmail("wanket.yandex.ru")
            addItems(items)

            if (BuildConfig.DEBUG) {
                addItem(Element("DEBUG BUILD", null))
            }

        }.let {
            setContentView(it.create())
        }
    }
}
