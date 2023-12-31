package com.h5mota.core.component

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberSaveableWebViewState
import com.google.accompanist.web.rememberWebViewNavigator
import com.h5mota.MainActivity
import com.h5mota.R
import com.h5mota.ui.Constant
import com.h5mota.ui.getMainActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.Date


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebScreen(url: String, onUrlLoaded: ((String?) -> Unit)? = null) {
    var loading by remember { mutableStateOf(false) }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    val activity = getMainActivity(LocalContext.current as ContextWrapper)
    val saveDir = activity.getExternalFilesDir("saves")!!

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (loading) {
            LinearProgressIndicator(progress = pageProgress, modifier = Modifier.fillMaxWidth())
        }

        val state = rememberSaveableWebViewState()
        val navigator = rememberWebViewNavigator()
        val logFile = File(
            activity.getExternalFilesDir("_logs"),
            "log-" + Constant.DATE_FORMATER.format(Date()) + ".txt"
        )

        val webClient = remember {
            object : AccompanistWebViewClient() {
                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    loading = true
                    onUrlLoaded?.invoke(url)
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    super.onPageFinished(view, url)
                    loading = false

                    maybeInjectLocalForage(view, saveDir)
                }

                @SuppressLint("WebViewClientOnReceivedSslError")
                override fun onReceivedSslError(
                    view: WebView?, handler: SslErrorHandler?, error: SslError?
                ) {
                    handler?.proceed()
                }
            }
        }

        val webChromeClient = remember {
            object : AccompanistWebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    pageProgress = newProgress / 100f
                }

                override fun onJsAlert(
                    view: WebView?, url: String?, message: String?, result: JsResult
                ): Boolean {
                    AlertDialog.Builder(activity).setTitle("JsAlert").setMessage(message)
                        .setPositiveButton(
                            "OK"
                        ) { _, _ -> result.confirm() }.setCancelable(false).show()
                    return true
                }

                override fun onJsConfirm(
                    view: WebView?, url: String?, message: String?, result: JsResult
                ): Boolean {
                    AlertDialog.Builder(activity).setTitle("Javascript发来的提示")
                        .setMessage(message).setPositiveButton(
                            "OK"
                        ) { _, _ -> result.confirm() }.setNegativeButton(
                            "Cancel"
                        ) { _, _ -> result.cancel() }.setCancelable(false).show()
                    return true
                }

                override fun onJsPrompt(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    defaultValue: String?,
                    result: JsPromptResult
                ): Boolean {
                    val et = EditText(activity)
                    et.setText(defaultValue)
                    AlertDialog.Builder(activity).setTitle(message).setView(et).setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        result.confirm(et.text.toString())
                    }.setNegativeButton(
                        "Cancel"
                    ) { _, _ -> result.cancel() }.setCancelable(false).show()
                    return true
                }

                override fun onShowFileChooser(
                    mWebView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri?>?>,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "*/*"
                    activity.jsActivityLauncher.launch(
                        Intent.createChooser(
                            intent, "请选择文件"
                        )
                    ) { result ->
                        if (result.resultCode != Activity.RESULT_OK) return@launch

                        filePathCallback.onReceiveValue(
                            WebChromeClient.FileChooserParams.parseResult(
                                result.resultCode, result.data
                            )
                        )
                    }
                    return true
                }

                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    val msg = message.message();
                    Log.i("H5mota_WebActivity", msg);
                    if (msg.equals("[object Object]") || msg.equals("localForage supported!") || msg.equals(
                            "插件编写测试"
                        ) || msg.equals("开始游戏") || msg.startsWith("插件函数转发") || msg.startsWith(
                            "警告！"
                        )
                    ) {
                        return false;
                    }
                    val level: ConsoleMessage.MessageLevel = message.messageLevel();
                    if (level != ConsoleMessage.MessageLevel.LOG && level != ConsoleMessage.MessageLevel.ERROR) {
                        return false;
                    }
                    val printWriter = PrintWriter(FileWriter(logFile, true))

                    try {
                        val s = String.format(
                            "[%s] {%s, Line %s, Source %s} %s\r\n",
                            Constant.DATE_FORMATER.format(Date()),
                            level.toString(),
                            message.lineNumber(),
                            message.sourceId(),
                            msg
                        );
                        printWriter.write(s);
                    } catch (e: Exception) {
                        Log.i("Console", "error", e);
                    } finally {
                        printWriter.close()
                    }
                    return false;
                }
            }
        }

        LaunchedEffect(navigator) {
            val bundle = state.viewState
            if (bundle == null) navigator.loadUrl(url)
        }

        WebView(
            state = state,
            modifier = Modifier.fillMaxSize(),
            navigator = navigator,
            onCreated = { webView ->
                webView.apply {
                    addJavascriptInterface(JsInterface(activity, saveDir, this), "jsinterface")
                    settings.apply {
                        javaScriptEnabled = true
                        javaScriptCanOpenWindowsAutomatically = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
                        allowContentAccess = true
                        defaultTextEncodingName = "utf-8"
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        databaseEnabled = true
                        loadsImagesAutomatically = true
                        setNeedInitialFocus(true)

                    }
                    scrollBarStyle = View.SCROLLBARS_OUTSIDE_INSET
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    activity.webScreenLifeCycleHook.setWebScreen(this)

                    setDownloadListener { url, _, contentDisposition, mimetype, _ ->
                        try {
                            val request = DownloadManager.Request(Uri.parse(url))
                            Log.i("mimetype", mimetype)
                            request.setMimeType(mimetype)
                            request.allowScanningByMediaScanner()
                            request.setNotificationVisibility(
                                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                            )
                            val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)
                            val dir =
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val file = File(dir, filename)
                            if (file.exists()) file.delete()
                            request.setDestinationUri(Uri.fromFile(file))
                            request.setTitle("正在下载$filename...")
                            request.setDescription("文件保存在" + file.absolutePath)
                            val downloadManager =
                                activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                            downloadManager!!.enqueue(request)
                            Toast.makeText(
                                context, "文件下载中，请在通知栏查看进度", Toast.LENGTH_LONG
                            ).show()
                        } catch (e: java.lang.Exception) {
                            Log.e("ERROR", "Error", e)
                            if (url.startsWith("blob")) {
                                Toast.makeText(context, "无法下载文件！", Toast.LENGTH_LONG).show()
                                return@setDownloadListener
                            }
                            activity.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW, Uri.parse(url)
                                )
                            )
                        }
                    }


                }
            },
            onDispose = { webView ->
                webView.destroy()
                onUrlLoaded?.invoke(null)
            },
            client = webClient,
            chromeClient = webChromeClient
        )
    }
}

