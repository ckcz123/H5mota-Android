package com.h5mota.core.server

import android.util.Log
import com.h5mota.core.base.Constant
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.SimpleWebServer;
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import useHttpClientBuilder
import java.io.File
import java.io.IOException


class MotaWebServer(
    val host: String,
    val port: Int,
    val wwwRoot: File,
    quite: Boolean
): SimpleWebServer(host, port, wwwRoot , quite) {
    fun shouldRedirect(path: String): Boolean {
        return path == "/games/sync.php"
    }

    override fun serve(session: NanoHTTPD.IHTTPSession): Response? {
        val path = session.uri
        return if (session.method == Method.POST && shouldRedirect(path)) {
            try {
                session.parseBody(HashMap())
            } catch (e: ResponseException) {
                Log.e("Parse Body", "error", e)
            } catch (e: IOException) {
                Log.e("Parse Body", "error", e)
            }
            val okHttpClient = useHttpClientBuilder().build()
            val formBody = FormBody.Builder()
            val map: Map<String, List<String>> = session.getParameters()
            println("========> $map")
            map.forEach {(key, value) ->
                if (key.isNotEmpty() && value.isNotEmpty()) formBody.add(key, value[0])}
            try {
                okHttpClient
                    .newCall(
                        Request.Builder().url(Constant.DOMAIN + path).post(formBody.build()).build()
                    )
                    .execute().use { response ->
                        val code: Int = response.code
                        return if (code == 200) {
                            newFixedLengthResponse(
                                Response.Status.OK,
                                "application/json",
                                response.body?.string() ?: ""
                            )
                        } else {
                            newFixedLengthResponse(
                                Response.Status.lookup(code), "text/plain", response.message
                            )
                        }
                    }
            } catch (ignore: Exception) {
            }
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "")
        } else {
            super.serve(session)
        }
    }
}

