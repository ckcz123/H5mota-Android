package com.h5mota.feature.online_game

import androidx.compose.runtime.Composable
import com.h5mota.core.component.WebScreen

@Composable
internal fun OnlineGameRoute(onUrlLoaded: ((String?) -> Unit)? = null) {
    OnlineGameScreen(onUrlLoaded)
}

@Composable
internal fun OnlineGameScreen(onUrlLoaded: ((String?) -> Unit)? = null) {
    WebScreen("https://h5mota.com/", onUrlLoaded)
}