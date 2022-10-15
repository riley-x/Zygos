package com.example.zygos.data

import android.icu.util.Calendar
import kotlin.math.roundToInt
import kotlin.math.roundToLong

fun Calendar.toIntDate() : Int {
    val year: Int = get(Calendar.YEAR)
    val month: Int = get(Calendar.MONTH)
    val day: Int = get(Calendar.DAY_OF_MONTH)
    return year * 10000 + month * 100 + day
}

fun getDay(date: Int) = date % 100
fun getMonth(date: Int) = (date / 100) % 100
fun getYear(date: Int) = (date / 10000) % 100


fun floatToLongDollar(x: Float) : Long {
    return (x * 10000f).roundToLong()
}

fun Float.toLongDollar(): Long {
    return floatToLongDollar(this)
}

fun floatFromLongDollar(x: Long) : Float {
    return (x / 10000f)
}

fun Long.toFloatDollar() : Float {
    return floatFromLongDollar(this)
}
