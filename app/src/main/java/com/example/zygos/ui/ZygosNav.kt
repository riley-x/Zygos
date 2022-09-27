package com.example.zygos.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun ZygosNav(
    tabs: List<ZygosTab>,
    currentTab: String,
    modifier: Modifier = Modifier,
    onTabSelected: (ZygosTab) -> Unit = { },
) {
    BottomNavigation(
        modifier = modifier.height(48.dp),
        elevation = 6.dp,
    ) {
        tabs.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(
                    item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                    // For some reason height can't make the icon larger than some fixed value
                    // (maybe to do with the fixed BottomNavigationHeight = 56.dp), but size can.
                    // Or maybe the preview is just buggy.
                ) },
                selected = currentTab == item.route,
                onClick = { onTabSelected(item) },
                unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
            )
        }
    }
}


@Preview
@Composable
fun PreviewZygosNav() {
    ZygosTheme {
        ZygosNav(
            tabs = zygosTabs,
            currentTab = zygosTabs[0].route,
        )
    }
}