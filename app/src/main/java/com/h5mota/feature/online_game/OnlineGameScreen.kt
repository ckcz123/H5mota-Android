package com.h5mota.feature.online_game

import androidx.compose.runtime.Composable
import com.h5mota.core.component.WebScreen

@Composable
internal fun OnlineGameRoute(url: String, onUrlLoaded: ((String?) -> Unit)? = null) {
    OnlineGameScreen(url, onUrlLoaded)
}

@Composable
internal fun OnlineGameScreen(url: String, onUrlLoaded: ((String?) -> Unit)? = null) {
    WebScreen(url, onUrlLoaded)
}