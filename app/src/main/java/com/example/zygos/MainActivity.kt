package com.example.zygos

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.example.zygos.data.database.ZygosDatabase
import com.example.zygos.ui.ZygosApp
import com.example.zygos.viewModel.ZygosViewModel
import com.example.zygos.viewModel.ZygosViewModelFactory


class ZygosApplication : Application() {
    val database: ZygosDatabase by lazy { ZygosDatabase.getDatabase(this) }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val viewModel: ZygosViewModel by viewModels {
            ZygosViewModelFactory(application as ZygosApplication)
        }

        setContent {
            ZygosApp(viewModel)
        }
    }
}




