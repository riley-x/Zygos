package com.example.zygos.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CandlestickChart
import androidx.compose.material.icons.sharp.PieChart
import androidx.compose.material.icons.sharp.Receipt
import androidx.compose.material.icons.sharp.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

interface ZygosDestination {
    val icon: ImageVector
    val route: String
}

// See https://fonts.google.com/icons?icon.category=Business%26Payments&icon.platform=web&icon.style=Sharp

object Performance : ZygosDestination {
    override val icon = Icons.Sharp.ShowChart
    override val route = "performance"
}

object Holdings : ZygosDestination {
    override val icon = Icons.Sharp.PieChart
    override val route = "holdings"
}

object Chart : ZygosDestination {
    override val icon = Icons.Sharp.CandlestickChart
    override val route = "chart"
}

object Transactions : ZygosDestination {
    override val icon = Icons.Sharp.Receipt
    override val route = "transactions"
}

val zygosTabs = listOf(Performance, Holdings, Chart, Transactions)