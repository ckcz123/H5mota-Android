package com.h5mota.feature.forum

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val forumNavigationRoute = "forum_route"

fun NavGraphBuilder.forumScreen() {
    composable(route = forumNavigationRoute) {
        ForumRoute()
    }
}