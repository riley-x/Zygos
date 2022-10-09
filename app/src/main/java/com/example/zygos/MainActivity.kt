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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.zygos.data.database.Transaction
import com.example.zygos.data.database.TransactionType
import com.example.zygos.data.database.ZygosDatabase
import com.example.zygos.ui.*
import com.example.zygos.ui.analytics.AnalyticsScreen
import com.example.zygos.ui.chart.ChartScreen
import com.example.zygos.ui.components.*
import com.example.zygos.ui.holdings.HoldingsScreen
import com.example.zygos.viewModel.holdingsListDisplayOptions
import com.example.zygos.viewModel.holdingsListSortOptions
import com.example.zygos.ui.performance.PerformanceScreen
import com.example.zygos.ui.positionDetails.PositionDetailsScreen
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.ui.transactions.TransactionDetailsScreen
import com.example.zygos.ui.transactions.TransactionsListOptionsDialog
import com.example.zygos.ui.transactions.TransactionsScreen
import com.example.zygos.viewModel.*
import kotlinx.coroutines.launch


class ZygosApplication : Application() {
    val database: ZygosDatabase by lazy { ZygosDatabase.getDatabase(this) }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val viewModel: ZygosViewModel by viewModels {
            ZygosViewModelFactory(application as ZygosApplication)
        }

        setContent {
            ZygosApp(viewModel)
        }
    }
}



