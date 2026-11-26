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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.EstimatorEngine
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrDoubleField
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalThdrPalette
import com.example.viewmodel.EstimatorUiState
import com.example.viewmodel.EstimatorViewModel

@Composable
fun MaterialsScreen(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    isWideScreen: Boolean
) {
    val palette = LocalThdrPalette.current
    var newMatName by remember { mutableStateOf("") }
    var newMatQty by remember { mutableDoubleStateOf(0.0) }

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
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.materials_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val allMaterials = state.takeoff.materials
                    if (allMaterials.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, palette.outlineSubtle, RoundedCornerShape(12.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Materials will appear after services and measurements are entered.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMuted,
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
                                    color = palette.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = EstimatorEngine.formatQty(qty),
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                                    color = palette.brandHighlight,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                Text(
                                    text = "qty",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textMuted
                                )
                                if (isManual) {
                                    IconButton(
                                        onClick = { viewModel.removeManualMaterial(name) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove custom material",
                                            tint = palette.textMuted
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = palette.outlineSubtle)
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
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.calc_tools_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ThdrSubCard {
                        Text(
                            text = "Measurement Formulas Reference",
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.brandHighlight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val formulas = listOf(
                            "Floor area" to "Length × Width",
                            "Ceiling area" to "Length × Width",
                            "Wall area" to "(Length + Width) × 2 (× Height for wall SF)",
                            "Linear footage" to "Sum of measured lengths + (Width × 2)",
                            "Roofing squares" to "Roof area ÷ 100 (10×10 sqft / 100²ft)"
                        )
                        formulas.forEach { (name, formula) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, style = MaterialTheme.typography.bodySmall, color = palette.textPrimary)
                                Text(
                                    formula,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                                    color = palette.brandHighlight,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    MaterialCalculatorsSection(viewModel = viewModel, isWideScreen = isWideScreen)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = palette.outlineSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Calculator Results",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
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
                                .border(1.dp, palette.outlineSubtle, RoundedCornerShape(12.dp))
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Run a calculator to see material quantities here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMuted
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
                                Text("Material / takeoff", style = MaterialTheme.typography.labelSmall, color = palette.textMuted, modifier = Modifier.weight(2f))
                                Text("Qty", style = MaterialTheme.typography.labelSmall, color = palette.textMuted, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                                Text("Unit", style = MaterialTheme.typography.labelSmall, color = palette.textMuted, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = palette.outlineSubtle)
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
                                        color = palette.textPrimary,
                                        modifier = Modifier.weight(2f)
                                    )
                                    Text(
                                        text = row.displayQty,
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                                        color = palette.brandHighlight,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.7f)
                                    )
                                    Text(
                                        text = row.unit,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textMuted,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.7f)
                                    )
                                }
                                HorizontalDivider(color = palette.outlineSubtle)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.calc_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
            }
        }
    }
}