fun maybeInjectLocalForage(view: WebView, saveDir: File) {
    view.evaluateJavascript(
        """
            (function() {
                if (!window.core || !window.core.plugin) return "";
                if (!window.localforage || !window.jsinterface) return ""
                if (core.utils._setLocalForage_set || !core.plugin._afterLoadResources) return "";
                return core.firstData.name;
            })()
        """.trimIndent()
    ) {
        val name = it.replace("\"", "")
        if (name.isEmpty()) return@evaluateJavascript
        val f = File(saveDir, name)
        if (!f.isDirectory || f.list { _, s -> s.startsWith("save") }
                .isNullOrEmpty()) return@evaluateJavascript
        Log.i("H5mota_WebActivity", "Inject localForage for $name...")
        // Inject
        view.evaluateJavascript(
            """
            (function () {              
              var _afterLoadResources = core.plugin._afterLoadResources;
              core.plugin._afterLoadResources = function () {
                console.log('Forwarding localforage...');
                core.platform.useLocalForage = true;
                Object.defineProperty(core.platform, 'useLocalForage', { writable: false });
                if (window.LZString) LZString.compress = function (s) { return s; };
                if (window.lzw_encode) lzw_encode = function (s) { return s; };
                localforage.setItem = function (name, data, callback) {
                  var id = setTimeout(null);
                  core['__callback' + id] = callback;
                  window.jsinterface.setLocalForage(id, name, data);
                }
                localforage.getItem = function (name, callback) {
                  var id = setTimeout(null);
                  core['__callback' + id] = callback;
                  window.jsinterface.getLocalForage(id, name);
                }
                localforage.removeItem = function (name, callback) {
                  var id = setTimeout(null);
                  core['__callback' + id] = callback;
                  window.jsinterface.removeLocalForage(id, name);
                }
                localforage.clear = function (callback) {
                  var id = setTimeout(null);
                  core['__callback' + id] = callback;
                  window.jsinterface.clearLocalForage(id);
                }
                localforage.iterate = function (iter, callback) {
                  var id = setTimeout(null);
                  core['__iter' + id] = iter;
                  core['__callback' + id] = callback;
                  window.jsinterface.iterateLocalForage(id);
                }
                localforage.keys = function (callback) {
                  var id = setTimeout(null);
                  core['__callback' + id] = callback;
                  window.jsinterface.keysLocalForage(id);
                }
                localforage.length = function (callback) {
                  var id = setTimeout(null);
                  core['__callback' + id] = callback;
                  window.jsinterface.lengthLocalForage(id);
                }
                core.control.getSaveIndexes(function (indexes) {
                  core.saves.ids = indexes;
                });
              }
              if (_afterLoadResources) _afterLoadResources.call(core.plugin);
            })()
                    """.trimIndent()
        ) { Log.i("WebScreen", "Forward finished!") }
    }
}

