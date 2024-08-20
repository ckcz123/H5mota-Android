package com.h5mota.ui

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import android.util.Log
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.os.trace
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost

import com.h5mota.R
import com.h5mota.core.icon.MotaIcons
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.h5mota.MainActivity
import com.h5mota.core.base.Constant
import com.h5mota.feature.forum.forumNavigationRoute
import com.h5mota.feature.forum.forumScreen
import com.h5mota.feature.offline_game.offlineGameListNavigationRoute
import com.h5mota.feature.offline_game.offlineGameListScreen
import com.h5mota.feature.online_game.onlineGameNavigationRoute
import com.h5mota.feature.online_game.onlineGameScreen
import com.h5mota.feature.setting.settingNavigationRoute
import com.h5mota.feature.setting.settingScreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Stable
class MotaAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
    val context: Context
) {

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    /**
     * Map of top level destinations to be used in the TopBar, BottomBar and NavRail. The key is the
     * route.
     */
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.values().asList()

    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar

    /**
     * UI logic for navigating to a top level destination in the app. Top level destinations have
     * only one copy of the destination of the back stack, and save and restore state whenever you
     * navigate to and from it.
     *
     * @param topLevelDestination: The destination the app needs to navigate to.
     */
    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        trace("Navigation: ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }

                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                restoreState = true
            }

            when (topLevelDestination) {
                TopLevelDestination.ONLINE_GAME -> navController.navigateToScreen(
                    onlineGameNavigationRoute, topLevelNavOptions, context
                )

                TopLevelDestination.OFFLINE_GAME -> navController.navigateToScreen(
                    offlineGameListNavigationRoute, topLevelNavOptions, context
                )

                TopLevelDestination.FORUM -> navController.navigateToScreen(
                    forumNavigationRoute,
                    topLevelNavOptions,
                    context
                )

                TopLevelDestination.SETTING -> navController.navigateToScreen(
                    settingNavigationRoute,
                    topLevelNavOptions,
                    context
                )
            }
        }
    }
}

fun NavController.navigateToScreen(
    route: String,
    navOptions: NavOptions? = null,
    context: Context
) {
    val activity = getMainActivity(context as ContextWrapper)
    if (Constant.isPlayingGame(activity.webScreenLifeCycleHook.url)) {
        AlertDialog.Builder(activity).setTitle("请确认").setMessage("是否离开游戏页面")
            .setPositiveButton(
                "OK"
            ) { _, _ -> this.navigate(route, navOptions) }.setCancelable(true)
            .setNegativeButton("Cancel") {_, _ -> }.setCancelable(true)
                .show()
    } else {
        this.navigate(route, navOptions)
    }
}

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
) {
    ONLINE_GAME(
        selectedIcon = MotaIcons.VideogameAsset,
        unselectedIcon = MotaIcons.VideogameAsset,
        iconTextId = R.string.online_game,
        titleTextId = R.string.online_game,
    ),
    OFFLINE_GAME(
        selectedIcon = MotaIcons.VideogameAssetOff,
        unselectedIcon = MotaIcons.VideogameAssetOff,
        iconTextId = R.string.offline_game,
        titleTextId = R.string.offline_game,
    ),
    FORUM(
        selectedIcon = MotaIcons.Forum,
        unselectedIcon = MotaIcons.Forum,
        iconTextId = R.string.forum,
        titleTextId = R.string.forum,
    ),
    SETTING(
        selectedIcon = MotaIcons.Setting,
        unselectedIcon = MotaIcons.Setting,
        iconTextId = R.string.setting,
        titleTextId = R.string.setting,
    )
}

@Composable
fun rememberMotaAppState(
    windowSizeClass: WindowSizeClass,
    context: Context,
    navController: NavHostController = rememberNavController(),
): MotaAppState {
    return remember(
        windowSizeClass,
    ) {
        MotaAppState(
            navController,
            windowSizeClass,
            context
        )
    }
}

/**
 * Top-level navigation graph.
 *
 * The navigation graph defined in this file defines the different top level routes. Navigation
 * within each route is handled using state and Back Handlers.
 */
@Composable
fun MotaNavHost(
    domain: String,
    appState: MotaAppState,
    modifier: Modifier = Modifier,
    startDestination: String = onlineGameNavigationRoute,
    onUrlLoaded: ((String?) -> Unit)? = null,
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        onlineGameScreen(url = "${domain}/", onUrlLoaded = onUrlLoaded)
        offlineGameListScreen(onUrlLoaded = onUrlLoaded)
        forumScreen(url = "${domain}/bbs", onUrlLoaded = onUrlLoaded)
        settingScreen()
    }
}

fun getMainActivity(context: ContextWrapper): MainActivity {
    var ctx: Context = context
    while (ctx is ContextWrapper) {
        if (ctx is MainActivity) {
            return ctx
        }
        ctx = ctx.baseContext
    }
    throw IllegalStateException("Unable to find main activity!")
}
