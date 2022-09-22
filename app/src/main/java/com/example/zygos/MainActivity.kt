package com.example.zygos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.runtime.*
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
    ZygosTheme {
        Scaffold(
            bottomBar = { ZygosNav() },
        ) {
            Surface(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
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
}

@Composable
fun ZygosNav() {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(Icons.Sharp.ShowChart, Icons.Sharp.PieChart)
    BottomNavigation(
        //elevation = 0.dp,
    ) {
        items.forEachIndexed { index, item ->
            BottomNavigationItem(
                icon = { Icon(item, contentDescription = null) },
                selected = selectedItem == index,
                onClick = { selectedItem = index }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ZygosApp()
}