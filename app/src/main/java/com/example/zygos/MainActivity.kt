package com.example.zygos

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.zygos.data.Position
import com.example.zygos.ui.*
import com.example.zygos.ui.chart.ChartScreen
import com.example.zygos.ui.components.AccountSelection
import com.example.zygos.ui.holdings.HoldingsScreen
import com.example.zygos.ui.performance.PerformanceScreen
import com.example.zygos.ui.positionDetails.PositionDetailsScreen
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.ui.transactions.TransactionsScreen
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
        val x = currentDestination?.hierarchy
        if (x != null) {
            for (s in x) Log.w("ZygosHeirarchy", "$s")
        }
        val currentTab = zygosTabs.drop(1).find { tab ->
            currentDestination?.hierarchy?.any { it.route == tab.graph || it.route == tab.route } == true
        } ?: zygosTabs[0]

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
                        if (tab.route != currentDestination?.route) {
                            if (tab.route == currentTab.route) { // Return to tab home, clear the tab's back stack
                                navController.navigateSingleTopTo(tab.route, shouldSaveState = false)
                            } else {
                                navController.navigateSingleTopTo(tab.graph)
                            }
                        }
                    },
                )
            },
        ) { innerPadding ->
            /** Pick the tab to show **/
            when (preview) {
                /** Previews **/
                Performance.route -> {
                    PerformanceScreen(innerPadding)
                }
                Holdings.route -> {
                    HoldingsScreen(
                        positions = viewModel.positions,
                        innerPadding = innerPadding
                    )
                }
                /** Actual run code, with Navigation Host **/
                else -> {
                    NavHost(
                        navController = navController,
                        startDestination = Performance.graph,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        navigation(startDestination = Performance.route, route = Performance.graph) {
                            composable(route = Performance.route) {
                                PerformanceScreen(innerPadding)
                            }
                        }

                        navigation(startDestination = Holdings.route, route = Holdings.graph) {
                            composable(route = Holdings.route) {
                                HoldingsScreen(
                                    positions = viewModel.positions,
                                    innerPadding = innerPadding,
                                    onPositionClick = {
                                        navController.navigateToPosition(it)
                                    }
                                )
                            }
                            composable(
                                route = PositionDetails.routeWithArgs,
                                arguments = PositionDetails.arguments,
                            ) { navBackStackEntry ->
                                val ticker = navBackStackEntry.arguments?.getString(PositionDetails.routeArgName)
                                PositionDetailsScreen(innerPadding)
                            }
                        }

                        navigation(startDestination = Chart.route, route = Chart.graph) {
                            composable(route = Chart.route) {
                                ChartScreen(innerPadding)
                            }
                        }

                        navigation(startDestination = Transactions.route, route = Transactions.graph) {
                            composable(route = Transactions.route) {
                                TransactionsScreen(innerPadding)
                            }
                        }

                    }
                }
            }
        }
    }
}


@Composable
fun ZygosNav(
    tabs: List<ZygosTab>,
    currentTab: String,
    onTabSelected: (ZygosTab) -> Unit,
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

// Not sure if this is causing bugs when switching screens too fast
// Maybe replace with singleTopTo below?
fun NavHostController.navigateSingleScreenTo(route: String) {
    backQueue.removeIf { it.destination.route == route }
    navigate(route) {
        restoreState = true
    }
}

fun NavHostController.navigateSingleTopTo(route: String, shouldSaveState: Boolean = true) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = shouldSaveState
        }
        launchSingleTop = true
        restoreState = true
    }

private fun NavHostController.navigateToPosition(position: Position) {
    this.navigate("${PositionDetails.route}/${position.ticker}") {
        launchSingleTop = true
        restoreState = true
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewTabPerformance() {
    ZygosApp(
        preview = Performance.route
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewTabHoldings() {
    ZygosApp(
        preview = Holdings.route
    )
}