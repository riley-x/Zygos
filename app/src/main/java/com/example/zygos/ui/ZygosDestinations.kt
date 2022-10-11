package com.example.zygos.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface ZygosTab {
    val icon: ImageVector
    val route: String // route of tab's home screen
    val graph: String // route of tab's graph
}

// See https://fonts.google.com/icons?icon.category=Business%26Payments&icon.platform=web&icon.style=Sharp

object PerformanceTab : ZygosTab {
    override val icon = Icons.Sharp.ShowChart
    override val route = "performance"
    override val graph = "performance_graph"
}

object HoldingsTab : ZygosTab {
    override val icon = Icons.Sharp.PieChart
    override val route = "holdings"
    override val graph = "holdings_graph"
}

object ChartTab : ZygosTab {
    override val icon = Icons.Sharp.CandlestickChart
    override val route = "chart"
    override val graph = "chart_graph"
}

object AnalyticsTab : ZygosTab {
    override val icon = Icons.Sharp.BubbleChart
    override val route = "analytics"
    override val graph = "analytics_graph"
}

object PositionDetailsDestination {
    const val route = "position_details"
}

object ColorSelectorDestination {
    const val route = "color_selector"
    const val routeArgName = "tab"
    const val routeWithArgs = "$route/{$routeArgName}"
    val arguments = listOf(
        navArgument(routeArgName) { type = NavType.StringType }
    )
}

object TransactionAllDestination {
    const val route = "transaction_all"
}

object TransactionDetailsDestination {
    const val route = "transaction_details"
}


val zygosTabs = listOf(PerformanceTab, HoldingsTab, ChartTab, AnalyticsTab)