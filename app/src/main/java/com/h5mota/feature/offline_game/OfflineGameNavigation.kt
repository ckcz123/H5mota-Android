package com.h5mota.feature.offline_game

import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.h5mota.R
import com.h5mota.core.component.WebScreen
import com.h5mota.ui.Constant
import com.h5mota.ui.getMainActivity
import java.net.URLEncoder


const val offlineGameListNavigationRoute = "offline_game_route"

fun NavGraphBuilder.offlineGameListScreen(onUrlLoaded: ((String?) -> Unit)? = null) {
    composable(route = offlineGameListNavigationRoute) {
        OfflineGameRoute(onUrlLoaded = onUrlLoaded)
    }
}


@Composable
fun GameNavHost(
    games: List<GameItem>,
    navController: NavHostController,
    onUrlLoaded: ((String?) -> Unit)? = null
) {
    NavHost(
        navController = navController,
        startDestination = "game_list",
        modifier = Modifier,
    ) {
        composable("game_list") {
            val activity = getMainActivity(LocalContext.current as ContextWrapper)
            var permissionGranted by remember {
                mutableStateOf(activity.checkStoragePermission())
            }

            if (!permissionGranted) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(onClick = {
                        activity.requestPermission {
                            permissionGranted = activity.checkStoragePermission()
                        }
                    }) {
                        Text(stringResource(id = R.string.grant_storage_permission))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(R.string.offline_game_directory),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    if (games.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(id = R.string.empty_game),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        games.forEach { game -> GameCard(game.title, game.uri, navController) }
                    }
                }
            }
        }
        games.forEach { game ->
            composable(game.uri) {
                WebScreen(
                    Constant.LOCAL + URLEncoder.encode(
                        game.uri, "utf-8"
                    ),
                    onUrlLoaded = onUrlLoaded,
                )
            }
        }
    }
}

fun NavController.navigateToGameScreen(uri: String, navOptions: NavOptions? = null) {
    this.navigate(uri, navOptions)
}

data class GameItem(val title: String, val uri: String)