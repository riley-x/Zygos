package com.example.zygos.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CandlestickChart
import androidx.compose.material.icons.sharp.PieChart
import androidx.compose.material.icons.sharp.Receipt
import androidx.compose.material.icons.sharp.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface ZygosTab {
    val icon: ImageVector
    val route: String // route of tab's home screen
    val graph: String // route of tab's graph
}

// See https://fonts.google.com/icons?icon.category=Business%26Payments&icon.platform=web&icon.style=Sharp

object Performance : ZygosTab {
    override val icon = Icons.Sharp.ShowChart
    override val route = "performance"
    override val graph = "performance_graph"
}

object Holdings : ZygosTab {
    override val icon = Icons.Sharp.PieChart
    override val route = "holdings"
    override val graph = "holdings_graph"
}

object Chart : ZygosTab {
    override val icon = Icons.Sharp.CandlestickChart
    override val route = "chart"
    override val graph = "chart_graph"
}

object Transactions : ZygosTab {
    override val icon = Icons.Sharp.Receipt
    override val route = "transactions"
    override val graph = "transactions_graph"
}

object PositionDetails {
    const val route = "position"
    const val routeArgName = "ticker"
    const val routeWithArgs = "${route}/{${routeArgName}}"
    val arguments = listOf(
        navArgument(routeArgName) { type = NavType.StringType }
    )
}

val zygosTabs = listOf(Performance, Holdings, Chart, Transactions)