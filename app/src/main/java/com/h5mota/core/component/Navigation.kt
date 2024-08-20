package com.h5mota.core.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.h5mota.R
import com.h5mota.core.icon.MotaIcons
import com.h5mota.core.theme.MotaAndroidTheme

@Composable
fun RowScope.MotaNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    selectedIcon: @Composable () -> Unit = icon,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MotaNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = MotaNavigationDefaults.navigationContentColor(),
            selectedTextColor = MotaNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = MotaNavigationDefaults.navigationContentColor(),
            indicatorColor = MotaNavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

@Composable
fun MotaNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        contentColor = MotaNavigationDefaults.navigationContentColor(),
        tonalElevation = 0.dp,
        content = content,
    )
}

@Composable
fun MotaNavigationRail(
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationRail(
        modifier = modifier,
        contentColor = MotaNavigationDefaults.navigationContentColor(),
        header = header,
        content = content,
    )
}

@Composable
fun MotaNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    selectedIcon: @Composable () -> Unit = icon,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MotaNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = MotaNavigationDefaults.navigationContentColor(),
            selectedTextColor = MotaNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = MotaNavigationDefaults.navigationContentColor(),
            indicatorColor = MotaNavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

@Preview
@Composable
fun MotaNavigationPreview() {
    val items = listOf(
        stringResource(R.string.online_game),
        stringResource(R.string.offline_game),
        stringResource(R.string.forum),
        stringResource(R.string.setting),
    )
    val icons = listOf(
        MotaIcons.VideogameAsset,
        MotaIcons.VideogameAssetOff,
        MotaIcons.Forum,
        MotaIcons.Setting,
    )
    val selectedIcons = listOf(
        MotaIcons.VideogameAsset,
        MotaIcons.VideogameAssetOff,
        MotaIcons.Forum,
        MotaIcons.Setting,
    )

    MotaAndroidTheme{
        MotaNavigationRail {
            items.forEachIndexed { index, item ->
                MotaNavigationRailItem(
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = selectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                    selected = index == 0,
                    onClick = { },
                )
            }
        }
    }
}

object MotaNavigationDefaults {
    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer

    @Composable
    fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}