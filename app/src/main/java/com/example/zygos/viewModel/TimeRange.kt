package com.example.zygos.viewModel

import com.example.zygos.ui.components.HasDisplayName

enum class TimeRange(override val displayName: String): HasDisplayName {
    FIVE_DAYS("5d"),
    ONE_MONTH("1m"),
    THREE_MONTHS("3m"),
    ONE_YEAR("1y"),
    FIVE_YEARS("5y"),
    TWENTY_YEARS("20y")
}