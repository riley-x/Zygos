package com.example.zygos.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.example.zygos.ui.components.ImmutableList

/**
 * This file contains lists containing the various option choices in UI elements
 */

val accountPerformanceRangeOptions = ImmutableList(listOf("1m", "3m", "1y", "5y", "All"))
val chartRangeOptions = ImmutableList(listOf("1m", "3m", "1y", "5y"))

val watchlistSortOptions = ImmutableList(listOf("Ticker", "% Change"))
val watchlistDisplayOptions = ImmutableList(listOf("Change", "% Change", "Price"))

val transactionSortOptions = ImmutableList(listOf("Date", "Ticker"))
