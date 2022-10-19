package com.example.zygos.ui.components

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.text.NumberFormat
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.example.zygos.data.getDay
import com.example.zygos.data.getMonth
import com.example.zygos.data.getYear
import com.example.zygos.data.getYearShort
import java.sql.Timestamp

fun <E> List<E>.normalized(selector: (E) -> Float): List<Float> {
    val total = this.sumOf { selector(it).toDouble() }
    return this.map { (selector(it) / total).toFloat() }
}

@Stable
fun formatDollar(value: Float): String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    return format.format(value)
}

@Stable
fun formatDollarNoSymbol(value: Float, length: Int = 0): String {
    val format = NumberFormat.getNumberInstance()
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    val out = format.format(value)
    return out.padStart(length)
}

// NOTE: do not pre-multiply by 100!
@Stable
fun formatPercent(value: Float): String {
    val format = NumberFormat.getPercentInstance()
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    return format.format(value)
}

@Stable
fun formatDateInt(date: Int): String {
    if (date == 0) return ""
    val day = getDay(date)
    val month = getMonth(date)
    val year = getYearShort(date).toString().padStart(2, '0')
    return "$month/$day/$year"
}

@Stable
fun formatDateNoYear(timestamp: Long): String {
    return DateFormat.format("M/d", timestamp).toString()
}
@Stable
fun formatDate(timestamp: Long): String {
    return DateFormat.format("M/d/yy", timestamp).toString()
}
@Stable
fun formatDateNoDay(timestamp: Long): String {
    return DateFormat.format("MMM yy", timestamp).toString()
}
@Stable
fun formatTimeDayOfWeek(timestamp: Long): String {
    return DateFormat.format("E h:mm", timestamp).toString()
}


@Composable
fun TitleValue(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(title, color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium))
        Text(value)
    }
}


/**
 * Use this for const lists. Otherwise an composable that accepts a List<T> will recompose with the
 * parent, even though the underlying list hasn't changed
 */
@Immutable
data class ImmutableList<T>(
    val items: List<T>,
)
