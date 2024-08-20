package com.h5mota

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import com.h5mota.core.server.MotaWebServer
import com.h5mota.core.theme.MotaAndroidTheme
import com.h5mota.core.base.Constant.DIRECTORY
import com.h5mota.core.base.Constant.LOCAL_HOST
import com.h5mota.core.base.Constant.LOCAL_PORT
import com.h5mota.ui.MotaApp
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.io.File


class MainActivity : ComponentActivity() {
    lateinit var jsActivityLauncher: JsActivityLauncher
    val webScreenLifeCycleHook = WebScreenLifeCycleHook()
    var simpleWebServer: MotaWebServer? = null

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jsActivityLauncher = JsActivityLauncher(this)

        if (checkStoragePermission()) initStorage()

        setContent {
            MotaAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MotaApp(windowSizeClass = calculateWindowSizeClass(this))
                }
            }
        }
    }

    override fun onDestroy() {
        simpleWebServer?.stop()
        simpleWebServer = null
        super.onDestroy()
    }

    override fun onResume() {
        webScreenLifeCycleHook.onResume()
        super.onResume()
    }

    override fun onPause() {
        webScreenLifeCycleHook.onPause()
        super.onPause()
    }

    fun checkStoragePermission(): Boolean {
        return XXPermissions.isGranted(this, Permission.MANAGE_EXTERNAL_STORAGE)
    }

    fun checkNotificationPermission(): Boolean {
        return XXPermissions.isGranted(this, Permission.NOTIFICATION_SERVICE)
    }

    fun requestPermission(callback: () -> Unit) {
        XXPermissions
            .with(this)
            .permission(Permission.NOTIFICATION_SERVICE)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {
                override fun onGranted(
                    permissions: MutableList<String>,
                    allGranted: Boolean
                ) {
                    callback()
                    if (permissions.contains(Permission.MANAGE_EXTERNAL_STORAGE)) initStorage()
                }

                override fun onDenied(
                    permissions: MutableList<String>,
                    doNotAskAgain: Boolean
                ) {
                    callback()
                    noStorageAlert()
                }
            })
    }

    fun noStorageAlert() {
        Toast.makeText(
            this, "无存储卡读写权限，离线游戏暂时无法使用，请在系统设置中启用本软件的存储卡读写权限",
            Toast.LENGTH_LONG
        ).show()
    }

    fun initStorage() {
        if (!checkStoragePermission() || simpleWebServer != null) {
            return
        }

        if (!DIRECTORY.exists()) {
            DIRECTORY.mkdir()
            File(DIRECTORY, ".nomedia").createNewFile()
        } else if (!File(DIRECTORY, ".nomedia").exists()) {
            AlertDialog
                .Builder(this)
                .setTitle("提示")
                .setMessage("这个版本开始，离线塔目录下的图片不会被索引到图库中。\n\n" +
                        "对于已经被索引的图片，可以采用如下方法进行处理：\n" +
                        "1. 使用文件管理器打开手机存储，找到 H5mota 目录\n" +
                        "2. 将其重命名，例如 H5mota1\n" +
                        "3. 打开图库，确认已经被索引的图片消失\n" +
                        "4. 将目录重命名回 H5mota 即可\n" +
                        "如有问题可在论坛或群里进行反馈。")
                .setCancelable(true)
                .setPositiveButton("确定", null)
                .show()
            File(DIRECTORY, ".nomedia").createNewFile()
        }

        try {
            simpleWebServer = MotaWebServer(LOCAL_HOST, LOCAL_PORT, DIRECTORY, true)
            simpleWebServer!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
            simpleWebServer = null
        }

        Thread {
            maybeMigrateLegacyTowers()
        } .start()
    }

    fun maybeMigrateLegacyTowers() {
        val legacyTowerDir = getExternalFilesDir("towers")
        if (legacyTowerDir == null || !legacyTowerDir.exists()) return
        val towers = legacyTowerDir.listFiles()!!
            .filter { tower ->
                tower.isDirectory && File(tower, "index.html").exists()
                        && File(tower, "main.js").exists()
                        && File(tower, "libs").exists()
            }
        towers.forEach { tower ->
            val dest = File(DIRECTORY, tower.name)
            if (dest.exists() || tower.copyRecursively(dest)) {
                tower.deleteRecursively()
            }
        }
    }
}

class WebScreenLifeCycleHook {
    var view: WebView? = null
    private var bgmPaused = false

    val url get() = view?.url

    fun setWebScreen(webView: WebView) {
        view = webView
        bgmPaused = false
    }

    fun onPause() {
        view?.apply {
            pauseMusic()
            onPause()
            pauseTimers()
        }
    }

    fun onResume() {
        view?.apply {
            resumeMusic()
            onResume()
            resumeTimers()
        }
    }

    fun pauseMusic() {
        view?.evaluateJavascript(
            """
                (function() {
                    if (!window.main || !window.core || window.main.mode != 'play') {
                        return false;
                    }
                    if (!window.core.musicStatus.bgmStatus) {
                           return false;
                    }
                    window.core.musicStatus.bgmStatus = false;
                    window.core.pauseBgm();
                    return true;
                })()
            """.trimIndent()
        ) { value -> bgmPaused = value == "true" }
    }

    fun resumeMusic() {
        if (bgmPaused) {
            bgmPaused = false
            view?.evaluateJavascript(
                """
                    (function() {
                        if (!window.main || !window.core || window.main.mode != 'play') {
                            return;
                        }
                        if (window.core.musicStatus.bgmStatus) {
                            return;
                        }
                        window.core.musicStatus.bgmStatus = true;
                        window.core.resumeBgm();
                    })()
                """.trimIndent()
            ) {}
        }
    }
}

class JsActivityLauncher(activity: ComponentActivity) {
    private lateinit var callback: (result: ActivityResult) -> Unit
    private val activityResultLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> callback(result) }

    fun launch(intent: Intent, callback: (result: ActivityResult) -> Unit) {
        this.callback = callback
        activityResultLauncher.launch(intent)
    }
}