package com.h5mota

import android.annotation.TargetApi
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.h5mota.core.base.Constant
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE != action) return
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val c = downloadManager.query(query)
        if (!c.moveToFirst()) return
        val columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
        if (DownloadManager.STATUS_SUCCESSFUL != c.getInt(columnIndex)) return
        val urlIndex = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
        if (urlIndex < 0) return;
        val uriString = c.getString(urlIndex)
        // TODO : Use this local uri and launch intent to open file
        val uri = Uri.parse(uriString)
        val file = File(uri.path)
        val name = file.name
        if (name.endsWith(".zip")) {
            if (Utils.unzip(file, Constant.DIRECTORY)) {
                Toast.makeText(context, "已成功下载并解压到到离线游戏列表中。", Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(
                    context,
                    "已成功下载文件，但是未能成功解压，请手动进行解压操作到 手机存储/H5mota 目录下以进行离线游戏。",
                    Toast.LENGTH_LONG
                ).show()

            }
        } else if (name.endsWith(".apk")) {
            Toast.makeText(
                context,
                "安装更新包已下载，请在通知栏手动进行安装。", Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                "$name 已经成功下载到 手机存储/Downloads 目录下！", Toast.LENGTH_LONG
            ).show()
        }
    }
}


object Utils {
    @TargetApi(24)
    fun unzip(file: File?, directory: File?, charset: Charset?): Boolean {
        if (Build.VERSION.SDK_INT < 24) return false
        try {
            FileInputStream(file).use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream), charset)
                    .use { zipInputStream ->
                        val buffer = ByteArray(1024)
                        var count: Int
                        while (true) {
                            val zipEntry = zipInputStream.nextEntry ?: break
                            val filename = zipEntry.name
                            if (zipEntry.isDirectory) {
                                File(directory, filename).mkdirs()
                            } else {
                                val outputStream =
                                    FileOutputStream(File(directory, filename))
                                while (zipInputStream.read(buffer).also { count = it } != -1) {
                                    outputStream.write(buffer, 0, count)
                                }
                                outputStream.close()
                                zipInputStream.closeEntry()
                            }
                        }
                        return true
                    }
            }
        } catch (e: java.lang.Exception) {
            Log.e("unzip", "error", e)
            return false
        }
    }

    @TargetApi(24)
    fun unzip(file: File?, directory: File?): Boolean {
        return (unzip(file, directory, Charset.forName("GBK"))
                || unzip(file, directory, StandardCharsets.UTF_8))
    }
}