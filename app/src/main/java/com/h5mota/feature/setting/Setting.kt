package com.h5mota.feature.setting

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val settingNavigationRoute = "setting"

fun NavGraphBuilder.settingScreen() {
    composable(route = settingNavigationRoute) {
        SettingRoute()
    }
}