package com.example.zygos.ui.components

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.HasName
import com.example.zygos.viewModel.HasValue
import com.example.zygos.viewModel.NamedValue
import com.example.zygos.viewModel.TestViewModel


@Composable
fun <T> lineGraph(
    color: Color = MaterialTheme.colors.onSurface,
    size: Float = with(LocalDensity.current) { 2.dp.toPx() },
): TimeSeriesGrapher<T>
        where T : HasName,
              T : HasValue {
    return fun(
        drawScope: DrawScope,
        values: List<T>,
        deltaX: Float,
        deltaY: Float,
        startY: Float,
        minX: Float,
        minY: Float,
    ) {
        for (i in 1 until values.size) {
            drawScope.drawLine(
                start = Offset(
                    x = deltaX * (i - 1 - minX),
                    y = startY + deltaY * (values[i - 1].value - minY)
                ),
                end = Offset(
                    x = deltaX * (i - minX),
                    y = startY + deltaY * (values[i].value - minY)
                ),
                color = color,
                strokeWidth = size,
            )
        }
    }
}

@Preview(
    widthDp = 360,
    heightDp = 400,
)
@Composable
fun TimeSeriesLineGraphPreview() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        TimeSeriesGraph(
            grapher = lineGraph(),
            state = viewModel.accountPerformanceState,
            modifier = Modifier.size(300.dp, 400.dp)
        )
    }
}