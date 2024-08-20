package com.h5mota.feature.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.h5mota.BuildConfig
import com.h5mota.core.base.Constant

@Composable
internal fun SettingCard(content: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 0.dp, 0.dp, 8.dp),
    ) {
        Text(
            text = content,
            modifier = Modifier
                .padding(16.dp),
            textAlign = TextAlign.Left,
        )
    }
}

@Composable
internal fun SettingRoute() {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SettingCard("Version: ${BuildConfig.VERSION_NAME}")
        SettingCard("Connect enhance: ${Constant.USE_PROXY}")
        SettingCard(Constant.LOG.joinToString("\n"))
    }
}
