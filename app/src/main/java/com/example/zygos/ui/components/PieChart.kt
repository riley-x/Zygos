package com.example.zygos.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
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
    stroke: Dp = 30.dp,
) {
    if (tickers.isEmpty()) return

    val total = values.sum()
    val angles = FloatArray(values.size + 1) // first entry is 0, last entry is 360
    for (index in values.indices) {
        angles[index + 1] = angles[index] + 360f * values[index] / total
    }

    var focusIndex by remember { mutableStateOf(-1) }
    var boxSize by remember { mutableStateOf(IntSize(10, 10)) } // 960 x 1644
    val disallowIntercept = RequestDisallowInterceptTouchEvent()

    Box(
        modifier = modifier
            .pointerInteropFilter(
                requestDisallowInterceptTouchEvent = disallowIntercept
            ) { motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_MOVE ||
                    motionEvent.action == MotionEvent.ACTION_DOWN) {
                    disallowIntercept(true)
                    val x = motionEvent.x - boxSize.width / 2
                    val y = motionEvent.y - boxSize.height / 2

                    // Here we map y -> (Right = +x) and x -> (Up = -y)
                    // since we start at the top and move clockwise
                    var phi = toDegrees(kotlin.math.atan2(x, -y).toDouble())
                    if (phi < 0) phi += 360

                    val index = angles.indexOfFirst { it > phi }
                    focusIndex = index - 1
                } else {
                    disallowIntercept(false)
                    focusIndex = -1
                }
            true
            }
            .onGloballyPositioned { boxSize = it.size },
        contentAlignment = Alignment.Center,
    ) {
        val strokeNormalPx = with(LocalDensity.current) { Stroke(stroke.toPx()) }
        val strokeFocusPx = with(LocalDensity.current) { Stroke((10.dp + stroke).toPx()) }
        Canvas(Modifier.fillMaxSize()) {
            val innerRadius = (size.minDimension - strokeFocusPx.width) / 2
            val halfSize = size / 2.0f
            val topLeft = Offset(
                halfSize.width - innerRadius,
                halfSize.height - innerRadius
            )
            val size = Size(innerRadius * 2, innerRadius * 2)
            var startAngle = -90f
            val totalAngle = 360f
            values.forEachIndexed { index, value ->
                val sweep = value / total * totalAngle
                drawArc(
                    color = colors[index],
                    startAngle = startAngle + DividerLengthInDegrees / 2,
                    sweepAngle = sweep - DividerLengthInDegrees,
                    topLeft = topLeft,
                    size = size,
                    useCenter = false,
                    style = if (focusIndex == index) strokeFocusPx  else strokeNormalPx
                )
                startAngle += sweep
            }
        }
        val centerText = if (focusIndex < 0 || focusIndex >= tickers.size) {
            "Total\n" + formatDollar(total)
        } else {
            tickers[focusIndex] + "\n" + formatDollar(values[focusIndex])
        }
        Text(centerText,
            style = MaterialTheme.typography.h2,
            textAlign = TextAlign.Center,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZygosTheme {
        Surface() {
            PieChart(
                tickers = listOf("A", "B", "C", "D"),
                values = listOf(2f, 3f, 4f, 1f),
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