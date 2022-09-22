package com.example.zygos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.components.PieChart
import com.example.zygos.ui.theme.ZygosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZygosApp()
        }
    }
}

@Composable
fun ZygosApp() {
    ZygosTheme() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZygosApp()
}