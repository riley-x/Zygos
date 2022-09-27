package com.example.zygos.ui.components

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.zygos.viewModel.NamedValue


@Composable
fun lineGraph(
    color: Color = MaterialTheme.colors.onSurface,
    size: Float = with(LocalDensity.current) { 2.dp.toPx() },
): TimeSeriesGrapher<NamedValue> {
    return fun(
       drawScope: DrawScope,
       values: SnapshotStateList<NamedValue>,
       deltaX: Float,
       deltaY: Float,
       startY: Float,
       minY: Float,
    ) {
        for (i in 1 until values.size) {
            drawScope.drawLine(
                start = Offset(
                    x = deltaX * (i - 1),
                    y = startY + deltaY * (values[i - 1].value - minY)
                ),
                end = Offset(x = deltaX * i, y = startY + deltaY * (values[i].value - minY)),
                color = color,
                strokeWidth = size,
            )
        }
    }
}