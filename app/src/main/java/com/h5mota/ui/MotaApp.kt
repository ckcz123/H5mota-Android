package com.h5mota.ui;

import android.app.DownloadManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.h5mota.BuildConfig
import com.h5mota.MainActivity
import com.h5mota.core.base.Constant
import com.h5mota.core.component.MotaNavigationBar
import com.h5mota.core.component.MotaNavigationBarItem
import com.h5mota.core.component.MotaNavigationRail
import com.h5mota.core.component.MotaNavigationRailItem
import com.h5mota.core.base.Constant.APK_FILE
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import useHttpClientBuilder
import java.io.File


@Composable
fun MotaApp(
    windowSizeClass: WindowSizeClass,
    appState: MotaAppState = rememberMotaAppState(
        windowSizeClass = windowSizeClass,
        context = LocalContext.current
    ),
) {
    var domain by remember { mutableStateOf(Constant.DOMAIN) }
    var isPlayingGame by remember { mutableStateOf(false) }
    checkVersion()
    checkSiteBackup(onChange = { backUpDomain ->
        domain = backUpDomain
    })
    Scaffold(
        modifier = Modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            if (appState.shouldShowBottomBar && !isPlayingGame) {
                MotaNavBar(
                    destinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentDestination
                )
            }
        }
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (appState.shouldShowNavRail && !isPlayingGame) {
                MotaNavRail(
                    destinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentDestination
                )
            }

            Column(Modifier.fillMaxSize()) {
                MotaNavHost(domain = domain, appState = appState, onUrlLoaded = {
                    isPlayingGame = Constant.isPlayingGame(it)
                })
            }
        }
    }
}

private fun checkDomainAvailable(domain: String): Boolean {
    val result = runCatching {
        val okHttpClient = useHttpClientBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        okHttpClient
            .newCall(
                Request.Builder()
                    .url("${domain}/backend/backup/now-available")
                    .build()
            )
            .execute().use { response ->
                val s = response.body!!.string()
                s == "true"
            }
    }
    return result.getOrDefault(false)
}

private fun getBackupDomains(): List<String> {
    val result = runCatching {
        val okHttpClient = useHttpClientBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        okHttpClient
            .newCall(
                Request.Builder()
                    .url("${Constant.PRESS_DOMAIN}/proxy_backend/backup/domains")
                    .build()
            )
            .execute().use { response ->
                val s = response.body!!.string()
                val domainArray = JSONArray(s)
                (0 until domainArray.length()).map { i ->
                    val domainObject = domainArray.getJSONObject(i)
                    domainObject.getString("domain")
                }
            }
    }
    val domains = result.getOrDefault(listOf<String>())
    return domains
}

@Composable
private fun checkSiteBackup(onChange: (String) -> Unit) {
    var availableDomain by remember { mutableStateOf(Constant.DOMAIN) }
    var threadStarted by remember { mutableStateOf(false) }
    if (!threadStarted) {
        threadStarted = true
        Thread {
            if (checkDomainAvailable(Constant.DOMAIN)) {
                return@Thread;
            }
            Constant.changeProxyUsage(true)
            if (checkDomainAvailable((Constant.DOMAIN))) {
                return@Thread;
            }
            val domains = getBackupDomains()
            for (domain in domains) {
                if (checkDomainAvailable((domain))) {
                    availableDomain = domain
                }
                return@Thread;
            }
            availableDomain = Constant.PRESS_DOMAIN
        }.start()
    }
    if (availableDomain != Constant.DOMAIN) {
        val activity = getMainActivity(LocalContext.current as ContextWrapper)
        Toast.makeText(activity, "${Constant.DOMAIN}无法访问，切换至备份站点 $availableDomain", Toast.LENGTH_LONG).show()
        Constant.changeDomain(availableDomain)
        onChange(availableDomain)
    }
}

@Composable
private fun checkVersion() {
    var updatedInfo by remember { mutableStateOf(JSONObject()) }
    var dismissDialog by remember { mutableStateOf(false) }
    var threadStarted by remember { mutableStateOf(false) }
    if (!threadStarted) {
        threadStarted = true
        Thread {
            runCatching {
                val okHttpClient = OkHttpClient()
                    .newBuilder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
                okHttpClient
                    .newCall(
                        Request.Builder()
                            .url("${Constant.PRESS_DOMAIN}/proxy_backend/client/version.php?version=${BuildConfig.VERSION_NAME}")
                            .build()
                    )
                    .execute().use { response ->
                        val s = response.body!!.string()
                        val jsonObject = JSONObject(s)
                        val androidInfo = jsonObject.getJSONObject("android")
                        val versionCode = androidInfo.getInt("version_code")
                        if (versionCode > BuildConfig.VERSION_CODE) {
                            updatedInfo = androidInfo
                        }
                    }
            }
        }.start()
    }

    if (!dismissDialog && updatedInfo.has("url")) {
        val activity = getMainActivity(LocalContext.current as ContextWrapper)
        AlertDialog(
            onDismissRequest = {
                dismissDialog = true
            },
            confirmButton = {
                Button(onClick = {
                    downloadApk(activity, updatedInfo.getString("url"))
                    dismissDialog = true
                }, content = { Text("确定") })
            },
            title = { Text("存在新版本 ${updatedInfo.getString("version")}") },
            text = { Text(updatedInfo.getString("text")) },
        )
    }
}

private fun downloadApk(activity: MainActivity, url: String) {
    if (!activity.checkNotificationPermission()) {
        activity.requestPermission { downloadApk(activity, url) }
        return
    }
    try {
        val request = DownloadManager.Request(Uri.parse(url))
        val mimetype = "application/vnd.android.package-archive"
        Log.i("mimetype", mimetype)
        request.setMimeType(mimetype)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )
        val filename = APK_FILE
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(dir, filename)
        if (file.exists()) file.delete()
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setDestinationUri(Uri.fromFile(file))
        request.setTitle(filename)
        request.setDescription("文件保存在" + file.absolutePath)
        val downloadManager =
            activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        downloadManager!!.enqueue(request)
        Toast.makeText(activity, "文件下载中，请在通知栏查看进度", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Log.e("ERROR", "Error", e)
        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

@Composable
private fun MotaNavBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    MotaNavigationBar(
        modifier = modifier,
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            MotaNavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                selectedIcon = {
                    Icon(
                        imageVector = destination.selectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.iconTextId)) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun MotaNavRail(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    MotaNavigationRail(
        modifier = modifier,
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            MotaNavigationRailItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                selectedIcon = {
                    Icon(
                        imageVector = destination.selectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.iconTextId)) },
                modifier = modifier,
            )
        }
    }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false

