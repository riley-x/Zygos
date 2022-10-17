package com.example.zygos.ui.holdings

import com.example.zygos.ui.components.HasDisplayName
import com.example.zygos.ui.components.ImmutableList

enum class HoldingsListSortOptions(override val displayName: String) : HasDisplayName {
    TICKER("Ticker"),
    EQUITY("Equity"),
    RETURNS("Returns"),
    RETURNS_TODAY("Returns Today"),
    RETURNS_PERCENT("% Returns"),
    RETURNS_PERCENT_TODAY("% Change"),
}

enum class HoldingsListDisplayOptions(override val displayName: String) : HasDisplayName {
    EQUITY("Equity/Mark"),
    RETURNS_TOTAL("Returns Total"),
    RETURNS_TODAY("Returns Today"),
}

val holdingsListSortOptions = ImmutableList(HoldingsListSortOptions.values().toList())
val holdingsListDisplayOptions = ImmutableList(HoldingsListDisplayOptions.values().toList())