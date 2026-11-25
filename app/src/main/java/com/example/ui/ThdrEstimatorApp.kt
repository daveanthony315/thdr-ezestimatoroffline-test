package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.DocumentType
import com.example.ui.components.BlueprintBackdrop
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.ThdrHeaderBar
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.EstimatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MaterialsScreen
import com.example.ui.screens.PriceBookScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SavedDocumentsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LocalThdrPalette
import com.example.viewmodel.EstimatorViewModel

object AppRoutes {
    const val ROUTE_HOME = "home"
    const val ROUTE_ESTIMATE = "estimate"
    const val ROUTE_SAVED_ESTIMATES = "saved_estimates"
    const val ROUTE_PROPOSALS = "proposals"
    const val ROUTE_CUSTOMERS = "customers"
    const val ROUTE_PRICEBOOK = "pricebook"
    const val ROUTE_MATERIALS = "materials"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_PRIVACY_POLICY = "privacy_policy"
}

private enum class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME(
        route = AppRoutes.ROUTE_HOME,
        label = "Home",
        icon = Icons.Default.Home,
        testTag = "nav_home"
    ),
    ESTIMATE(
        route = AppRoutes.ROUTE_ESTIMATE,
        label = "Estimate",
        icon = Icons.Default.Calculate,
        testTag = "nav_estimate"
    ),
    PROPOSALS(
        route = AppRoutes.ROUTE_PROPOSALS,
        label = "Proposals",
        icon = Icons.Default.Description,
        testTag = "nav_proposals"
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
    val palette = LocalThdrPalette.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.ROUTE_HOME
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

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateSubScreen(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    BlueprintBackdrop(showBlueprintGrid = state.settings.showBlueprintBackdrop) {
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
                        companyName = state.settings.companyName,
                        companyTagline = state.settings.companyTagline,
                        onLogoClick = { navigateTopLevel(AppRoutes.ROUTE_HOME) },
                        onOpenCustomers = { navigateSubScreen(AppRoutes.ROUTE_CUSTOMERS) },
                        onOpenSettings = { navigateSubScreen(AppRoutes.ROUTE_SETTINGS) },
                        modifier = Modifier.windowInsetsPadding(WindowInsets(0))
                    )
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = palette.surface.copy(alpha = 0.97f),
                            contentColor = palette.textPrimary
                        ) {
                            TopDestination.entries.forEach { dest ->
                                val selected = currentRoute == dest.route ||
                                    (dest == TopDestination.ESTIMATE && currentRoute == AppRoutes.ROUTE_SAVED_ESTIMATES)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = dest.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = palette.brandHighlight,
                                        selectedTextColor = palette.accent,
                                        indicatorColor = palette.bg,
                                        unselectedIconColor = palette.textMuted,
                                        unselectedTextColor = palette.textMuted
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
                            containerColor = palette.surface.copy(alpha = 0.94f),
                            contentColor = palette.textPrimary,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            TopDestination.entries.forEach { dest ->
                                val selected = currentRoute == dest.route ||
                                    (dest == TopDestination.ESTIMATE && currentRoute == AppRoutes.ROUTE_SAVED_ESTIMATES)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = palette.brandHighlight,
                                        selectedTextColor = palette.accent,
                                        indicatorColor = palette.bg,
                                        unselectedIconColor = palette.textMuted,
                                        unselectedTextColor = palette.textMuted
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
                            startDestination = AppRoutes.ROUTE_HOME
                        ) {
                            composable(AppRoutes.ROUTE_HOME) {
                                HomeScreen(
                                    state = state,
                                    isWideScreen = isWideContent,
                                    onCreateEstimate = {
                                        viewModel.createNewEstimate()
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    },
                                    onOpenSavedEstimates = {
                                        navigateSubScreen(AppRoutes.ROUTE_SAVED_ESTIMATES)
                                    },
                                    onCreateProposal = {
                                        viewModel.createNewProposal()
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    },
                                    onOpenSavedProposals = {
                                        navigateTopLevel(AppRoutes.ROUTE_PROPOSALS)
                                    },
                                    onOpenSettings = {
                                        navigateSubScreen(AppRoutes.ROUTE_SETTINGS)
                                    },
                                    onOpenCustomers = {
                                        navigateSubScreen(AppRoutes.ROUTE_CUSTOMERS)
                                    },
                                    onOpenPriceBook = {
                                        navigateTopLevel(AppRoutes.ROUTE_PRICEBOOK)
                                    },
                                    onOpenMaterials = {
                                        navigateTopLevel(AppRoutes.ROUTE_MATERIALS)
                                    },
                                    onResumeActiveDraft = {
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    },
                                    onOpenSavedDocument = { doc ->
                                        viewModel.loadSavedProposal(doc)
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    },
                                    onOpenPrivacyPolicy = {
                                        navigateSubScreen(AppRoutes.ROUTE_PRIVACY_POLICY)
                                    }
                                )
                            }
                            composable(AppRoutes.ROUTE_ESTIMATE) {
                                EstimatorScreen(
                                    state = state,
                                    viewModel = viewModel,
                                    isWideScreen = isWideContent,
                                    onOpenSavedList = { docType ->
                                        if (docType == DocumentType.PROPOSAL) {
                                            navigateTopLevel(AppRoutes.ROUTE_PROPOSALS)
                                        } else {
                                            navigateSubScreen(AppRoutes.ROUTE_SAVED_ESTIMATES)
                                        }
                                    }
                                )
                            }
                            composable(AppRoutes.ROUTE_SAVED_ESTIMATES) {
                                SavedDocumentsScreen(
                                    docType = DocumentType.ESTIMATE,
                                    state = state,
                                    viewModel = viewModel,
                                    onCreateNew = {
                                        viewModel.createNewEstimate()
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    },
                                    onOpenDocument = { doc ->
                                        viewModel.loadSavedProposal(doc)
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    },
                                    onConvertedToProposal = {
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    }
                                )
                            }
                            composable(AppRoutes.ROUTE_PROPOSALS) {
                                SavedDocumentsScreen(
                                    docType = DocumentType.PROPOSAL,
                                    state = state,
                                    viewModel = viewModel,
                                    onCreateNew = {
                                        viewModel.createNewProposal()
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    },
                                    onOpenDocument = { doc ->
                                        viewModel.loadSavedProposal(doc)
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    },
                                    onConvertedToProposal = {
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    }
                                )
                            }
                            composable(AppRoutes.ROUTE_CUSTOMERS) {
                                CustomersScreen(
                                    state = state,
                                    viewModel = viewModel,
                                    onStartEstimateForCustomer = { customer ->
                                        viewModel.createNewEstimate(customer)
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    },
                                    onStartProposalForCustomer = { customer ->
                                        viewModel.createNewProposal(customer)
                                        navigateTopLevel(AppRoutes.ROUTE_ESTIMATE)
                                    }
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
                            composable(AppRoutes.ROUTE_SETTINGS) {
                                SettingsScreen(
                                    state = state,
                                    viewModel = viewModel,
                                    onOpenPrivacyPolicy = {
                                        navigateSubScreen(AppRoutes.ROUTE_PRIVACY_POLICY)
                                    }
                                )
                            }
                            composable(AppRoutes.ROUTE_PRIVACY_POLICY) {
                                PrivacyPolicyScreen(
                                    settings = state.settings,
                                    onBack = { navController.popBackStack() }
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
