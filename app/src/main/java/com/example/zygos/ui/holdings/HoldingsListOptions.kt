package com.example.zygos.ui.holdings

import com.example.zygos.ui.components.HasDisplayName
import com.example.zygos.ui.components.ImmutableList

enum class HoldingsListOptions(override val displayName: String) : HasDisplayName {
    TICKER("Ticker"),
    EQUITY("Equity"),
    RETURNS("Returns"),
    RETURNS_TODAY("Returns Today"),
    RETURNS_PERCENT("% Returns"),
    RETURNS_PERCENT_TODAY("% Change"),
    MARK("Mark"),
}

val holdingsListSortOptions = ImmutableList(listOf(
    HoldingsListOptions.TICKER,
    HoldingsListOptions.EQUITY,
    HoldingsListOptions.RETURNS,
    HoldingsListOptions.RETURNS_PERCENT,
    HoldingsListOptions.RETURNS_TODAY,
    HoldingsListOptions.RETURNS_PERCENT_TODAY,
))

val holdingsListDisplayOptions = ImmutableList(listOf(
    HoldingsListOptions.MARK,
    HoldingsListOptions.EQUITY,
    HoldingsListOptions.RETURNS,
    HoldingsListOptions.RETURNS_PERCENT,
    HoldingsListOptions.RETURNS_TODAY,
    HoldingsListOptions.RETURNS_PERCENT_TODAY,
))