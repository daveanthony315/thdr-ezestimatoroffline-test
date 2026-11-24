package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.BlueprintBackdrop
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.ThdrHeaderBar
import com.example.ui.screens.EstimatorScreen
import com.example.ui.screens.MaterialsScreen
import com.example.ui.screens.PriceBookScreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.BlueprintBg
import com.example.ui.theme.BlueprintSurface
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.EstimatorViewModel

object AppRoutes {
    const val ROUTE_ESTIMATE = "estimate"
    const val ROUTE_PRICEBOOK = "pricebook"
    const val ROUTE_MATERIALS = "materials"
}

private enum class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    ESTIMATE(
        route = AppRoutes.ROUTE_ESTIMATE,
        label = "Estimate",
        icon = Icons.Default.Calculate,
        testTag = "nav_estimate"
    ),
    PRICEBOOK(
        route = AppRoutes.ROUTE_PRICEBOOK,
        label = "Price Book",
        icon = Icons.AutoMirrored.Filled.ListAlt,
        testTag = "nav_pricebook"
    ),
    MATERIALS(
        route = AppRoutes.ROUTE_MATERIALS,
        label = "Materials",
        icon = Icons.Default.Build,
        testTag = "nav_materials"
    )
}

@Composable
fun ThdrEstimatorApp(viewModel: EstimatorViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.ROUTE_ESTIMATE
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportPriceBookToUri(uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BlueprintBackdrop {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp
            val isWideContent = maxWidth >= 680.dp

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    ThdrHeaderBar(
                        savedStatus = state.headerStatus,
                        modifier = Modifier.windowInsetsPadding(WindowInsets(0))
                    )
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = BlueprintSurface.copy(alpha = 0.97f),
                            contentColor = TextPrimary
                        ) {
                            TopDestination.entries.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BrandCyan,
                                        selectedTextColor = AccentOrange,
                                        indicatorColor = BlueprintBg,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = BlueprintSurface.copy(alpha = 0.94f),
                            contentColor = TextPrimary,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            TopDestination.entries.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = BrandCyan,
                                        selectedTextColor = AccentOrange,
                                        indicatorColor = BlueprintBg,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = AppRoutes.ROUTE_ESTIMATE
                        ) {
                            composable(AppRoutes.ROUTE_ESTIMATE) {
                                EstimatorScreen(
                                    state = state,
                                    viewModel = viewModel,
                                    isWideScreen = isWideContent
                                )
                            }
                            composable(AppRoutes.ROUTE_PRICEBOOK) {
                                PriceBookScreen(
                                    state = state,
                                    viewModel = viewModel,
                                    isWideScreen = isWideContent,
                                    onExportBackup = {
                                        exportLauncher.launch("THDR_Price_Book_V7_Backup.json")
                                    },
                                    onImportBackup = {
                                        importLauncher.launch(arrayOf("application/json", "*/*"))
                                    }
                                )
                            }
                            composable(AppRoutes.ROUTE_MATERIALS) {
                                MaterialsScreen(
                                    state = state,
                                    viewModel = viewModel,
                                    isWideScreen = isWideContent
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingImportUri?.let { uri ->
        ConfirmActionDialog(
            title = "Import Price Book Backup?",
            message = "Replace all current services, subcategories, pricing, and timeframes with this backup?",
            confirmText = "Import",
            onConfirm = { viewModel.importPriceBookFromUri(uri) },
            onDismiss = { pendingImportUri = null }
        )
    }
}
