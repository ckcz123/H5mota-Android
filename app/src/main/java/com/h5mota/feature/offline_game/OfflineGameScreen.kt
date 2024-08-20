package com.h5mota.feature.offline_game

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.h5mota.core.base.Constant
import org.json.JSONObject
import java.io.File


@Composable
internal fun OfflineGameRoute(
    navController: NavHostController = rememberNavController(),
    onUrlLoaded: ((String?) -> Unit)? = null
) {
    val games = mutableListOf<GameItem>()
    if (Constant.DIRECTORY.exists()) {
        for (file in Constant.DIRECTORY.listFiles()!!) {
            if (File(file, "index.html").exists()
                && File(file, "main.js").exists()
                && File(file, "libs").exists()
            ) {
                var title = file.name
                val data = File(file, "project/data.js")
                if (data.exists()) {
                    try {
                        val json = JSONObject(
                            data.readLines()
                                .drop(1)
                                .joinToString("\n")
                        )
                        val realTitle = json.getJSONObject("firstData").getString("title")
                        if (title != realTitle) {
                            title += " ($realTitle)"
                        }
                    } catch (_: Exception) {
                    }
                }

                games.add(GameItem(title, file.name))
            }
        }
    }
    GameNavHost(games, navController, onUrlLoaded = onUrlLoaded)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GameCard(title: String, uri: String, navController: NavHostController) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 0.dp, 0.dp, 8.dp),
        onClick = { navController.navigateToGameScreen(uri) }
    ) {
        Text(
            text = title,
            modifier = Modifier
                .padding(16.dp),
            textAlign = TextAlign.Center,
        )
    }
}
