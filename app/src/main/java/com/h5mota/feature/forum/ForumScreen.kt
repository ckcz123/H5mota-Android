package com.h5mota.feature.forum

import androidx.compose.runtime.Composable
import com.h5mota.core.component.WebScreen
import com.h5mota.core.base.Constant


@Composable
internal fun ForumRoute(url: String, onUrlLoaded: ((String?) -> Unit)? = null) {
    WebScreen(url, onUrlLoaded = onUrlLoaded)
}
