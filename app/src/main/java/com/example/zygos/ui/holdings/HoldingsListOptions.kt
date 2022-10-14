package com.example.zygos.ui.holdings

import com.example.zygos.ui.components.HasDisplayName
import com.example.zygos.ui.components.ImmutableList

enum class HoldingsListOptions(override val displayName: String) : HasDisplayName {
    TICKER("Ticker"),
    EQUITY("Equity"),
    RETURNS("Returns"),
    RETURNS_PERCENT("% Returns"),
    RETURNS_TODAY("Returns Today"),
    RETURNS_PERCENT_TODAY("% Returns Today")
}

val holdingsListSortOptions = ImmutableList(HoldingsListOptions.values().toList())

val holdingsListDisplayOptions = ImmutableList(listOf(
    HoldingsListOptions.EQUITY,
    HoldingsListOptions.RETURNS,
    HoldingsListOptions.RETURNS_PERCENT,
    HoldingsListOptions.RETURNS_TODAY,
    HoldingsListOptions.RETURNS_PERCENT_TODAY,
))