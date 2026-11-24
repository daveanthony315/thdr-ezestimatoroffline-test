package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.ConstructionMaterialCalculator
import com.example.model.MaterialCalcKind
import com.example.ui.components.ThdrDoubleField
import com.example.ui.components.ThdrDropdownField
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.TextMuted
import com.example.viewmodel.EstimatorViewModel

@Composable
fun MaterialCalculatorsSection(
    viewModel: EstimatorViewModel,
    isWideScreen: Boolean
) {
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
    var floorPattern by remember {
        mutableStateOf(ConstructionMaterialCalculator.FlooringPattern.STANDARD_INLINE)
    }

    var roofL by remember { mutableDoubleStateOf(0.0) }
    var roofW by remember { mutableDoubleStateOf(0.0) }
    var roofPitch by remember { mutableDoubleStateOf(1.0) }

    val panels: List<@Composable () -> Unit> = listOf(
        {
            CalculatorPanel(
                title = MaterialCalcKind.FLOORING.title,
                note = "Floor area = Length × Width. Applies standard waste factors (10% inline, 15% diagonal, 20% herringbone).",
                buttonText = "Calculate flooring",
                testTag = "calc_flooring_btn",
                onCalculate = {
                    viewModel.runMaterialCalculator(
                        MaterialCalcKind.FLOORING,
                        floorArea,
                        1.0 + floorPattern.wasteFactor
                    )
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThdrDoubleField(floorArea, { floorArea = it }, "Net floor area (SF)", testTag = "floor_area")
                    ThdrDropdownField(
                        label = "Material / pattern",
                        selectedLabel = floorPattern.label,
                        options = ConstructionMaterialCalculator.FlooringPattern.entries.map { it.label to it },
                        onOptionSelected = { floorPattern = it }
                    )
                }
            }
        },
        {
            CalculatorPanel(
                title = MaterialCalcKind.PAINT.title,
                note = "Deductions: 15 SF/window & 20 SF/door. Coverage: 350 SF/gal/coat plus optional 10% cut-in & touch-up waste.",
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
                title = MaterialCalcKind.ROOFING.title,
                note = "Roofing squares = Roof area ÷ 100 (10×10 sq ft per square), plus 10% waste and 3 bundles per square.",
                buttonText = "Calculate roofing",
                testTag = "calc_roofing_btn",
                onCalculate = {
                    viewModel.runMaterialCalculator(MaterialCalcKind.ROOFING, roofL, roofW, roofPitch)
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThdrDoubleField(roofL, { roofL = it }, "Length (ft)", Modifier.weight(1f), "roof_l")
                        ThdrDoubleField(roofW, { roofW = it }, "Width (ft)", Modifier.weight(1f), "roof_w")
                    }
                    val pitchOptions = listOf(
                        "Flat / footprint (1.00×)" to 1.00,
                        "4/12 pitch (1.05×)" to 1.05,
                        "6/12 pitch (1.12×)" to 1.12,
                        "8/12 pitch (1.20×)" to 1.20,
                        "12/12 pitch (1.41×)" to 1.41
                    )
                    ThdrDropdownField(
                        label = "Pitch multiplier",
                        selectedLabel = pitchOptions.firstOrNull { it.second == roofPitch }?.first ?: "Custom (${roofPitch}×)",
                        options = pitchOptions,
                        onOptionSelected = { roofPitch = it }
                    )
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
