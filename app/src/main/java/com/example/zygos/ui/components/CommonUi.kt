package com.example.zygos.ui.components

import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import androidx.compose.runtime.Immutable

fun <E> List<E>.normalized(selector: (E) -> Float): List<Float> {
    val total = this.sumOf { selector(it).toDouble() }
    return this.map { (selector(it) / total).toFloat() }
}

fun formatDollar(value: Float): String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    return format.format(value)
}


// NOTE: do not pre-multiply by 100!
fun formatPercent(value: Float): String {
    val format = NumberFormat.getPercentInstance()
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    return format.format(value)
}

/**
 * Use this for const lists. Otherwise an composable that accepts a List<T> will recompose with the
 * parent, even though the underlying list hasn't changed
 */
@Immutable
data class ImmutableList<T>(
    val items: List<T>,
)
