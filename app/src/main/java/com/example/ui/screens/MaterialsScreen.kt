package com.example.ui.screens

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.EstimatorEngine
import com.example.model.MaterialCalcKind
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrDoubleField
import com.example.ui.components.ThdrDropdownField
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.EstimatorUiState
import com.example.viewmodel.EstimatorViewModel

@Composable
fun MaterialsScreen(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    isWideScreen: Boolean
) {
    var newMatName by remember { mutableStateOf("") }
    var newMatQty by remember { mutableDoubleStateOf(0.0) }

    // V7 calculator states
    var concL by remember { mutableDoubleStateOf(0.0) }
    var concW by remember { mutableDoubleStateOf(0.0) }
    var concT by remember { mutableDoubleStateOf(6.0) }

    var cmuL by remember { mutableDoubleStateOf(0.0) }
    var cmuH by remember { mutableDoubleStateOf(0.0) }

    var frameL by remember { mutableDoubleStateOf(0.0) }
    var frameH by remember { mutableDoubleStateOf(8.0) }

    var dryWall by remember { mutableDoubleStateOf(0.0) }
    var dryCeil by remember { mutableDoubleStateOf(0.0) }
    var drySize by remember { mutableDoubleStateOf(32.0) }

    var paintArea by remember { mutableDoubleStateOf(0.0) }
    var paintWindows by remember { mutableDoubleStateOf(0.0) }
    var paintDoors by remember { mutableDoubleStateOf(0.0) }
    var paintCoats by remember { mutableDoubleStateOf(2.0) }

    var floorArea by remember { mutableDoubleStateOf(0.0) }
    var floorPatternLabel by remember { mutableStateOf("Standard inline — 10% waste") }
    var floorPatternFactor by remember { mutableDoubleStateOf(1.10) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Consolidated Project Takeoff
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "Materials",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.materials_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val allMaterials = state.takeoff.materials
                    if (allMaterials.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OutlineSubtle, RoundedCornerShape(12.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Materials will appear after services and measurements are entered.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        allMaterials.entries.forEach { (name, qty) ->
                            val isManual = state.draft.manualMaterials.containsKey(name)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = EstimatorEngine.formatQty(qty),
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                                    color = BrandCyan,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                Text(
                                    text = "qty",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                if (isManual) {
                                    IconButton(
                                        onClick = { viewModel.removeManualMaterial(name) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove custom material",
                                            tint = TextMuted
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = OutlineSubtle)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThdrTextField(
                            value = newMatName,
                            onValueChange = { newMatName = it },
                            label = "Add material name",
                            modifier = Modifier.weight(1.8f),
                            testTag = "manual_material_name"
                        )
                        ThdrDoubleField(
                            value = newMatQty,
                            onValueChange = { newMatQty = it },
                            label = "Qty",
                            modifier = Modifier.weight(0.9f),
                            testTag = "manual_material_qty"
                        )
                        ThdrPrimaryButton(
                            text = "Add",
                            onClick = {
                                if (newMatName.isNotBlank() && newMatQty > 0.0) {
                                    viewModel.addManualMaterial(newMatName, newMatQty)
                                    newMatName = ""
                                    newMatQty = 0.0
                                }
                            },
                            testTag = "add_manual_material_btn"
                        )
                    }
                }
            }
        }

        // Material Calculation Tools
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = stringResource(R.string.calc_tools_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.calc_tools_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val panels: List<@Composable () -> Unit> = listOf(
                        {
                            CalculatorPanel(
                                title = MaterialCalcKind.CONCRETE.title,
                                note = "Uses 10% waste. Bag yields: 45 × 60-lb bags or 34 × 80-lb bags per cubic yard.",
                                buttonText = "Calculate concrete",
                                testTag = "calc_concrete_btn",
                                onCalculate = {
                                    viewModel.runMaterialCalculator(MaterialCalcKind.CONCRETE, concL, concW, concT)
                                }
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ThdrDoubleField(concL, { concL = it }, "Length (ft)", Modifier.weight(1f), "conc_l")
                                    ThdrDoubleField(concW, { concW = it }, "Width (ft)", Modifier.weight(1f), "conc_w")
                                    ThdrDoubleField(concT, { concT = it }, "Thick (in)", Modifier.weight(1f), "conc_t")
                                }
                            }
                        },
                        {
                            CalculatorPanel(
                                title = MaterialCalcKind.CMU.title,
                                note = "Formula: wall area ÷ 0.89 × 1.05. Assumes standard 8 × 8 × 16 in CMU.",
                                buttonText = "Calculate CMU",
                                testTag = "calc_cmu_btn",
                                onCalculate = {
                                    viewModel.runMaterialCalculator(MaterialCalcKind.CMU, cmuL, cmuH)
                                }
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ThdrDoubleField(cmuL, { cmuL = it }, "Wall length (ft)", Modifier.weight(1f), "cmu_l")
                                    ThdrDoubleField(cmuH, { cmuH = it }, "Wall height (ft)", Modifier.weight(1f), "cmu_h")
                                }
                            }
                        },
                        {
                            CalculatorPanel(
                                title = MaterialCalcKind.FRAMING.title,
                                note = "Assumes 16 in. O.C.: (wall LF ÷ 1.33 + 1) × 1.15. Plate quantity assumes three continuous runs.",
                                buttonText = "Calculate framing",
                                testTag = "calc_framing_btn",
                                onCalculate = {
                                    viewModel.runMaterialCalculator(MaterialCalcKind.FRAMING, frameL, frameH)
                                }
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ThdrDoubleField(frameL, { frameL = it }, "Wall length (LF)", Modifier.weight(1f), "frame_l")
                                    ThdrDoubleField(frameH, { frameH = it }, "Wall height (ft)", Modifier.weight(1f), "frame_h")
                                }
                            }
                        },
                        {
                            CalculatorPanel(
                                title = MaterialCalcKind.DRYWALL.title,
                                note = "Uses the 10% allowance for cutoffs and seam trimming.",
                                buttonText = "Calculate drywall",
                                testTag = "calc_drywall_btn",
                                onCalculate = {
                                    viewModel.runMaterialCalculator(MaterialCalcKind.DRYWALL, dryWall, dryCeil, drySize)
                                }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ThdrDoubleField(dryWall, { dryWall = it }, "Wall (SF)", Modifier.weight(1f), "dry_wall")
                                        ThdrDoubleField(dryCeil, { dryCeil = it }, "Ceiling (SF)", Modifier.weight(1f), "dry_ceil")
                                    }
                                    ThdrDropdownField(
                                        label = "Sheet size",
                                        selectedLabel = if (drySize == 48.0) "4 × 12 (48 SF)" else "4 × 8 (32 SF)",
                                        options = listOf("4 × 8 (32 SF)" to 32.0, "4 × 12 (48 SF)" to 48.0),
                                        onOptionSelected = { drySize = it }
                                    )
                                }
                            }
                        },
                        {
                            CalculatorPanel(
                                title = MaterialCalcKind.PAINT.title,
                                note = "Deductions: approx. 15 SF per standard window and 20 SF per entry door. Coverage: 350 SF per gallon per coat.",
                                buttonText = "Calculate paint",
                                testTag = "calc_paint_btn",
                                onCalculate = {
                                    viewModel.runMaterialCalculator(
                                        MaterialCalcKind.PAINT,
                                        paintArea, paintWindows, paintDoors, paintCoats
                                    )
                                }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ThdrDoubleField(paintArea, { paintArea = it }, "Surface (SF)", Modifier.weight(1f), "paint_area")
                                        ThdrDoubleField(paintCoats, { paintCoats = it }, "Coats", Modifier.weight(1f), "paint_coats", isInteger = true)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ThdrDoubleField(paintWindows, { paintWindows = it }, "Windows", Modifier.weight(1f), "paint_win", isInteger = true)
                                        ThdrDoubleField(paintDoors, { paintDoors = it }, "Doors", Modifier.weight(1f), "paint_doors", isInteger = true)
                                    }
                                }
                            }
                        },
                        {
                            CalculatorPanel(
                                title = MaterialCalcKind.FLOORING.title,
                                note = "Uses pattern modifiers. Select the closest installation pattern.",
                                buttonText = "Calculate flooring",
                                testTag = "calc_flooring_btn",
                                onCalculate = {
                                    viewModel.runMaterialCalculator(MaterialCalcKind.FLOORING, floorArea, floorPatternFactor)
                                }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ThdrDoubleField(floorArea, { floorArea = it }, "Net floor area (SF)", testTag = "floor_area")
                                    val patterns = listOf(
                                        "Standard inline — 10% waste" to 1.10,
                                        "Carpet / vinyl roll goods — 10% allowance" to 1.10,
                                        "Diagonal / intricate — 15% waste" to 1.15,
                                        "Complex herringbone / chevron — 20% waste" to 1.20
                                    )
                                    ThdrDropdownField(
                                        label = "Material / pattern",
                                        selectedLabel = floorPatternLabel,
                                        options = patterns.map { it.first to it },
                                        onOptionSelected = { (lbl, fac) ->
                                            floorPatternLabel = lbl
                                            floorPatternFactor = fac
                                        }
                                    )
                                }
                            }
                        }
                    )

                    if (isWideScreen) {
                        panels.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) { pair[0]() }
                                Box(modifier = Modifier.weight(1f)) {
                                    if (pair.size > 1) pair[1]() else Spacer(Modifier)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            panels.forEach { panel -> panel() }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = OutlineSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Calculator Results",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThdrSecondaryButton(
                            text = "Add to project materials",
                            onClick = { viewModel.addCalcResultsToProjectMaterials() },
                            modifier = Modifier.weight(1f),
                            testTag = "add_calc_results_btn"
                        )
                        ThdrSecondaryButton(
                            text = "Clear results",
                            onClick = { viewModel.clearCalcResults() },
                            modifier = Modifier.weight(1f),
                            testTag = "clear_calc_results_btn"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    if (state.calcResults.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OutlineSubtle, RoundedCornerShape(12.dp))
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Run a calculator to see material quantities here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    } else {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Material / takeoff", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.weight(2f))
                                Text("Qty", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                                Text("Unit", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = OutlineSubtle)
                            state.calcResults.forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = row.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary,
                                        modifier = Modifier.weight(2f)
                                    )
                                    Text(
                                        text = row.displayQty,
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                                        color = BrandCyan,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.7f)
                                    )
                                    Text(
                                        text = row.unit,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.7f)
                                    )
                                }
                                HorizontalDivider(color = OutlineSubtle)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.calc_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculatorPanel(
    title: String,
    note: String,
    buttonText: String,
    testTag: String,
    onCalculate: () -> Unit,
    inputs: @Composable () -> Unit
) {
    ThdrSubCard {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = BrandCyan
        )
        Spacer(modifier = Modifier.height(8.dp))
        inputs()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = note,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        ThdrPrimaryButton(
            text = buttonText,
            onClick = onCalculate,
            modifier = Modifier.fillMaxWidth(),
            testTag = testTag
        )
    }
}
