package com.example.zygos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun ComponentIndicatorLine(
    modifier: Modifier = Modifier,
    strokeWidth: Float = 5f,
    last: Boolean = false,
) {
    val lineColor = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
    Canvas(modifier = modifier) {
        val halfSize = size / 2.0f
        drawLine(
            color = lineColor,
            start = Offset(strokeWidth / 2f, 0f),
            end = Offset(strokeWidth / 2f, if (last) halfSize.height + strokeWidth / 2f else size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = lineColor,
            start = Offset(strokeWidth, halfSize.height),
            end = Offset(size.width, halfSize.height),
            strokeWidth = strokeWidth
        )
    }
}

@Preview
@Composable
fun PreviewComponentIndicatorLine() {
    ZygosTheme {
        Surface {
            ComponentIndicatorLine(Modifier.size(52.dp))
        }
    }
}

@Preview
@Composable
fun PreviewComponentIndicatorLine2() {
    ZygosTheme {
        Surface {
            ComponentIndicatorLine(Modifier.size(52.dp), last = true)
        }
    }
}