package com.h5mota.feature.online_game

import androidx.compose.runtime.Composable
import com.h5mota.core.component.WebScreen

@Composable
internal fun OnlineGameRoute() {
    OnlineGameScreen()
}

@Composable
internal fun OnlineGameScreen() {
    WebScreen("https://h5mota.com/")
}