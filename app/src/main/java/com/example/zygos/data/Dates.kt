package com.example.zygos.data

import android.icu.util.Calendar

fun Calendar.toIntDate() : Int {
    val year: Int = get(Calendar.YEAR)
    val month: Int = get(Calendar.MONTH)
    val day: Int = get(Calendar.DAY_OF_MONTH)
    return year * 10000 + month * 100 + day
}