package ru.wanket.opengappsupdater.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class GitHubGApps(private val context: Context, arch: String) {
    private val urlUpdates = "https://api.github.com/repos/opengapps/$arch/releases/latest"

    fun getInfoGApps(completeListener: Response.Listener<String>, errorListener: Response.ErrorListener) {
        val queue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(Request.Method.GET, urlUpdates, completeListener, errorListener)
        queue.add(stringRequest)
    }
}
