package com.example.zygos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zygos.components.PieChart
import com.example.zygos.ui.theme.ZygosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZygosApp()
        }
    }
}

@Composable
fun ZygosApp() {
    ZygosTheme {
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination
        var currentTab = zygosTabs.find { it.route == currentDestination?.route } ?: Performance

        Scaffold(
            bottomBar = {
                ZygosNav(
                    tabs = zygosTabs,
                    currentTab = currentTab.route,
                    onTabSelected = { tab ->
                        if (tab.route != currentTab.route) navController.navigateSingleTopTo(tab.route)
                    },
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Performance.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(route = Performance.route) {
                    PerformanceScreen(innerPadding)
                }
                composable(route = Holdings.route) {
                    HoldingsScreen(innerPadding)
                }
                composable(route = Chart.route) {
                    ChartScreen(innerPadding)
                }
            }
        }
    }
}




@Composable
fun PerformanceScreen(
    innerPadding: PaddingValues,
) {
    Surface(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Text("Performance Screen")
    }
}

@Composable
fun HoldingsScreen(
    innerPadding: PaddingValues,
) {
    Surface(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        PieChart(
            values = listOf(0.2f, 0.3f, 0.4f, 0.1f),
            colors = listOf(
                Color(0xFF004940),
                Color(0xFF005D57),
                Color(0xFF04B97F),
                Color(0xFF37EFBA)
            ),
            modifier = Modifier.size(100.dp),
        )
    }
}

@Composable
fun ChartScreen(
    innerPadding: PaddingValues,
) {
    Surface(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Text("Chart Screen")
    }
}

@Composable
fun ZygosNav(
    tabs: List<ZygosDestination>,
    currentTab: String,
    onTabSelected: (ZygosDestination) -> Unit,
) {
    BottomNavigation(
        modifier = Modifier.height(48.dp)
        //elevation = 0.dp,
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
            )
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) {
    backQueue.removeIf { it.destination.route == route }
    navigate(route) {
//        popUpTo(
//            this@navigateSingleTopTo.graph.findStartDestination().id
//        ) {
//            saveState = true
//        }
//        launchSingleTop = true
        restoreState = true
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ZygosApp()
}