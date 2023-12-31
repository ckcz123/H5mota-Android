package com.h5mota.feature.forum

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val forumNavigationRoute = "forum_route"

fun NavGraphBuilder.forumScreen(onUrlLoaded: ((String?) -> Unit)? = null) {
    composable(route = forumNavigationRoute) {
        ForumRoute(onUrlLoaded = onUrlLoaded)
    }
}