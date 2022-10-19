package com.example.zygos.ui.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier



@Composable
fun ValueOrPercent(
    value: Float,
    percent: Float,
    showPercentages: State<Boolean>,
    modifier: Modifier = Modifier,
    isColor: Boolean = true,
) {
    val color =
        if (!isColor) MaterialTheme.colors.onSurface
        else if ((showPercentages.value && percent >= 0) || (!showPercentages.value && value >= 0)) MaterialTheme.colors.primary
        else MaterialTheme.colors.error

    Text(
        text = if (showPercentages.value) formatPercent(percent) else formatDollar(value),
        color = color,
        modifier = modifier
    )
}