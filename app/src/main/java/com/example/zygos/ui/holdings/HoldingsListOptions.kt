package com.example.zygos.ui.holdings

import com.example.zygos.ui.components.HasDisplayName
import com.example.zygos.ui.components.ImmutableList

enum class HoldingsListSortOptions(override val displayName: String) : HasDisplayName {
    TICKER("Ticker"),
    EQUITY("Equity"),
    RETURNS("Returns"),
    RETURNS_PERCENT("% Returns"),
    RETURNS_TODAY("Returns Today"),
    RETURNS_PERCENT_TODAY("% Returns Today")
}

val holdingsListSortOptions = ImmutableList(HoldingsListSortOptions.values().toList())

val holdingsListDisplayOptions = ImmutableList(listOf(
    HoldingsListSortOptions.EQUITY,
    HoldingsListSortOptions.RETURNS,
    HoldingsListSortOptions.RETURNS_PERCENT,
    HoldingsListSortOptions.RETURNS_TODAY,
    HoldingsListSortOptions.RETURNS_PERCENT_TODAY,
))