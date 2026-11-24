package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ThdrEstimatorApp
import com.example.ui.theme.ThdrTheme
import com.example.viewmodel.EstimatorViewModel

class MainActivity : ComponentActivity() {
    private val estimatorViewModel: EstimatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by estimatorViewModel.uiState.collectAsStateWithLifecycle()
            ThdrTheme(settings = state.settings) {
                ThdrEstimatorApp(viewModel = estimatorViewModel)
            }
        }
    }
}
