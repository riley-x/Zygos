package com.example.zygos.data

import android.icu.util.Calendar
import android.icu.util.TimeZone
import kotlin.math.roundToLong

fun getIntDate(year: Int, month: Int, day: Int): Int {
    return year * 10000 + month * 100 + day
}

fun Calendar.toIntDate() : Int {
    val year: Int = get(Calendar.YEAR)
    val month: Int = get(Calendar.MONTH) + 1 // months are 0-indexed????? https://stackoverflow.com/questions/344380/why-is-january-month-0-in-java-calendar
    val day: Int = get(Calendar.DAY_OF_MONTH)
    return getIntDate(year, month, day)
}

fun Calendar.setIntDate(date: Int) {
    set(getYear(date), getMonth(date) - 1, getDay(date)) // months are 0-indexed
}

fun Calendar.toNewYork(): Calendar {
    timeZone = TimeZone.getTimeZone("America/New_York")
    return this
}

fun Calendar.toMidnight(): Calendar {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}

fun getTimestamp(date: Int): Long {
    val cal = Calendar.getInstance()
    cal.setIntDate(date)
    return cal.timeInMillis
}

fun fromTimestamp(timestamp: Long): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    return cal.toIntDate()
}

fun getDay(date: Int) = date % 100
fun getMonth(date: Int) = (date / 100) % 100
fun getYear(date: Int) = (date / 10000)
fun getYearShort(date: Int) = (date / 10000) % 100


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