class JsInterface constructor(
    private val activity: MainActivity, private val saveDir: File, private val webView: WebView
) {
    init {
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
    }

    @JavascriptInterface
    public fun download(filename: String, content: String) {
        try {
            val directory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(directory, filename)
            FileWriter(file).apply {
                write(content)
                close()
            }
            val downloadPathMessage =
                activity.resources.getString(R.string.download_message_prefix) + file.absolutePath
            Toast.makeText(
                activity, downloadPathMessage, Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            Log.e("Error", "download error", e)
            Toast.makeText(
                activity,
                activity.resources.getString(R.string.download_failure),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @JavascriptInterface
    fun copy(content: String) {
        (activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
            ClipData.newPlainText("content", content)
        );
        Toast.makeText(
            activity,
            activity.resources.getString(R.string.copy_to_clipboard_success),
            Toast.LENGTH_SHORT
        ).show()
    }

    @JavascriptInterface
    fun readFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        activity.jsActivityLauncher.launch(intent) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@launch
            val uri = result.data?.data ?: return@launch
            Log.i("Path", uri.path ?: "")
            try {
                activity.contentResolver.openInputStream(uri).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        val builder = java.lang.StringBuilder()
                        while (reader.readLine().also { line = it } != null) builder.append(line)
                        webView.evaluateJavascript(
                            "core.readFileContent(" + replaceContent(builder.toString()) + ")", null
                        )
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(activity, "读取失败！", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun setLocalForage(id: Int, name: String, data: String) {
        try {
            FileWriter(getFile(name)).use { writer ->
                writer.write(data)
                executeLocalForageCallback(id)
            }
        } catch (e: IOException) {
            Log.e("ERROR", "Unable to setLocalForage", e)
            executeLocalForageCallback(id, (e.message), null)
        }
    }

    @JavascriptInterface
    fun getLocalForage(id: Int, name: String) {
        try {
            BufferedReader(InputStreamReader(FileInputStream(getFile(name)))).use { bufferedReader ->
                var line: String?
                val builder = java.lang.StringBuilder()
                while (bufferedReader.readLine().also { line = it } != null) builder.append(line)
                executeLocalForageCallback(id, null, replaceContent(builder.toString()))
            }
        } catch (e: IOException) {
            executeLocalForageCallback(id, null, null)
        }
    }

    @JavascriptInterface
    fun removeLocalForage(id: Int, name: String) {
        getFile(name).delete()
        executeLocalForageCallback(id)
    }

    @JavascriptInterface
    fun clearLocalForage(id: Int) {
        saveDir.deleteRecursively()
        saveDir.mkdir()
        executeLocalForageCallback(id)
    }

    @JavascriptInterface
    fun iterateLocalForage(id: Int) {
        executeLocalForageIterate(id, getAllSaves())
    }

    @JavascriptInterface
    fun keysLocalForage(id: Int) {
        val builder = java.lang.StringBuilder().append('[')
        var first = true
        for (name in getAllSaves()) {
            if (!first) builder.append(',')
            builder.append(replaceContent(name))
            first = false
        }
        builder.append(']')
        executeLocalForageCallback(id, null, builder.toString())
    }

    @JavascriptInterface
    fun lengthLocalForage(id: Int) {
        executeLocalForageCallback(id, null, getAllSaves().size.toString())
    }

    private fun getAllSaves(): List<String> {
        val files: Array<File> = saveDir.listFiles() ?: return ArrayList()
        val names: MutableList<String> = ArrayList()
        for (f in files) {
            if (f.isDirectory) {
                for (f2 in f.listFiles()!!) {
                    if (f2.isFile) {
                        names.add(f.name + "_" + f2.name)
                    }
                }
            } else {
                names.add(f.name)
            }
        }
        return names
    }


    private fun executeLocalForageCallback(id: Int) {
        activity.runOnUiThread {
            webView.evaluateJavascript(
                """if (window.core && window.core.__callback$id) {
  var callback = core.__callback$id;
  delete core.__callback$id;
  callback();
}
""", null
            )
        }
    }

    private fun getFile(name: String): File {
        val index = name.indexOf('_')
        if (index > 0 && index < name.length - 1) {
            val dir = File(saveDir, name.substring(0, index))
            if (!dir.exists()) {
                dir.mkdir()
            }
            return File(dir, name.substring(index + 1))
        }
        return File(saveDir, name)
    }

    private fun executeLocalForageCallback(id: Int, err: String?, data: String?) {
        activity.runOnUiThread {
            webView.evaluateJavascript(
                """if (window.core && window.core.__callback$id) {
  var callback = core.__callback$id;
  delete core.__callback$id;
  callback($err, $data);}
""", null
            )
        }
    }

    private fun executeLocalForageIterate(id: Int, keys: List<String>?) {
        val builder = StringBuilder()
        val iterName = "core.__iter$id"
        builder.append("if (window.core && window.").append(iterName).append(") {\n")
        if (keys != null) {
            for (key in keys) {
                builder.append("  ").append(iterName).append("(null, ").append(replaceContent(key))
                    .append(");\n")
            }
        }
        builder.append("  delete ").append(iterName).append(";")
        builder.append("}\n")
        activity.runOnUiThread {
            webView.evaluateJavascript(builder.toString()) { executeLocalForageCallback(id) }
        }
    }

    private fun replaceContent(content: String): String {
        return "'" + content.replace("'".toRegex(), "\"").replace("\n".toRegex(), "\\n") + "'"
    }
}


