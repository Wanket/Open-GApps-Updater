package ru.wanket.opengappsupdater.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import mehdi.sakout.aboutpage.Element
import ru.wanket.opengappsupdater.R

class AboutPage(private val context: Context) : mehdi.sakout.aboutpage.AboutPage(context) {

    fun addItems(items: List<String>) {
        items.forEach {
            addItem(Element(it, null))
        }
    }

    fun add4PDA(url: String) {
        return add4PDA(url, context.getString(R.string.about_4PDA))
    }

    private fun add4PDA(url: String, title: String) {
        Element().apply {
            this.title = title
            iconDrawable = R.drawable.about_icon_4pda
            iconTint = R.color.about_4pda_color
            value = url

            intent = Intent().apply {
                action = Intent.ACTION_VIEW
                addCategory(Intent.CATEGORY_BROWSABLE)
                data = Uri.parse(url)
            }

            addItem(this)
        }
    }
}
