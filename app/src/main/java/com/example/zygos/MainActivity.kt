package com.example.zygos

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.zygos.data.LotDatabase
import com.example.zygos.ui.*
import com.example.zygos.ui.chart.ChartScreen
import com.example.zygos.ui.components.*
import com.example.zygos.ui.holdings.HoldingsScreen
import com.example.zygos.ui.holdings.holdingsListDisplayOptions
import com.example.zygos.ui.holdings.holdingsListSortOptions
import com.example.zygos.ui.performance.PerformanceScreen
import com.example.zygos.ui.positionDetails.PositionDetailsScreen
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.ui.transactions.TransactionsScreen
import com.example.zygos.viewModel.*
import kotlinx.coroutines.launch


class ZygosApplication : Application() {
    val lotDatabase: LotDatabase by lazy { LotDatabase.getDatabase(this) }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: ZygosViewModel by viewModels {
            ZygosViewModelFactory(application as ZygosApplication)
        }

        setContent {
            ZygosApp(viewModel)
        }
    }
}



@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ZygosApp(
    viewModel: ZygosViewModel = viewModel(),
) {
    ZygosTheme {
        LogCompositions("Zygos", "ZygosApp")

//        val context = LocalContext.current.filesDir
        LaunchedEffect(Unit) {
            viewModel.startup()
        }

        /** Get the coroutine scope for the entire app **/
        val appScope = rememberCoroutineScope()

        /** Get the nav controller and current tab **/
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination
        val currentTab = zygosTabs.drop(1).find { tab ->
            currentDestination?.hierarchy?.any { it.route == tab.graph || it.route == tab.route } == true
        } ?: zygosTabs[0]

        /** Dialog state **/
        var openAddAccountDialog by remember { mutableStateOf(false) }

        /** ModalBottomSheetLayout state **/
        var listOptionsSheetVersion by remember { mutableStateOf("") }
        var listOptionsSheetIsClosing by remember { mutableStateOf(false) }
        val listOptionsSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true,
            confirmStateChange = {
                //Log.i("ZygosViewModel", viewModel.holdingsSortOption)
                // for some reason this causes the lambda to not ever run,
                // and the bottom sheet can't be opened

                if (it == ModalBottomSheetValue.Hidden) {
                    listOptionsSheetIsClosing = true
                    Log.i("ZygosViewModel", "$listOptionsSheetIsClosing")
                }
                true
            }
        )

        /** Callbacks
         * These need to be defined here (at viewModel/appScope scope) to enable smart
         * recomposition of any function that is passed these
         */
        fun onHoldingsListOptionsShow() = appScope.launch {
            listOptionsSheetVersion = "holdings"
            listOptionsSheetState.show()
        }
        fun onWatchlistOptionsShow() = appScope.launch {
            listOptionsSheetVersion = "watchlist"
            listOptionsSheetState.show()
        }
        fun onAddAccountClick() {
            openAddAccountDialog = true
        }
        fun onAddAccount(account: String) {
            openAddAccountDialog = false
            if (account.isNotBlank()) {
                viewModel.addAccount(account)
            }
        }
        fun onHoldingsPositionSelected(pos: Position) = navController.navigateToPosition(pos)
        fun onTickerSelected(ticker: String) {
            viewModel.setTicker(ticker)
            navController.navigateSingleTopTo(Chart.route)
        }
        fun myCallback() = viewModel.setAccount(viewModel.accounts.random())

        /** Set the top and bottom bars **/
        Scaffold(
            bottomBar = {
                ZygosNav(
                    tabs = zygosTabs,
                    currentTab = currentTab.route,
                    onTabSelected = { tab ->
                        if (tab.route != currentDestination?.route) {
                            if (tab.route == currentTab.route) { // Return to tab home, clear the tab's back stack
                                navController.navigateSingleTopTo(
                                    tab.route,
                                    shouldSaveState = false
                                )
                            } else {
                                navController.navigateSingleTopTo(tab.graph)
                            }
                        }
                    },
                )
            },
        ) { innerPadding ->
            /** Pick the tab to show **/
            NavHost(
                navController = navController,
                startDestination = Performance.graph,
                modifier = Modifier.padding(innerPadding),
            ) {
                navigation(startDestination = Performance.route, route = Performance.graph) {
                    composable(route = Performance.route) {
                        LogCompositions("Zygos", "ZygosApp/Scaffold/Performance.route")
                        PerformanceScreen(
                            accountStartingValue = viewModel.accountStartingValue,
                            accountPerformance = viewModel.accountPerformance,
                            accountPerformanceTicksX = viewModel.accountPerformanceTicksX,
                            accountPerformanceTicksY = viewModel.accountPerformanceTicksY,
                            accountPerformanceRange = viewModel.accountPerformanceRange,
                            watchlist = viewModel.watchlist,
                            watchlistDisplayOption = viewModel.watchlistDisplayOption,
                            onTickerSelected = ::onTickerSelected,
                            onWatchlistOptionsClick = ::onWatchlistOptionsShow,
                            onAccountPerformanceRangeSelected = viewModel::setPerformanceRange,
                            accountBar = {
                                AccountSelection(
                                    accounts = viewModel.accounts,
                                    currentAccount = viewModel.currentAccount,
                                    onAccountSelected = viewModel::setAccount,
                                    onAddAccount = ::onAddAccountClick,
                                    modifier = Modifier.topBar(),
                                )
                            },
                        )
                    }
                }

                navigation(startDestination = Holdings.route, route = Holdings.graph) {
                    composable(route = Holdings.route) {
                        LogCompositions("Zygos", "ZygosApp/Scaffold/Holdings.route")
                        HoldingsScreen(
                            positions = viewModel.positions,
                            displayOption = viewModel.holdingsDisplayOption,
                            onPositionClick = ::onHoldingsPositionSelected,
                            holdingsListOptionsCallback = ::onHoldingsListOptionsShow,
                            accountBar = {
                                AccountSelection(
                                    accounts = viewModel.accounts,
                                    currentAccount = viewModel.currentAccount,
                                    onAccountSelected = viewModel::setAccount,
                                    onAddAccount = ::onAddAccountClick,
                                    modifier = Modifier.topBar(),
                                )
                            },
                        )
                    }
                    composable(
                        route = PositionDetails.routeWithArgs,
                        arguments = PositionDetails.arguments,
                    ) { navBackStackEntry ->
                        LogCompositions("Zygos", "ZygosApp/Scaffold/PositionDetails.route")
                        val ticker =
                            navBackStackEntry.arguments?.getString(PositionDetails.routeArgName)
                        PositionDetailsScreen()
                    }
                }

                navigation(startDestination = Chart.route, route = Chart.graph) {
                    composable(route = Chart.route) {
                        LogCompositions("Zygos", "ZygosApp/Scaffold/Chart.route")
                        ChartScreen(
                            ticker = viewModel.chartTicker,
                            data = viewModel.chartData,
                            ticksY = viewModel.chartTicksY,
                            ticksX = viewModel.chartTicksX,
                            chartRange = viewModel.chartRange,
                            onChartRangeSelected = viewModel::setChartRange,
                            onTickerChanged = viewModel::setTicker,
                        )
                    }
                }

                navigation(startDestination = Transactions.route, route = Transactions.graph) {
                    composable(route = Transactions.route) {
                        LogCompositions("Zygos", "ZygosApp/Scaffold/Transactions.route")
                        TransactionsScreen(
                            testState = viewModel.currentAccount,
                            onClick = ::myCallback,
                        )
                    }
                }

            }
        }

        /** Bottom Sheet **/
        // TODO changing a selection inside the bottom sheet causes this top level app to recompose
        // Was happening in old version before hoisting too though.
        ModalBottomSheetLayout(
            scrimColor = Color.Black.copy(alpha = 0.6f),
            sheetElevation = 0.dp,
            sheetState = listOptionsSheetState,
            sheetContent = bottomSheetContent(listOptionsSheetVersion, viewModel),
            modifier = Modifier.recomposeHighlighter(),
        ) { }
        if (listOptionsSheetIsClosing) {
            // You could just call the sort function here, but it'll block the thread? Also maybe
            // recomposition would kill it? In which case you could use SideEffect or DisposableEffect's
            // onDispose, but they both still block the UI thread when adding a Thread.sleep() call.
            // Also still not sure why you can't access viewModel from the bottomSheet callback...
            LaunchedEffect(true) {
                //delay(3000) // this happens asynchronously! Make sure that all other state is ok with the positions list being modified
                viewModel.sortList(listOptionsSheetVersion)
                listOptionsSheetIsClosing = false
            }
        }
        if (openAddAccountDialog) {
            AddAccountDialog(
                onDismiss = ::onAddAccount
            )
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


fun bottomSheetContent(
    version: String,
    viewModel: ZygosViewModel,
): (@Composable ColumnScope.() -> Unit) {
    return when (version) {
        "holdings" -> listOptionsSheet(
            currentSortOption = viewModel.holdingsSortOption,
            currentDisplayOption = viewModel.holdingsDisplayOption,
            isSortedAscending = viewModel.holdingsSortIsAscending,
            displayOptions = holdingsListDisplayOptions,
            sortOptions = holdingsListSortOptions,
            onDisplayOptionSelected = { viewModel.holdingsDisplayOption = it },
            onSortOptionSelected = { viewModel.setHoldingsSortMethod(it) },
        )
        else -> listOptionsSheet(
            currentSortOption = viewModel.watchlistSortOption,
            currentDisplayOption = viewModel.watchlistDisplayOption,
            isSortedAscending = viewModel.watchlistSortIsAscending,
            displayOptions = watchlistDisplayOptions,
            sortOptions = watchlistSortOptions,
            onDisplayOptionSelected = { viewModel.watchlistDisplayOption = it },
            onSortOptionSelected = { viewModel.setWatchlistSortMethod(it) },
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewZygosApp() {
    ZygosApp()
}