@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
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
        var openTransactionsListOptionsDialog by remember { mutableStateOf(false) }
        var openRecalculateAllLotsDialog by remember { mutableStateOf(false) }

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
        val bottomSheetContent = bottomSheetContent(listOptionsSheetVersion, viewModel)

        /** Callbacks
         * These need to be defined here (at viewModel/appScope scope) to enable smart
         * recomposition of any function that is passed these
         */
        fun popBackstack() {
            navController.popBackStack()
        }

        fun onHoldingsListOptionsShow(version: String) = appScope.launch {
            listOptionsSheetVersion = version
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

        /** Transaction Callbacks **/
        fun onTransactionsListOptionsShow() {
            openTransactionsListOptionsDialog = true
        }
        fun onTransactionsListOptionsDialogClose(isCancel: Boolean, ticker: String, type: TransactionType) {
            openTransactionsListOptionsDialog = false
            if (!isCancel) {
                viewModel.transactions.filterLaunch(ticker, type)
            }
        }
        fun toTransactionAll() = navController.navigate(TransactionAllDestination.route) {
            launchSingleTop = true
            restoreState = true
        }
        fun toTransactionDetails() {
            viewModel.transactions.clearFocus()
            navController.navigate(TransactionDetailsDestination.route) {
                launchSingleTop = true
                restoreState = true
            }
        }
        fun toTransactionDetails(t: Transaction) {
            viewModel.transactions.setFocus(t)
            navController.navigate(TransactionDetailsDestination.route) {
                launchSingleTop = true
                restoreState = true
            }
        }
        fun onRecalculateAllLotsClick() {
            openRecalculateAllLotsDialog = true
        }
        fun onRecalculateAllLotsClose(confirmed: Boolean) {
            openRecalculateAllLotsDialog = false
            if (confirmed) {
                viewModel.recalculateAllLots()
                popBackstack()
            }
        }


        fun onHoldingsPositionSelected(ticker: String) = navController.navigateToPosition(ticker)

        fun onTickerSelected(ticker: String) {
            viewModel.setTicker(ticker)
            navController.navigateSingleTopTo(ChartTab.route)
        }


        /** Set the top and bottom bars **/
        Scaffold(
            topBar = {
                AccountSelectionHeader(
                    accounts = viewModel.accounts,
                    currentAccount = viewModel.currentAccount,
                    onAccountSelected = viewModel::setAccount,
                    onAddAccount = ::onAddAccountClick,
                    modifier = Modifier.topBar(),
                )
            },
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
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
        ) { innerPadding ->
            /** Pick the tab to show **/
            NavHost(
                navController = navController,
                startDestination = PerformanceTab.graph,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.ime)
                    .padding(if (WindowInsets.Companion.isImeVisible) PaddingValues() else innerPadding)
            ) {
                navigation(startDestination = PerformanceTab.route, route = PerformanceTab.graph) {
                    composable(route = PerformanceTab.route) {
                        LogCompositions("Zygos", "ZygosApp/Scaffold/Performance.route")
                        PerformanceScreen(
                            accountPerformanceState = viewModel.accountPerformanceState,
                            accountPerformanceTimeRange = viewModel.accountPerformanceTimeRange,
                            watchlist = viewModel.watchlist,
                            watchlistDisplayOption = viewModel.watchlistDisplayOption,
                            onTickerSelected = ::onTickerSelected,
                            onWatchlistOptionsClick = ::onWatchlistOptionsShow,
                            onAccountPerformanceRangeSelected = viewModel::updateAccountPerformanceRange,
                        )
                    }
                }

                navigation(startDestination = HoldingsTab.route, route = HoldingsTab.graph) {
                    composable(route = HoldingsTab.route) {
                        LogCompositions("Zygos", "ZygosApp/Scaffold/Holdings.route")
                        HoldingsScreen(
                            longPositionsAreLoading = viewModel.longPositions.isLoading,
                            shortPositionsAreLoading = viewModel.shortPositions.isLoading,
                            tickerColors = viewModel.tickerColors,
                            longPositions = viewModel.longPositions.list,
                            shortPositions = viewModel.shortPositions.list,
                            displayOption = viewModel.longPositions.displayOption, // TODO short display
                            onPositionClick = ::onHoldingsPositionSelected,
                            holdingsListOptionsCallback = ::onHoldingsListOptionsShow,
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

                navigation(startDestination = ChartTab.route, route = ChartTab.graph) {
                    composable(route = ChartTab.route) {
                        LogCompositions("Zygos", "ZygosApp/Scaffold/Chart.route")
                        ChartScreen(
                            ticker = viewModel.chartTicker,
                            chartState = viewModel.chartState,
                            chartRange = viewModel.chartRange,
                            onChartRangeSelected = viewModel::setChartRange,
                            onTickerChanged = viewModel::setTicker,
                        )
                    }
                }

                navigation(startDestination = AnalyticsTab.route, route = AnalyticsTab.graph) {
                    composable(route = AnalyticsTab.route) {
                        LogCompositions("Zygos", "ZygosApp/Scaffold/Transactions.route")
                        AnalyticsScreen(
                            transactions = viewModel.transactions.latest,
                            tickerColors = viewModel.tickerColors,
                            onTransactionClick = ::toTransactionDetails,
                            onTransactionSeeAll = ::toTransactionAll,
                            onAddTransaction = ::toTransactionDetails,
                        )
                    }
                    composable(route = TransactionAllDestination.route) {
                        LogCompositions("Zygos", "ZygosApp/Scaffold/TransactionAllDestination.route")
                        TransactionsScreen(
                            transactions = viewModel.transactions.all,
                            currentFilterTicker = viewModel.transactions.currentFilterTicker,
                            currentFilterType = viewModel.transactions.currentFilterType,
                            onTransactionClick = ::toTransactionDetails,
                            transactionsListOptionsCallback = ::onTransactionsListOptionsShow,
                            onRecalculateAll = ::onRecalculateAllLotsClick,
                        )
                    }
                    composable(route = TransactionDetailsDestination.route) {
                        LogCompositions("Zygos", "ZygosApp/Scaffold/TransactionDetailsDestination.route")
                        TransactionDetailsScreen(
                            initialTransaction = viewModel.transactions.focused,
                            accounts = viewModel.accounts,
                            onSave = viewModel.transactions::add,
                            onCancel = ::popBackstack,
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
            sheetContent = bottomSheetContent,
            modifier = Modifier.recomposeHighlighter(),
        ) { }
        if (listOptionsSheetIsClosing) {
            // For some reason this can't be placed into the bottomSheet callback. Calling the viewModel
            // inside the callback causes the callback to never be executed??? And the bottom sheet will never open
            LaunchedEffect(true) { // this needs to be a launched effect because we're modifying state below
                viewModel.sortList(listOptionsSheetVersion)
                listOptionsSheetIsClosing = false
            }
        }
        if (openAddAccountDialog) {
            AddAccountDialog(
                onDismiss = ::onAddAccount
            )
        }
        if (openTransactionsListOptionsDialog) {
            TransactionsListOptionsDialog(
                currentSortOption = viewModel.transactions.sortOption,
                isSortedAscending = viewModel.transactions.sortIsAscending,
                sortOptions = transactionSortOptions,
                onSortOptionSelected = viewModel.transactions::setSortMethod,
                onDismiss = ::onTransactionsListOptionsDialogClose,
            )
        }
        if (openRecalculateAllLotsDialog) {
            ConfirmationDialog(
                text = "Recalculate all holdings from transactions?",
                onDismiss = ::onRecalculateAllLotsClose,
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

fun NavHostController.navigateToPosition(ticker: String) {
    this.navigate("${PositionDetails.route}/$ticker") {
        launchSingleTop = true
        restoreState = true
    }
}


fun bottomSheetContent(
    version: String,
    viewModel: ZygosViewModel,
): (@Composable ColumnScope.() -> Unit) {
    return when (version) {
        "long positions" -> listOptionsSheet(
            currentSortOption = viewModel.longPositions.sortOption,
            currentDisplayOption = viewModel.longPositions.displayOption,
            isSortedAscending = viewModel.longPositions.sortIsAscending,
            displayOptions = holdingsListDisplayOptions,
            sortOptions = holdingsListSortOptions,
            onDisplayOptionSelected = viewModel.longPositions::displayOption::set,
            onSortOptionSelected = viewModel.longPositions::setSortMethod,
        )
        "short positions" -> listOptionsSheet(
            currentSortOption = viewModel.shortPositions.sortOption,
            currentDisplayOption = viewModel.shortPositions.displayOption,
            isSortedAscending = viewModel.shortPositions.sortIsAscending,
            displayOptions = holdingsListDisplayOptions,
            sortOptions = holdingsListSortOptions,
            onDisplayOptionSelected = viewModel.shortPositions::displayOption::set,
            onSortOptionSelected = viewModel.shortPositions::setSortMethod,
        )
        else -> listOptionsSheet(
            currentSortOption = viewModel.watchlistSortOption,
            currentDisplayOption = viewModel.watchlistDisplayOption,
            isSortedAscending = viewModel.watchlistSortIsAscending,
            displayOptions = watchlistDisplayOptions,
            sortOptions = watchlistSortOptions,
            onDisplayOptionSelected = viewModel::watchlistDisplayOption::set,
            onSortOptionSelected = viewModel::setWatchlistSortMethod,
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewZygosApp() {
    ZygosApp()
}
