package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.EstimatorEngine
import com.example.model.MeasurementBasis
import com.example.model.PriceBookConstants
import com.example.model.PriceBookItem
import com.example.model.PriceViewMode
import com.example.model.TimeframeBenchmark
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.ThdrDangerButton
import com.example.ui.components.ThdrDoubleField
import com.example.ui.components.ThdrDropdownField
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentOrangeContainer
import com.example.ui.theme.BlueprintSurfaceHigh
import com.example.ui.theme.BlueprintSurfaceVariant
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.OutlineBlue
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

@Composable
fun CategoryBulkEditorCard(
    categoryName: String,
    initialDescription: String,
    sampleItem: PriceBookItem?,
    unitOptions: List<String>,
    onApplyBulk: (
        description: String,
        unit: String,
        marketSection: String,
        hourlyRate: Double,
        laborRate: Double,
        benchmarkPrice: Double,
        lowPrice: Double,
        highPrice: Double,
        adjustmentStandard: Double,
        cleanupIncluded: String,
        materialIncluded: Boolean
    ) -> Unit
) {
    var expanded by remember(categoryName) { mutableStateOf(false) }
    var description by remember(categoryName, initialDescription) { mutableStateOf(initialDescription) }
    var unit by remember(categoryName, sampleItem?.unit) { mutableStateOf(sampleItem?.unit ?: "EA") }
    var marketSection by remember(categoryName, sampleItem?.marketSection) { mutableStateOf(sampleItem?.marketSection ?: "Interior") }
    var hourlyRate by remember(categoryName, sampleItem?.hourlyRate) { mutableDoubleStateOf(sampleItem?.hourlyRate ?: 85.0) }
    var laborRate by remember(categoryName, sampleItem?.laborRate) { mutableDoubleStateOf(sampleItem?.laborRate ?: 85.0) }
    var benchmarkPrice by remember(categoryName, sampleItem?.benchmarkUnitPrice) { mutableDoubleStateOf(sampleItem?.benchmarkUnitPrice ?: 0.0) }
    var lowPrice by remember(categoryName, sampleItem?.lowUnitPrice) { mutableDoubleStateOf(sampleItem?.lowUnitPrice ?: 0.0) }
    var highPrice by remember(categoryName, sampleItem?.highUnitPrice) { mutableDoubleStateOf(sampleItem?.highUnitPrice ?: 0.0) }
    var adjustmentStandard by remember(categoryName, sampleItem?.adjustmentStandard) { mutableDoubleStateOf(sampleItem?.adjustmentStandard ?: 1.0) }
    var cleanupIncluded by remember(categoryName, sampleItem?.cleanupIncluded) { mutableStateOf(sampleItem?.cleanupIncluded ?: "Not specified") }
    var materialIncluded by remember(categoryName, sampleItem?.materialIncluded) { mutableStateOf(sampleItem?.materialIncluded ?: false) }
    var showConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AccentOrangeContainer.copy(alpha = 0.72f))
            .border(1.dp, AccentAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Edit category information & apply to all subcategories",
                style = MaterialTheme.typography.titleSmall,
                color = AccentAmber,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle category bulk editor",
                tint = AccentAmber
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(10.dp))
            ThdrTextField(
                value = description,
                onValueChange = { description = it },
                label = "Service category description",
                placeholder = "Category description",
                singleLine = false,
                minLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThdrDropdownField(
                    label = "Unit of measure",
                    selectedLabel = unit,
                    options = unitOptions.map { it to it },
                    onOptionSelected = { unit = it },
                    modifier = Modifier.weight(1f)
                )
                ThdrDropdownField(
                    label = "Interior / Exterior",
                    selectedLabel = marketSection,
                    options = listOf("Interior" to "Interior", "Exterior" to "Exterior"),
                    onOptionSelected = { marketSection = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThdrDoubleField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it },
                    label = "Hourly rate ($/hr)",
                    modifier = Modifier.weight(1f)
                )
                ThdrDoubleField(
                    value = laborRate,
                    onValueChange = { laborRate = it },
                    label = "Internal labor ($/hr)",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThdrDoubleField(
                    value = benchmarkPrice,
                    onValueChange = { benchmarkPrice = it },
                    label = "Benchmark price",
                    modifier = Modifier.weight(1f)
                )
                ThdrDoubleField(
                    value = lowPrice,
                    onValueChange = { lowPrice = it },
                    label = "Low price",
                    modifier = Modifier.weight(1f)
                )
                ThdrDoubleField(
                    value = highPrice,
                    onValueChange = { highPrice = it },
                    label = "High price",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThdrDoubleField(
                    value = adjustmentStandard,
                    onValueChange = { adjustmentStandard = it },
                    label = "Std factor",
                    modifier = Modifier.weight(1f)
                )
                ThdrDropdownField(
                    label = "Materials included",
                    selectedLabel = if (materialIncluded) "Yes" else "No",
                    options = listOf("No" to false, "Yes" to true),
                    onOptionSelected = { materialIncluded = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ThdrTextField(
                value = cleanupIncluded,
                onValueChange = { cleanupIncluded = it },
                label = "Cleanup included"
            )
            Spacer(modifier = Modifier.height(10.dp))
            ThdrPrimaryButton(
                text = "Apply information to all subcategories",
                onClick = { showConfirm = true },
                orangeAccent = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Applies the fields above to every subcategory in this service. Individual items can still be edited afterward.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }

    if (showConfirm) {
        ConfirmActionDialog(
            title = "Apply Category Settings?",
            message = "Apply these settings to all subcategories in \"$categoryName\"? Existing values for the listed fields will be replaced.",
            confirmText = "Apply to all",
            onConfirm = {
                onApplyBulk(
                    description, unit, marketSection, hourlyRate, laborRate,
                    benchmarkPrice, lowPrice, highPrice, adjustmentStandard,
                    cleanupIncluded, materialIncluded
                )
            },
            onDismiss = { showConfirm = false }
        )
    }
}

@Composable
fun PriceBookSubcategoryCard(
    item: PriceBookItem,
    priceViewMode: PriceViewMode,
    categoryNames: List<String>,
    unitOptions: List<String>,
    onUpdateItem: (PriceBookItem) -> Unit,
    onDeleteItem: (Long) -> Unit
) {
    var expanded by remember(item.id) { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val isHr = item.unit.equals("HR", ignoreCase = true)
    val effectiveBasis = item.effectiveBasis()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BlueprintSurfaceVariant)
            .border(1.dp, OutlineSubtle, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueprintSurfaceHigh.copy(alpha = 0.7f))
                .clickable { expanded = !expanded }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.item,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "${item.unit} · ${effectiveBasis.label} · ${item.crewAssumption} · ${item.marketSection}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle line item",
                    tint = BrandCyan
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isHr) {
                    MetricMiniCell(
                        label = "Hourly rate",
                        value = "${EstimatorEngine.formatMoney(item.hourlyRate)}/HR",
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCell(
                        label = "Est. time",
                        value = String.format(Locale.US, "%.2f hrs", item.estimatedCompletionHours),
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCell(
                        label = "Total labor",
                        value = EstimatorEngine.formatMoney(item.hourlyRate * item.estimatedCompletionHours),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    MetricMiniCell(
                        label = priceViewMode.label,
                        value = "${EstimatorEngine.formatMoney(item.displayUnitPrice(priceViewMode))}/${item.unit}",
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCell(
                        label = "Low / High",
                        value = "${EstimatorEngine.formatMoney(item.lowUnitPrice)}–${EstimatorEngine.formatMoney(item.highUnitPrice)}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCell(
                        label = "Labor time",
                        value = String.format(Locale.US, "%.4f hrs/${item.unit}", item.benchmarkLaborHoursPerUnit),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThdrTextField(
                    value = item.item,
                    onValueChange = { onUpdateItem(item.copy(item = it)) },
                    label = "Subcategory / line item"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThdrDropdownField(
                        label = "Unit",
                        selectedLabel = item.unit,
                        options = unitOptions.map { it to it },
                        onOptionSelected = { onUpdateItem(item.copy(unit = it)) },
                        modifier = Modifier.weight(1f)
                    )
                    ThdrDropdownField(
                        label = "Interior / Exterior",
                        selectedLabel = item.marketSection,
                        options = listOf("Interior" to "Interior", "Exterior" to "Exterior"),
                        onOptionSelected = { onUpdateItem(item.copy(marketSection = it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                val basisOptions = MeasurementBasis.optionsForUnit(item.unit)
                ThdrDropdownField(
                    label = "Quantity calculation",
                    selectedLabel = effectiveBasis.label,
                    options = basisOptions.map { it.label to it.key },
                    onOptionSelected = { onUpdateItem(item.copy(measurementBasis = it)) }
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThdrDoubleField(
                        value = item.benchmarkUnitPrice,
                        onValueChange = { onUpdateItem(item.copy(benchmarkUnitPrice = it)) },
                        label = "Benchmark price ($)",
                        modifier = Modifier.weight(1f)
                    )
                    ThdrDoubleField(
                        value = item.laborRate,
                        onValueChange = { onUpdateItem(item.copy(laborRate = it)) },
                        label = "Internal labor ($/hr)",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThdrDoubleField(
                        value = item.lowUnitPrice,
                        onValueChange = { onUpdateItem(item.copy(lowUnitPrice = it)) },
                        label = "Low rate ($)",
                        modifier = Modifier.weight(1f)
                    )
                    ThdrDoubleField(
                        value = item.highUnitPrice,
                        onValueChange = { onUpdateItem(item.copy(highUnitPrice = it)) },
                        label = "High rate ($)",
                        modifier = Modifier.weight(1f)
                    )
                    ThdrDoubleField(
                        value = item.adjustmentStandard,
                        onValueChange = { onUpdateItem(item.copy(adjustmentStandard = it)) },
                        label = "Std factor",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThdrDropdownField(
                        label = "Service category",
                        selectedLabel = item.category,
                        options = categoryNames.map { it to it },
                        onOptionSelected = { onUpdateItem(item.copy(category = it)) },
                        modifier = Modifier.weight(1f)
                    )
                    ThdrDropdownField(
                        label = "Materials included",
                        selectedLabel = if (item.materialIncluded) "Yes" else "No",
                        options = listOf("No" to false, "Yes" to true),
                        onOptionSelected = { onUpdateItem(item.copy(materialIncluded = it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                ThdrTextField(
                    value = item.scopeNotes,
                    onValueChange = { onUpdateItem(item.copy(scopeNotes = it)) },
                    label = "Scope notes",
                    singleLine = false,
                    minLines = 2
                )
                ThdrTextField(
                    value = item.cleanupIncluded,
                    onValueChange = { onUpdateItem(item.copy(cleanupIncluded = it)) },
                    label = "Cleanup included"
                )

                if (isHr) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BlueprintSurfaceHigh.copy(alpha = 0.85f))
                            .border(1.dp, OutlineBlue, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Hourly pricing formula",
                            style = MaterialTheme.typography.titleSmall,
                            color = BrandCyan
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThdrDoubleField(
                                value = item.hourlyRate,
                                onValueChange = { onUpdateItem(item.copy(hourlyRate = it)) },
                                label = "Hourly labor rate ($/hr)",
                                modifier = Modifier.weight(1f)
                            )
                            ThdrDoubleField(
                                value = item.estimatedCompletionHours,
                                onValueChange = { onUpdateItem(item.copy(estimatedCompletionHours = it)) },
                                label = "Est. completion (hrs)",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = "Total: ${EstimatorEngine.formatMoney(item.hourlyRate * item.estimatedCompletionHours)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                            color = BrandCyan
                        )
                    }
                }

                ThdrDangerButton(
                    text = "Remove subcategory",
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "delete_pb_item_${item.id}"
                )
            }
        }
    }

    if (confirmDelete) {
        ConfirmActionDialog(
            title = "Delete Subcategory?",
            message = "Delete subcategory \"${item.item}\" from ${item.category}?",
            confirmText = "Delete",
            onConfirm = { onDeleteItem(item.id) },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun MetricMiniCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = JetBrainsMono,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (highlight) BrandCyan else TextPrimary
        )
    }
}

@Composable
fun TimeframeBenchmarkCard(
    tf: TimeframeBenchmark,
    onUpdate: (TimeframeBenchmark) -> Unit,
    onDelete: (Long) -> Unit
) {
    var expanded by remember(tf.id) { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BlueprintSurfaceVariant)
            .border(1.dp, OutlineSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tf.projectType,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandCyan
                )
                Text(
                    text = "${tf.crewCalendar} · ${EstimatorEngine.formatQty(tf.lowHours)}–${EstimatorEngine.formatQty(tf.highHours)} labor hours",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle timeframe benchmark",
                tint = BrandCyan
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThdrTextField(
                    value = tf.projectType,
                    onValueChange = { onUpdate(tf.copy(projectType = it)) },
                    label = "Project type"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThdrTextField(
                        value = tf.category,
                        onValueChange = { onUpdate(tf.copy(category = it)) },
                        label = "Category",
                        modifier = Modifier.weight(1f)
                    )
                    ThdrTextField(
                        value = tf.typicalScope,
                        onValueChange = { onUpdate(tf.copy(typicalScope = it)) },
                        label = "Typical scope",
                        modifier = Modifier.weight(1f)
                    )
                }
                ThdrTextField(
                    value = tf.crewCalendar,
                    onValueChange = { onUpdate(tf.copy(crewCalendar = it)) },
                    label = "Crew / calendar duration"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThdrDoubleField(
                        value = tf.lowHours,
                        onValueChange = { onUpdate(tf.copy(lowHours = it)) },
                        label = "Low labor hours",
                        modifier = Modifier.weight(1f)
                    )
                    ThdrDoubleField(
                        value = tf.highHours,
                        onValueChange = { onUpdate(tf.copy(highHours = it)) },
                        label = "High labor hours",
                        modifier = Modifier.weight(1f)
                    )
                }
                ThdrTextField(
                    value = tf.notes,
                    onValueChange = { onUpdate(tf.copy(notes = it)) },
                    label = "Scheduling notes",
                    singleLine = false,
                    minLines = 2
                )
                ThdrDangerButton(
                    text = "Remove timeframe",
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (confirmDelete) {
        ConfirmActionDialog(
            title = "Remove Timeframe?",
            message = "Delete \"${tf.projectType}\" from Project Timeframe Benchmarks?",
            confirmText = "Delete",
            onConfirm = { onDelete(tf.id) },
            onDismiss = { confirmDelete = false }
        )
    }
}
