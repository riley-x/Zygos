package com.example.zygos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zygos.ui.*
import com.example.zygos.ui.chart.ChartScreen
import com.example.zygos.ui.components.AccountSelection
import com.example.zygos.ui.components.PieChart
import com.example.zygos.ui.holdings.HoldingsScreen
import com.example.zygos.ui.performance.PerformanceScreen
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.ZygosViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZygosApp()
        }
    }
}

@Composable
fun ZygosApp(
    preview: String = "",
    viewModel : ZygosViewModel = viewModel(),
) {
    ZygosTheme {
        /** Get the nav controller **/
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination
        val currentTab = zygosTabs.find { it.route == currentDestination?.route } ?: Performance

        /** Set the top and bottom bars **/
        Scaffold(
            topBar = { AccountSelection(
                accounts = viewModel.accounts,
                currentAccount = viewModel.currentAccount,
                onAccountSelected = { viewModel.setAccount(it) },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) },
            bottomBar = {
                ZygosNav(
                    tabs = zygosTabs,
                    currentTab = currentTab.route,
                    onTabSelected = { tab ->
                        if (tab.route != currentTab.route) navController.navigateSingleScreenTo(tab.route)
                    },
                )
            },
        ) { innerPadding ->
            /** Pick the tab to show **/
            if (preview == Performance.route) {
                PerformanceScreen(innerPadding)
            } else {
                NavHost(
                    navController = navController,
                    startDestination = Performance.route,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    composable(route = Performance.route) {
                        PerformanceScreen(innerPadding)
                    }
                    composable(route = Holdings.route) {
                        HoldingsScreen(
                            positions = viewModel.positions,
                            innerPadding = innerPadding
                        )
                    }
                    composable(route = Chart.route) {
                        ChartScreen(innerPadding)
                    }
                }
            }
        }
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

fun NavHostController.navigateSingleScreenTo(route: String) {
    backQueue.removeIf { it.destination.route == route }
    navigate(route) {
        restoreState = true
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ZygosApp(
        preview = Performance.route
    )
}