package com.h5mota.feature.online_game

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val onlineGameNavigationRoute = "online_game_route"

fun NavGraphBuilder.onlineGameScreen(url: String, onUrlLoaded: ((String?) -> Unit)? = null) {
    composable(route = onlineGameNavigationRoute) {
        OnlineGameRoute(url, onUrlLoaded)
    }
}