package com.example.zygos.ui.components

import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat

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