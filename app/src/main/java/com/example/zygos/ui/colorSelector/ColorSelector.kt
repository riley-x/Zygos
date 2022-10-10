package com.example.zygos.ui.colorSelector

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils.clamp
import com.example.zygos.ui.components.ImmutableList
import com.example.zygos.ui.theme.ZygosTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


val colors = ImmutableList(listOf(
    Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
))


@Composable
fun ColorCircle(
    brightness: Float,
    modifier: Modifier = Modifier,
    overlay: DrawScope.() -> Unit = { },
) {
    Canvas(modifier.aspectRatio(1f).fillMaxSize()) {
        drawCircle(brush = Brush.sweepGradient(colors.items))

        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White, Color.Transparent)
            )
        )

        drawCircle(color = Color.Black.copy(alpha = 1 - brightness))

        overlay()
    }
}





@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorSelector(
    modifier: Modifier = Modifier,
    initialColor: Color = Color.White,
) {
    var boxSize by remember { mutableStateOf(IntSize(10, 10)) } // 960 x 1644

    fun toPolar(point: Offset, clamp: Boolean = false): Pair<Float, Float> {
        val x = point.x - boxSize.width / 2
        val y = point.y - boxSize.height / 2
        var r = 2 * sqrt(x * x + y * y) / minOf(boxSize.width, boxSize.height)
        if (clamp) r = clamp(r, 0f, 1f)

        var phi = Math.toDegrees(atan2(y, x).toDouble()).toFloat()
        if (phi < 0) phi += 360
        return Pair(r, phi)
    }
    fun fromPolar(r: Float, phi: Float): Offset {
        val minSize = minOf(boxSize.width, boxSize.height) / 2f
        val phiRad = Math.toRadians(phi.toDouble()).toFloat()
        val x = r * cos(phiRad) * minSize + boxSize.width / 2f
        val y = r * sin(phiRad) * minSize + boxSize.height / 2f
        return Offset(x, y)
    }

//    fun colorToPixel(color: Color): Offset {
//
//    }

    fun pixelToColor(point: Offset): Color {
        val (r, phi) = toPolar(point, clamp = true)
        val stopIndex = phi.toInt() / 60

        val hue = ColorUtils.blendARGB(
            colors.items[stopIndex].toArgb(),
            colors.items[stopIndex + 1].toArgb(),
            phi / 60 - stopIndex,
        )
        return Color(ColorUtils.blendARGB(Color.White.toArgb(), hue, r))
    }

    var currentColor by remember { mutableStateOf(initialColor) }
//    var currentHover by remember { mutableStateOf(colorToPixel(initialColor)) }
    var currentHover by remember(boxSize) { mutableStateOf(Offset(boxSize.width / 2f, boxSize.height / 2f)) }
    var brightness by remember { mutableStateOf(1.0f) }

    val indicatorRadius = with(LocalDensity.current) { 10.dp.toPx() }
    val indicatorWidth = with(LocalDensity.current) { 5.dp.toPx() }
    val indicatorColor = remember { derivedStateOf {
        if (brightness > 0.5) Color.Black else Color.White
    } }

    /** The circle indicator should be moved by dragging instead of tapping. Cache the last position
     * from ACTION_MOVE or ACTION_DOWN **/
    var dragStart by remember { mutableStateOf(currentHover) }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .onGloballyPositioned {
                    boxSize = it.size
                }
                .pointerInteropFilter(
                ) { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            dragStart = Offset(motionEvent.x, motionEvent.y)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            var newPos = currentHover + Offset(motionEvent.x, motionEvent.y) - dragStart
                            val (r, phi) = toPolar(newPos)
                            if (r > 1f) newPos = fromPolar(1f, phi)

                            currentHover = newPos
                            currentColor = pixelToColor(currentHover)
                            dragStart = Offset(motionEvent.x, motionEvent.y)
                        }
                    }
                    true
                }
        ) {
            ColorCircle(brightness = brightness)
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = currentColor,
                    radius = indicatorRadius,
                    center = currentHover,
                )
                drawCircle(
                    color = indicatorColor.value,
                    radius = indicatorRadius,
                    center = currentHover,
                    style = Stroke(indicatorWidth),
                )
            }
        }

        Text("${pixelToColor(currentHover)}", Modifier.padding(start = 20.dp, top = 50.dp))
    }
}


@Preview
@Composable
fun PreviewColorCircle() {
    ZygosTheme {
        Surface(Modifier.size(300.dp)) {
            ColorCircle(brightness = 0.4f)
        }
    }
}

@Preview
@Composable
fun PreviewColorSelector() {
    ZygosTheme {
        Surface {
            ColorSelector(
            )
        }
    }
}