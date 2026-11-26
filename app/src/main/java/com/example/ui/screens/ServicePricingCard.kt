package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.EstimatorEngine
import com.example.model.EstimateCatalog
import com.example.model.EstimateRoom
import com.example.model.MeasurementBasis
import com.example.model.PriceBookConstants
import com.example.model.PriceBookItem
import com.example.model.PricingUnitCategory
import com.example.model.RoomDimensions
import com.example.model.RoomServiceSelection
import com.example.ui.components.SelectablePill
import com.example.ui.components.ThdrDoubleField
import com.example.ui.components.ThdrDropdownField
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrTextField
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalThdrPalette
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServicePricingCard(
    roomIndex: Int,
    serviceIndex: Int,
    room: EstimateRoom,
    dims: RoomDimensions,
    service: String,
    priceBook: List<PriceBookItem>,
    onSelectPbItem: (String) -> Unit,
    onSelectPricingUnit: (String) -> Unit,
    onSelectMeasurementBasis: (String) -> Unit,
    onQtyChange: (Double) -> Unit,
    onResetQty: () -> Unit,
    onRateChange: (Double) -> Unit,
    onScopeNotesChange: (String) -> Unit,
    onToggleSub: (String) -> Unit,
    onSetQuestionAnswer: (String, String, Boolean) -> Unit
) {
    val palette = LocalThdrPalette.current
    val items = remember(service, priceBook) { EstimatorEngine.pbItemsForService(service, priceBook) }
    val sel = room.serviceSelections[service] ?: RoomServiceSelection(serviceName = service)
    var showScopeQuestions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceHigh.copy(alpha = 0.72f))
            .border(1.dp, palette.outline, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("service_box_${roomIndex}_$serviceIndex")
    ) {
        if (items.isEmpty()) {
            Text(
                text = service,
                style = MaterialTheme.typography.titleSmall,
                color = palette.brandHighlight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No matching Price Book line items are currently saved for this service. Add or assign a line item in the Price Book.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThdrTextField(
                value = sel.scopeNotes,
                onValueChange = onScopeNotesChange,
                label = "Scope notes",
                singleLine = false,
                minLines = 2
            )
            return@Column
        }

        val chosen = items.firstOrNull { it.item == sel.selectedPbItemName } ?: items.first()
        val effectiveUnit = sel.effectiveUnit(chosen.unit)
        val unitCategory = PricingUnitCategory.fromUnit(effectiveUnit)
        val basis = EstimatorEngine.effectiveBasisForSelection(sel, chosen, effectiveUnit)
        val basisOptions = remember(effectiveUnit) { MeasurementBasis.optionsForUnit(effectiveUnit) }
        val isHr = effectiveUnit.equals("HR", ignoreCase = true)
        val qty = if (sel.quantityManual) {
            sel.quantity
        } else {
            EstimatorEngine.defaultQty(effectiveUnit, dims, room, service, basis)
        }
        val rate = if (sel.rateInitialized) {
            sel.rate
        } else {
            EstimatorEngine.defaultRateForUnit(chosen, effectiveUnit)
        }
        val lineTotal = qty * rate
        val pricePresets = remember(chosen, effectiveUnit) {
            chosen.unitPricePresetsFor(effectiveUnit)
        }

        Text(
            text = "$service — Price Book & Unit Pricing",
            style = MaterialTheme.typography.titleSmall,
            color = palette.brandHighlight
        )
        Spacer(modifier = Modifier.height(8.dp))

        ThdrDropdownField(
            label = "Price Book line item",
            selectedLabel = "${chosen.item} · ${chosen.unit}",
            options = items.map { "${it.item} · ${it.unit}" to it.item },
            onOptionSelected = onSelectPbItem,
            testTag = "pb_item_picker_${roomIndex}_$serviceIndex"
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Pricing mode & unit",
            style = MaterialTheme.typography.labelMedium,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectablePill(
                text = "Hourly (HR)",
                selected = unitCategory == PricingUnitCategory.HOURLY,
                onClick = { onSelectPricingUnit("HR") },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                testTag = "unit_mode_hourly_${roomIndex}_$serviceIndex"
            )
            SelectablePill(
                text = "Measurement (SF/LF/SQ)",
                selected = unitCategory == PricingUnitCategory.MEASUREMENT,
                onClick = {
                    val target = when {
                        PricingUnitCategory.fromUnit(effectiveUnit) == PricingUnitCategory.MEASUREMENT -> effectiveUnit
                        PricingUnitCategory.fromUnit(chosen.unit) == PricingUnitCategory.MEASUREMENT -> chosen.unit
                        else -> "SF"
                    }
                    onSelectPricingUnit(target)
                },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                testTag = "unit_mode_measurement_${roomIndex}_$serviceIndex"
            )
            SelectablePill(
                text = "Individual unit (EA/Flat)",
                selected = unitCategory == PricingUnitCategory.INDIVIDUAL,
                onClick = {
                    val target = when {
                        PricingUnitCategory.fromUnit(effectiveUnit) == PricingUnitCategory.INDIVIDUAL -> effectiveUnit
                        PricingUnitCategory.fromUnit(chosen.unit) == PricingUnitCategory.INDIVIDUAL -> chosen.unit
                        else -> "EA"
                    }
                    onSelectPricingUnit(target)
                },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                testTag = "unit_mode_individual_${roomIndex}_$serviceIndex"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThdrDropdownField(
                label = "Pricing unit",
                selectedLabel = PriceBookConstants.describeUnit(effectiveUnit),
                options = PriceBookConstants.STANDARD_UNITS.map {
                    PriceBookConstants.describeUnit(it) to it
                },
                onOptionSelected = onSelectPricingUnit,
                modifier = Modifier.weight(1f),
                testTag = "pricing_unit_picker_${roomIndex}_$serviceIndex"
            )
            if (basisOptions.size > 1) {
                ThdrDropdownField(
                    label = "Measurement basis",
                    selectedLabel = basis.label,
                    options = basisOptions.map { it.label to it.key },
                    onOptionSelected = onSelectMeasurementBasis,
                    modifier = Modifier.weight(1f),
                    testTag = "measurement_basis_picker_${roomIndex}_$serviceIndex"
                )
            }
        }

        if (pricePresets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Individual unit price selection ($effectiveUnit)",
                style = MaterialTheme.typography.labelMedium,
                color = palette.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pricePresets.forEach { preset ->
                    val isSelected = abs(rate - preset.amount) < 0.005
                    SelectablePill(
                        text = "${preset.label} ${EstimatorEngine.formatMoney(preset.amount)}",
                        selected = isSelected,
                        onClick = { onRateChange(preset.amount) },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        testTag = "price_preset_${preset.label.lowercase()}_${roomIndex}_$serviceIndex"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThdrDoubleField(
                value = qty,
                onValueChange = onQtyChange,
                label = if (isHr) "Hours" else "Qty ($effectiveUnit)",
                modifier = Modifier.weight(1f),
                testTag = "service_qty_${roomIndex}_$serviceIndex"
            )
            ThdrDoubleField(
                value = rate,
                onValueChange = onRateChange,
                label = if (isHr) "Hourly rate ($)" else "Unit price / $effectiveUnit ($)",
                modifier = Modifier.weight(1f),
                testTag = "service_rate_${roomIndex}_$serviceIndex"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${basis.label} · $effectiveUnit",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Line total: ${EstimatorEngine.formatMoney(lineTotal)}",
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                color = palette.brandHighlight
            )
        }

        if (sel.quantityManual) {
            Spacer(modifier = Modifier.height(8.dp))
            ThdrSecondaryButton(
                text = "Reset quantity to measurement",
                onClick = onResetQty,
                testTag = "reset_qty_${roomIndex}_$serviceIndex"
            )
        }

        if (rate <= 0.0) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "No price is configured for this unit. Pick a preset or enter a custom unit price above.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.warning
            )
        }

        val def = EstimateCatalog.SERVICE_DEFINITIONS[service]
        if (def != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showScopeQuestions = !showScopeQuestions }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Scope options & takeoff details",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.brandHighlight
                )
                Icon(
                    imageVector = if (showScopeQuestions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle scope options",
                    tint = palette.brandHighlight
                )
            }

            if (showScopeQuestions) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Subcategories",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    def.subcategories.forEach { sub ->
                        SelectablePill(
                            text = sub,
                            selected = sel.selectedSubcategories.contains(sub),
                            onClick = { onToggleSub(sub) }
                        )
                    }
                }
                def.questions.forEachIndexed { qIdx, q ->
                    val qKey = "${service}_$qIdx"
                    val current = sel.questionAnswers[qKey].orEmpty()
                    Spacer(modifier = Modifier.height(8.dp))
                    if (q.isMulti) {
                        Text(
                            text = q.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            q.options.forEach { opt ->
                                val checked = current.contains(opt)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(palette.surfaceVariant.copy(alpha = 0.82f))
                                        .clickable { onSetQuestionAnswer(qKey, opt, true) }
                                        .padding(end = 8.dp)
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { onSetQuestionAnswer(qKey, opt, true) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = palette.primary,
                                            checkmarkColor = Color.White
                                        )
                                    )
                                    Text(opt, style = MaterialTheme.typography.bodySmall, color = palette.textPrimary)
                                }
                            }
                        }
                    } else {
                        ThdrDropdownField(
                            label = q.label,
                            selectedLabel = current.firstOrNull() ?: "Select...",
                            options = listOf("Select..." to "") + q.options.map { it to it },
                            onOptionSelected = { chosenOpt ->
                                onSetQuestionAnswer(qKey, chosenOpt, false)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        ThdrTextField(
            value = sel.scopeNotes,
            onValueChange = onScopeNotesChange,
            label = "Scope notes / exclusions",
            placeholder = "Specific scope, unusual conditions, exclusions...",
            singleLine = false,
            minLines = 2
        )
    }
}
