package com.example.zygos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme
import java.lang.Math.toDegrees

private const val DividerLengthInDegrees = 1.8f

/**
 * @param values: should be normalized already
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PieChart(
    tickers: List<String>,
    values: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    stroke: Dp = 10.dp,
) {
    if (tickers.isEmpty()) return

    val total = values.sum()
    val angles = FloatArray(values.size + 1)
    for (index in 0..values.size) {
        angles[index + 1] = angles[index] + 360f * values[index] / total
    }

    var centerText by remember { mutableStateOf(tickers[0]) }
    var boxSize by remember { mutableStateOf(IntSize(10, 10)) } // 960 x 1644

    Box(
        modifier = modifier
            .pointerInteropFilter { motionEvent ->
                val x = motionEvent.x - boxSize.width / 2
                val y = motionEvent.y - boxSize.height / 2

                // Here we map y -> (Right = +x) and x -> (Up = -y)
                // since we start at the top and move clockwise
                var phi = toDegrees(kotlin.math.atan2(x, -y).toDouble())
                if (phi < 0) phi += 360

                val index = angles.indexOfFirst { it > phi }
                centerText = tickers[index - 1]
                true
            }
            .onGloballyPositioned { boxSize = it.size },
        contentAlignment = Alignment.Center,
    ) {
        val strokePx = with(LocalDensity.current) { Stroke(stroke.toPx()) }
        Canvas(Modifier.fillMaxSize()) {
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
        Text(centerText)
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZygosTheme {
        Surface() {
            PieChart(
                tickers = listOf("A", "B", "C", "D"),
                values = listOf(0.2f, 0.3f, 0.4f, 0.1f),
                colors = listOf(
                    Color(0xFF004940),
                    Color(0xFF005D57),
                    Color(0xFF04B97F),
                    Color(0xFF37EFBA)
                ),
                modifier = Modifier.size(300.dp),
            )
        }
    }
}