package com.example.zygos.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

private const val DividerLengthInDegrees = 1.8f

/**
 * @param values: should be normalized already
 */
@Composable
fun PieChart(
    values: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    stroke: Dp = 10.dp,
) {
    val strokePx = with(LocalDensity.current) { Stroke(stroke.toPx()) }
    Canvas(modifier) {
        val innerRadius = (size.minDimension - strokePx.width) / 2
        val halfSize = size / 2.0f
        val topLeft = Offset(
            halfSize.width - innerRadius,
            halfSize.height - innerRadius
        )
        val size = Size(innerRadius * 2, innerRadius * 2)
        var startAngle = -90f
        val totalAngle = 360f
        values.forEachIndexed { index, value ->
            val sweep = value * totalAngle
            drawArc(
                color = colors[index],
                startAngle = startAngle + DividerLengthInDegrees / 2,
                sweepAngle = sweep - DividerLengthInDegrees,
                topLeft = topLeft,
                size = size,
                useCenter = false,
                style = strokePx
            )
            startAngle += sweep
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZygosTheme {
        Surface() {
            PieChart(
                values = listOf(0.2f, 0.3f, 0.4f, 0.1f),
                colors = listOf(
                    Color(0xFF004940),
                    Color(0xFF005D57),
                    Color(0xFF04B97F),
                    Color(0xFF37EFBA)
                ),
                modifier = Modifier.size(100.dp),
            )
        }
    }
}