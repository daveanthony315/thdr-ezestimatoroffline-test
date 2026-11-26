package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.PriceBookConstants
import com.example.model.PriceViewMode
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.PromptInputDialog
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrDangerButton
import com.example.ui.components.ThdrDropdownField
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.LocalThdrPalette
import com.example.viewmodel.EstimatorUiState
import com.example.viewmodel.EstimatorViewModel
import java.util.Locale

@Composable
fun PriceBookScreen(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    isWideScreen: Boolean,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    val palette = LocalThdrPalette.current
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddSubcategoryPickerDialog by remember { mutableStateOf(false) }
    var addSubToCategory by remember { mutableStateOf<String?>(null) }
    var renameCategoryTarget by remember { mutableStateOf<String?>(null) }
    var editDescCategoryTarget by remember { mutableStateOf<String?>(null) }
    var deleteCategoryTarget by remember { mutableStateOf<String?>(null) }
    var showAddTimeframeDialog by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

    val query = state.pbSearchQuery.trim().lowercase(Locale.US)
    val selectedFilter = state.pbCategoryFilter
    val categoryNames = remember(state.categories) { state.categories.map { it.name } }

    val filteredItems = remember(state.priceBookItems, query, selectedFilter) {
        state.priceBookItems.filter { item ->
            val filterMatch = when (selectedFilter) {
                PriceBookConstants.FILTER_ALL -> true
                PriceBookConstants.FILTER_INTERIOR -> item.marketSection == "Interior"
                PriceBookConstants.FILTER_EXTERIOR -> item.marketSection == "Exterior"
                else -> item.category == selectedFilter
            }
            val searchMatch = query.isEmpty() ||
                item.item.lowercase(Locale.US).contains(query) ||
                item.category.lowercase(Locale.US).contains(query) ||
                item.scopeNotes.lowercase(Locale.US).contains(query) ||
                item.unit.lowercase(Locale.US).contains(query)
            filterMatch && searchMatch
        }
    }

    val visibleCategories = remember(state.categories, filteredItems, query, selectedFilter) {
        state.categories.filter { cat ->
            val catMatch = when (selectedFilter) {
                PriceBookConstants.FILTER_ALL -> true
                PriceBookConstants.FILTER_INTERIOR, PriceBookConstants.FILTER_EXTERIOR ->
                    filteredItems.any { it.category == cat.name }
                else -> cat.name == selectedFilter
            }
            val searchMatch = query.isEmpty() ||
                cat.name.lowercase(Locale.US).contains(query) ||
                filteredItems.any { it.category == cat.name }
            val hasItemsOrAllowedEmpty = filteredItems.any { it.category == cat.name } ||
                (query.isEmpty() && selectedFilter != PriceBookConstants.FILTER_INTERIOR && selectedFilter != PriceBookConstants.FILTER_EXTERIOR &&
                    (selectedFilter == PriceBookConstants.FILTER_ALL || selectedFilter == cat.name))
            catMatch && searchMatch && hasItemsOrAllowedEmpty
        }
    }

    val suggestions = remember(state.categories, state.priceBookItems, query) {
        if (query.isEmpty()) emptyList()
        else (state.categories.map { it.name } + state.priceBookItems.map { it.item })
            .distinct()
            .filter { it.lowercase(Locale.US).contains(query) }
            .sorted()
            .take(10)
    }

    val unitOptions = remember(state.priceBookItems) {
        (PriceBookConstants.STANDARD_UNITS + state.priceBookItems.map { it.unit }.filter { it.isNotBlank() })
            .distinct()
            .sorted()
    }

    val tfCategories = remember(state.timeframes) {
        state.timeframes.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val tfQuery = state.tfSearchQuery.trim().lowercase(Locale.US)
    val filteredTimeframes = remember(state.timeframes, tfQuery, state.tfCategoryFilter) {
        state.timeframes.filter { tf ->
            val catOk = state.tfCategoryFilter.isEmpty() || tf.category == state.tfCategoryFilter
            val searchOk = tfQuery.isEmpty() ||
                tf.projectType.lowercase(Locale.US).contains(tfQuery) ||
                tf.category.lowercase(Locale.US).contains(tfQuery) ||
                tf.typicalScope.lowercase(Locale.US).contains(tfQuery) ||
                tf.notes.lowercase(Locale.US).contains(tfQuery)
            catOk && searchOk
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Price Book Header & Controls
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "Price Book",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.pricebook_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Search + suggestions
                    ThdrTextField(
                        value = state.pbSearchQuery,
                        onValueChange = {
                            viewModel.setPbSearchQuery(it)
                            showSuggestions = it.isNotBlank()
                        },
                        label = "Search services and subcategories",
                        placeholder = "Start typing a service name...",
                        testTag = "pb_search_input"
                    )
                    if (showSuggestions && suggestions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.surfaceVariant)
                                .border(1.dp, palette.outline, RoundedCornerShape(10.dp))
                        ) {
                            suggestions.forEach { s ->
                                Text(
                                    text = s,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setPbSearchQuery(s)
                                            showSuggestions = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                                HorizontalDivider(color = palette.outlineSubtle)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val filterOptions = listOf(
                        "All categories" to PriceBookConstants.FILTER_ALL,
                        "Interior" to PriceBookConstants.FILTER_INTERIOR,
                        "Exterior" to PriceBookConstants.FILTER_EXTERIOR
                    ) + categoryNames.map { it to it }
                    val selectedFilterLabel = filterOptions.firstOrNull { it.second == selectedFilter }?.first ?: "All categories"

                    if (isWideScreen) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ThdrDropdownField(
                                label = "Filter category",
                                selectedLabel = selectedFilterLabel,
                                options = filterOptions,
                                onOptionSelected = viewModel::setPbCategoryFilter,
                                modifier = Modifier.weight(1f),
                                testTag = "pb_category_filter"
                            )
                            ThdrDropdownField(
                                label = "Pricing benchmark",
                                selectedLabel = state.pbPriceViewMode.label,
                                options = PriceViewMode.entries.map { it.label to it },
                                onOptionSelected = viewModel::setPbPriceViewMode,
                                modifier = Modifier.weight(1f),
                                testTag = "pb_price_view_filter"
                            )
                        }
                    } else {
                        ThdrDropdownField(
                            label = "Filter category",
                            selectedLabel = selectedFilterLabel,
                            options = filterOptions,
                            onOptionSelected = viewModel::setPbCategoryFilter,
                            testTag = "pb_category_filter"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ThdrDropdownField(
                            label = "Pricing benchmark",
                            selectedLabel = state.pbPriceViewMode.label,
                            options = PriceViewMode.entries.map { it.label to it },
                            onOptionSelected = viewModel::setPbPriceViewMode,
                            testTag = "pb_price_view_filter"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Base labor rate: $85.00 / labor hour · Customer-facing units: SF, LF, EA, STEP, ROOM, and other listed units.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.brandHighlight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.pricebook_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThdrPrimaryButton(
                            text = "+ Add category",
                            onClick = { showAddCategoryDialog = true },
                            modifier = Modifier.weight(1f),
                            testTag = "add_pb_category_btn"
                        )
                        ThdrPrimaryButton(
                            text = "+ Add subcategory",
                            onClick = {
                                if (categoryNames.isEmpty()) {
                                    showAddCategoryDialog = true
                                } else {
                                    showAddSubcategoryPickerDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "add_pb_item_btn"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThdrSecondaryButton(
                            text = "Export full Price Book",
                            onClick = onExportBackup,
                            modifier = Modifier.weight(1f),
                            testTag = "export_pb_btn"
                        )
                        ThdrSecondaryButton(
                            text = "Import backup",
                            onClick = onImportBackup,
                            modifier = Modifier.weight(1f),
                            testTag = "import_pb_btn"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${state.categories.size} service categories · ${state.priceBookItems.size} subcategories / line items · ${filteredItems.size} matching items",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.brandHighlight,
                        modifier = Modifier.testTag("pb_stats_text")
                    )
                }
            }
        }

        // Accordion Categories (collapsed until clicked; opening one closes others)
        if (visibleCategories.isEmpty()) {
            item {
                Box(modifier = Modifier.widthIn(max = 980.dp)) {
                    ThdrCard {
                        Text(
                            text = "No services or line items match the selected search and category filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textMuted
                        )
                    }
                }
            }
        } else {
            items(visibleCategories, key = { it.name }) { category ->
                val catItems = filteredItems.filter { it.category == category.name }
                val isExpanded = state.expandedPbCategory == category.name
                Box(modifier = Modifier.widthIn(max = 980.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .background(palette.surface.copy(alpha = 0.96f))
                            .border(1.dp, palette.outline, RoundedCornerShape(15.dp))
                    ) {
                        val headerColors = if (palette.isDark) {
                            listOf(palette.primary.copy(alpha = 0.65f), palette.surface, palette.accent.copy(alpha = 0.24f))
                        } else {
                            listOf(palette.surfaceHigh, palette.surface, palette.accent.copy(alpha = 0.14f))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.horizontalGradient(headerColors))
                                .clickable { viewModel.togglePbCategoryExpanded(category.name) }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("pb_category_header_${category.name}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.brandHighlight
                                )
                                Text(
                                    text = "${catItems.size} matching subcategories",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textMuted
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle category",
                                tint = palette.brandHighlight
                            )
                        }

                        if (isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CategoryBulkEditorCard(
                                    categoryName = category.name,
                                    initialDescription = category.description,
                                    sampleItem = catItems.firstOrNull(),
                                    unitOptions = unitOptions,
                                    onApplyBulk = { desc, unit, market, hr, lr, bp, lp, hp, adj, clean, mat ->
                                        viewModel.applyCategoryBulkSettings(
                                            categoryName = category.name,
                                            description = desc,
                                            unit = unit,
                                            marketSection = market,
                                            hourlyRate = hr,
                                            laborRate = lr,
                                            benchmarkPrice = bp,
                                            lowPrice = lp,
                                            highPrice = hp,
                                            adjustmentStandard = adj,
                                            cleanupIncluded = clean,
                                            materialIncluded = mat
                                        )
                                    }
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ThdrPrimaryButton(
                                        text = "+ Add subcategory",
                                        onClick = { addSubToCategory = category.name },
                                        modifier = Modifier.weight(1f)
                                    )
                                    ThdrSecondaryButton(
                                        text = "Rename service",
                                        onClick = { renameCategoryTarget = category.name },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ThdrSecondaryButton(
                                        text = "Edit details",
                                        onClick = { editDescCategoryTarget = category.name },
                                        modifier = Modifier.weight(1f)
                                    )
                                    ThdrDangerButton(
                                        text = "Remove service",
                                        onClick = { deleteCategoryTarget = category.name },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (catItems.isEmpty()) {
                                    Text(
                                        text = "No subcategories match this filter.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textMuted,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                } else {
                                    catItems.forEach { pbItem ->
                                        PriceBookSubcategoryCard(
                                            item = pbItem,
                                            priceViewMode = state.pbPriceViewMode,
                                            categoryNames = categoryNames,
                                            unitOptions = unitOptions,
                                            onUpdateItem = viewModel::updatePbItem,
                                            onDeleteItem = viewModel::deletePbItem
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save Price Book Confirmation Card
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "Price Book changes",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.pbSaveStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted,
                        modifier = Modifier.testTag("pb_save_status")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ThdrPrimaryButton(
                        text = "Save Price Book",
                        onClick = { viewModel.savePriceBookNow() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "save_pricebook_btn"
                    )
                }
            }
        }

        // Project Timeframe Benchmarks Card
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "Project Timeframe Benchmarks",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Planning ranges for complete scopes. Scheduling references, not promised completion dates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ThdrTextField(
                        value = state.tfSearchQuery,
                        onValueChange = viewModel::setTfSearchQuery,
                        label = "Search project types",
                        placeholder = "Search project timeframe...",
                        testTag = "tf_search_input"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThdrDropdownField(
                            label = "Category",
                            selectedLabel = state.tfCategoryFilter.ifEmpty { "All categories" },
                            options = listOf("All categories" to "") + tfCategories.map { it to it },
                            onOptionSelected = viewModel::setTfCategoryFilter,
                            modifier = Modifier.weight(1f)
                        )
                        ThdrPrimaryButton(
                            text = "+ Add timeframe",
                            onClick = { showAddTimeframeDialog = true },
                            modifier = Modifier.weight(1f),
                            testTag = "add_timeframe_btn"
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredTimeframes.isEmpty()) {
                        Text(
                            text = "No timeframe benchmarks match this search.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredTimeframes.forEach { tf ->
                                TimeframeBenchmarkCard(
                                    tf = tf,
                                    onUpdate = viewModel::updateTimeframe,
                                    onDelete = viewModel::deleteTimeframe
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Calendar duration depends on crew size, sequencing, drying/curing, inspections, material availability, and trade coordination. Labor hours are total labor input, not elapsed clock hours.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        PromptInputDialog(
            title = "New Service Category",
            label = "Service category name",
            confirmText = "Add",
            onConfirm = viewModel::addPbCategory,
            onDismiss = { showAddCategoryDialog = false }
        )
    }

    if (showAddSubcategoryPickerDialog && categoryNames.isNotEmpty()) {
        var chosenCategory by remember(categoryNames) { mutableStateOf(categoryNames.first()) }
        var itemName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddSubcategoryPickerDialog = false },
            containerColor = palette.surface,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textPrimary,
            title = { Text("Add Subcategory / Line Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ThdrDropdownField(
                        label = "Service category",
                        selectedLabel = chosenCategory,
                        options = categoryNames.map { it to it },
                        onOptionSelected = { chosenCategory = it }
                    )
                    ThdrTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = "Subcategory / line item name"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            viewModel.addPbItem(chosenCategory, itemName)
                            showAddSubcategoryPickerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubcategoryPickerDialog = false }) {
                    Text("Cancel", color = palette.textMuted)
                }
            }
        )
    }

    addSubToCategory?.let { cat ->
        PromptInputDialog(
            title = "New Subcategory for $cat",
            label = "Subcategory / line item name",
            confirmText = "Add",
            onConfirm = { name -> viewModel.addPbItem(cat, name) },
            onDismiss = { addSubToCategory = null }
        )
    }

    renameCategoryTarget?.let { oldName ->
        PromptInputDialog(
            title = "Rename Service",
            label = "Service name",
            initialValue = oldName,
            confirmText = "Rename",
            onConfirm = { newName -> viewModel.renamePbCategory(oldName, newName) },
            onDismiss = { renameCategoryTarget = null }
        )
    }

    editDescCategoryTarget?.let { catName ->
        val existing = state.categories.firstOrNull { it.name == catName }?.description.orEmpty()
        PromptInputDialog(
            title = "Description for $catName",
            label = "Service description",
            initialValue = existing,
            confirmText = "Save",
            onConfirm = { desc -> viewModel.updatePbCategoryDescription(catName, desc) },
            onDismiss = { editDescCategoryTarget = null }
        )
    }

    deleteCategoryTarget?.let { catName ->
        val count = state.priceBookItems.count { it.category == catName }
        ConfirmActionDialog(
            title = "Remove Entire Service?",
            message = "Permanently remove \"$catName\" and all $count subcategories? Export a backup first if you may need this data.",
            confirmText = "Remove",
            onConfirm = { viewModel.deletePbCategory(catName) },
            onDismiss = { deleteCategoryTarget = null }
        )
    }

    if (showAddTimeframeDialog) {
        PromptInputDialog(
            title = "Add Project Timeframe",
            label = "Project type name",
            confirmText = "Add",
            onConfirm = viewModel::addTimeframe,
            onDismiss = { showAddTimeframeDialog = false }
        )
    }
}
