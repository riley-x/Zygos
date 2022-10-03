package com.example.zygos.data

import android.icu.util.Calendar
import kotlin.math.roundToInt

fun Calendar.toIntDate() : Int {
    val year: Int = get(Calendar.YEAR)
    val month: Int = get(Calendar.MONTH)
    val day: Int = get(Calendar.DAY_OF_MONTH)
    return year * 10000 + month * 100 + day
}

fun floatToIntDollar(x: Float) : Int {
    return (x * 10000f).roundToInt()
}

fun Float.toIntDollar(): Int {
    return floatToIntDollar(this)
}

fun floatFromIntDollar(x: Int) : Float {
    return (x / 10000f)
}

fun Int.toFloatDollar() : Float {
    return floatFromIntDollar(this)
}

