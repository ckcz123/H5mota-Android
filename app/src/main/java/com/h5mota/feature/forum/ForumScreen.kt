package com.h5mota.feature.forum

import androidx.compose.runtime.Composable
import com.h5mota.core.component.WebScreen


@Composable
internal fun ForumRoute(onUrlLoaded: ((String?) -> Unit)? = null) {
    WebScreen(url = "https://h5mota.com/bbs", onUrlLoaded = onUrlLoaded)
}
