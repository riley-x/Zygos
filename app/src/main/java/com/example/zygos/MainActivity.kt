package com.example.zygos

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.zygos.ui.holdings.holdingsListDisplayOptions
import com.example.zygos.ui.holdings.holdingsListOptionsSheet
import com.example.zygos.ui.holdings.holdingsListSortOptions
import com.example.zygos.ui.performance.PerformanceScreen
import com.example.zygos.ui.positionDetails.PositionDetailsScreen
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.ui.transactions.TransactionsScreen
import com.example.zygos.viewModel.ZygosViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZygosApp()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ZygosApp(
    preview: String = "",
    viewModel : ZygosViewModel = viewModel(),
) {
    ZygosTheme {
        /** Get the nav controller and current tab **/
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination
        val currentTab = zygosTabs.drop(1).find { tab ->
            currentDestination?.hierarchy?.any { it.route == tab.graph || it.route == tab.route } == true
        } ?: zygosTabs[0]

        /** ModalBottomSheetLayout state **/
        var holdingsListOptionsSheetIsClosing by remember{ mutableStateOf(false) }
        val holdingsListOptionsSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true,
            confirmStateChange = {
                if (it == ModalBottomSheetValue.Hidden) {
                    holdingsListOptionsSheetIsClosing = true

                    //Log.i("ZygosViewModel", viewModel.test)
                    // for some reason this causes the lambda to not ever run,
                    // and the bottom sheet can't be opened
                }
                true
            }
        )
        val scope = rememberCoroutineScope()

        /** Set the top and bottom bars **/
        Scaffold(
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
                    PerformanceScreen(
                        innerPadding = innerPadding,
                        accountBar = { AccountSelection(
                            accounts = listOf("Robinhood", "Test"),
                            currentAccount = "Robinhood",
                            modifier = Modifier.topBar(),
                        ) }
                    )
                }
                Holdings.route -> {
                    HoldingsScreen(
                        positions = viewModel.positions,
                        innerPadding = innerPadding,
                        accountBar = { AccountSelection(
                            accounts = listOf("Robinhood", "Test"),
                            currentAccount = "Robinhood",
                            modifier = Modifier.topBar(),
                        ) }
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
                                PerformanceScreen(
                                    innerPadding = innerPadding,
                                    accountBar = { AccountSelection(
                                        accounts = viewModel.accounts,
                                        currentAccount = viewModel.currentAccount,
                                        onAccountSelected = { viewModel.setAccount(it) },
                                        modifier = Modifier.topBar(),
                                    ) },
                                )
                            }
                        }

                        navigation(startDestination = Holdings.route, route = Holdings.graph) {
                            composable(route = Holdings.route) {
                                HoldingsScreen(
                                    positions = viewModel.positions,
                                    innerPadding = innerPadding,
                                    onPositionClick = {
                                        navController.navigateToPosition(it)
                                    },
                                    accountBar = { AccountSelection(
                                        accounts = viewModel.accounts,
                                        currentAccount = viewModel.currentAccount,
                                        onAccountSelected = { viewModel.setAccount(it) },
                                        modifier = Modifier.topBar(),
                                    ) },
                                    holdingsListOptionsCallback = {
                                        scope.launch { holdingsListOptionsSheetState.show() }
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

        /** Bottom Sheet **/
        ModalBottomSheetLayout(
            scrimColor = Color.Black.copy(alpha = 0.6f),
            sheetElevation = 0.dp,
            sheetState = holdingsListOptionsSheetState,
            sheetContent = holdingsListOptionsSheet(
                currentSortOption = viewModel.holdingsSortOption,
                currentDisplayOption = viewModel.holdingsDisplayOption,
                isSortedAscending = viewModel.holdingsSortIsAscending,
                onDisplayOptionSelected = { viewModel.holdingsDisplayOption = it },
                onSortOptionSelected = { viewModel.setHoldingsSortMethod(it) },
            )
        ) { }
        if (holdingsListOptionsSheetIsClosing) {
            holdingsListOptionsSheetIsClosing = false
            SideEffect {
                viewModel.sortHoldingsList()
            }
        }
    }
}


fun Modifier.topBar(): Modifier {
    return this.padding(horizontal = 8.dp, vertical = 6.dp)
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

fun NavHostController.navigateToPosition(position: Position) {
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