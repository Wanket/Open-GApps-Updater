package ru.wanket.opengappsupdater

import android.content.Context
import android.widget.Toast as T

object Toast {
    fun show(context: Context, message: CharSequence) {
        T.makeText(context, message, T.LENGTH_LONG).show()
    }
}
