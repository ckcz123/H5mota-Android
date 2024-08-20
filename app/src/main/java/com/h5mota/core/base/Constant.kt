package com.h5mota.core.base

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object Constant {
    var DOMAIN = "https://h5mota.com"
    var USE_PROXY = false
    const val PRESS_DOMAIN = "https://mota.press"
    const val LOCAL_HOST = "127.0.0.1"
    const val LOCAL_PORT = 1055
    const val LOCAL = "http://${LOCAL_HOST}:${LOCAL_PORT}/"
    val DIRECTORY = File(Environment.getExternalStorageDirectory(), "H5mota")
    val DATE_FORMATER = SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE)
    const val APK_FILE = "H5mota.apk"
    val LOG = mutableListOf("Log")

    fun isPlayingGame(url: String?): Boolean {
        return url.orEmpty().let {
            if (it.startsWith("$DOMAIN/games/")) true
            else it.startsWith(LOCAL)
        }
    }

    fun isInDomain(url: String?): Boolean {
        return url.orEmpty().startsWith(DOMAIN)
    }

    fun changeDomain(url: String) {
        DOMAIN = url;
    }

    fun changeProxyUsage(usage: Boolean) {
        USE_PROXY = usage;
    }
}