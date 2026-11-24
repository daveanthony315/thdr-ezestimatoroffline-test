package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.ThdrEstimatorApp
import com.example.ui.theme.ThdrTheme
import com.example.viewmodel.EstimatorViewModel

class MainActivity : ComponentActivity() {
    private val estimatorViewModel: EstimatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThdrTheme {
                ThdrEstimatorApp(viewModel = estimatorViewModel)
            }
        }
    }
}
