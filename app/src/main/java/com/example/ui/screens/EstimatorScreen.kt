package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.EstimatorEngine
import com.example.model.DocumentType
import com.example.model.EstimateCatalog
import com.example.model.EstimateSide
import com.example.model.MascotSlotChoice
import com.example.model.SavedProposal
import com.example.ui.components.BrandingImage
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrDoubleField
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalThdrPalette
import com.example.viewmodel.EstimatorUiState
import com.example.viewmodel.EstimatorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EstimatorScreen(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    isWideScreen: Boolean,
    onOpenSavedList: (DocumentType) -> Unit = {}
) {
    val palette = LocalThdrPalette.current
    val context = LocalContext.current
    var proposalToDelete by remember { mutableStateOf<SavedProposal?>(null) }
    val draft = state.draft
    val currency = state.settings.currencySymbol
    val visibleRooms = remember(draft.rooms, draft.estimateSide) {
        draft.rooms.withIndex().filter { it.value.side == draft.estimateSide }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                EstimatorDocumentHeaderSection(
                    state = state,
                    viewModel = viewModel,
                    isWideScreen = isWideScreen,
                    onRequestDeleteSaved = { proposalToDelete = it },
                    onOpenSavedList = onOpenSavedList
                )
            }
        }

        // Choose Estimate Area + Add Room / Exterior Area
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = stringResource(R.string.choose_estimate_area),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SideToggleButton(
                            label = "INTERIOR",
                            selected = draft.estimateSide == EstimateSide.INTERIOR,
                            onClick = { viewModel.setEstimateSide(EstimateSide.INTERIOR) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("interior_side_btn")
                        )
                        SideToggleButton(
                            label = "EXTERIOR",
                            selected = draft.estimateSide == EstimateSide.EXTERIOR,
                            onClick = { viewModel.setEstimateSide(EstimateSide.EXTERIOR) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("exterior_side_btn")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    if (draft.estimateSide == EstimateSide.INTERIOR) {
                        Text(
                            text = stringResource(R.string.add_a_room),
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = stringResource(R.string.add_room_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EstimateCatalog.INTERIOR_ROOM_TYPES.forEach { roomType ->
                                AreaAddButton(
                                    label = roomType,
                                    onClick = { viewModel.addRoomType(roomType) },
                                    testTag = "add_room_$roomType"
                                )
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.add_exterior_area),
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = stringResource(R.string.add_exterior_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EstimateCatalog.EXTERIOR_AREA_TYPES.forEach { areaType ->
                                AreaAddButton(
                                    label = areaType,
                                    onClick = { viewModel.addExteriorArea(areaType) },
                                    testTag = "add_exterior_$areaType"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Room Cards for selected side (or mascot prompt when empty)
        if (visibleRooms.isEmpty()) {
            item {
                Box(modifier = Modifier.widthIn(max = 980.dp)) {
                    ThdrCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (state.settings.mascotChoice != MascotSlotChoice.HIDDEN) {
                                val fallbackRes = when (state.settings.mascotChoice) {
                                    MascotSlotChoice.COMPANY_EMBLEM -> R.drawable.thdr_logo
                                    MascotSlotChoice.STANDING_MASCOT -> R.drawable.thdr_mascot_avatar
                                    else -> R.drawable.thdr_mascot_badge
                                }
                                val customPath = if (state.settings.mascotChoice == MascotSlotChoice.CUSTOM) {
                                    state.settings.customMascotPath
                                } else ""
                                BrandingImage(
                                    customPath = customPath,
                                    fallbackResId = fallbackRes,
                                    contentDescription = "Company Estimator Badge",
                                    contentScale = ContentScale.Fit,
                                    maxDecodePx = 256,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "No ${draft.estimateSide.label.lowercase()} areas added yet",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = "Select a room or exterior area button above to enter dimensions, services, and materials.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }
        itemsIndexed(visibleRooms, key = { _, pair -> pair.value.id }) { _, indexedRoom ->
            val originalIndex = indexedRoom.index
            val room = indexedRoom.value
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                RoomEditorCard(
                    roomIndex = originalIndex,
                    room = room,
                    priceBook = state.priceBookItems,
                    onToggleExpand = { viewModel.toggleRoomExpanded(room.id) },
                    onRemove = { viewModel.removeRoom(room.id) },
                    onNameChange = { viewModel.updateRoomName(room.id, it) },
                    onDimChange = { l, w, h -> viewModel.updateRoomDimensions(room.id, l, w, h) },
                    onFenceLfChange = { viewModel.updateRoomFenceLength(room.id, it) },
                    onOpeningsChange = { d, w -> viewModel.updateRoomOpenings(room.id, d, w) },
                    onConditionChange = { viewModel.updateRoomCondition(room.id, it) },
                    onRoomNotesChange = { viewModel.updateRoomNotes(room.id, it) },
                    onToggleService = { viewModel.toggleRoomService(room.id, it) },
                    onSelectPbItem = { svc, pbItem -> viewModel.selectServicePbItem(room.id, svc, pbItem) },
                    onSelectPricingUnit = { svc, unit -> viewModel.updateServicePricingUnit(room.id, svc, unit) },
                    onSelectMeasurementBasis = { svc, basis -> viewModel.updateServiceMeasurementBasis(room.id, svc, basis) },
                    onServiceQtyChange = { svc, qty -> viewModel.updateServiceQty(room.id, svc, qty) },
                    onResetQty = { svc -> viewModel.resetServiceQtyToMeasurement(room.id, svc) },
                    onServiceRateChange = { svc, rate -> viewModel.updateServiceRate(room.id, svc, rate) },
                    onServiceScopeNotesChange = { svc, notes -> viewModel.updateServiceScopeNotes(room.id, svc, notes) },
                    onToggleServiceSub = { svc, sub -> viewModel.toggleServiceSubcategory(room.id, svc, sub) },
                    onSetQuestionAnswer = { qKey, svc, opt, isMulti ->
                        viewModel.setServiceQuestionAnswer(room.id, qKey, svc, opt, isMulti)
                    }
                )
            }
        }

        // Materials Needed — Entire Project
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = stringResource(R.string.materials_needed_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.materials_needed_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (state.takeoff.materials.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, palette.outlineSubtle, RoundedCornerShape(12.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.materials_needed_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        state.takeoff.materials.entries.forEach { (name, qty) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
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
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Text(
                                    text = "calculated",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textMuted
                                )
                            }
                            HorizontalDivider(color = palette.outlineSubtle)
                        }
                    }
                }
            }
        }

        // Estimate / Proposal Summary
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    val isProposal = draft.docType == DocumentType.PROPOSAL
                    Text(
                        text = if (isProposal) "Proposal Summary" else stringResource(R.string.estimate_summary_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        palette.primary.copy(alpha = 0.90f),
                                        palette.primaryDeep.copy(alpha = 0.92f)
                                    )
                                )
                            )
                            .border(
                                width = if (isProposal) 1.5.dp else 1.dp,
                                color = if (isProposal) palette.accent.copy(alpha = 0.80f) else palette.outline,
                                shape = RoundedCornerShape(15.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (isProposal) "CLIENT PROPOSAL TOTAL" else stringResource(R.string.customer_estimate_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = EstimatorEngine.formatMoney(state.takeoff.grandTotal, currency),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Black
                            ),
                            color = Color.White,
                            modifier = Modifier.testTag("grand_total_text")
                        )
                        if (isProposal) {
                            val depPct = (draft.depositPercentText.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
                            if (depPct > 0.0) {
                                val depAmt = state.takeoff.grandTotal * (depPct / 100.0)
                                Text(
                                    text = "Deposit Due (${EstimatorEngine.formatQty(depPct)}%): ${EstimatorEngine.formatMoney(depAmt, currency)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.92f),
                                    modifier = Modifier.testTag("proposal_deposit_due_text")
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.services_label), color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                EstimatorEngine.formatMoney(state.takeoff.serviceTotal, currency),
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                                modifier = Modifier.testTag("service_total_text")
                            )
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.materials_extra_label), color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                EstimatorEngine.formatMoney(0.0, currency),
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThdrDoubleField(
                            value = draft.discountText.toDoubleOrNull() ?: 0.0,
                            onValueChange = { viewModel.updateDiscount(it.toString()) },
                            label = "Discount ($currency)",
                            modifier = Modifier.weight(1f),
                            testTag = "discount_input"
                        )
                        ThdrDoubleField(
                            value = draft.taxText.toDoubleOrNull() ?: 0.0,
                            onValueChange = { viewModel.updateTax(it.toString()) },
                            label = stringResource(R.string.tax_label),
                            modifier = Modifier.weight(1f),
                            testTag = "tax_input"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val breakdownText = if (state.takeoff.serviceBreakdown.isEmpty()) {
                        "No services selected."
                    } else {
                        state.takeoff.serviceBreakdown.entries.joinToString(" · ") { (k, v) ->
                            "$k ${EstimatorEngine.formatMoney(v, currency)}"
                        }
                    }
                    Text(
                        text = "Service pricing breakdown: $breakdownText",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    if (state.settings.showInternalCostProfit) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "INTERNAL ONLY · Estimated job cost: ${EstimatorEngine.formatMoney(state.takeoff.jobCost, currency)} · Gross profit: ${EstimatorEngine.formatMoney(state.takeoff.grossProfit, currency)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = palette.textMuted,
                            modifier = Modifier.testTag("internal_cost_profit_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThdrPrimaryButton(
                            text = if (isProposal) "Save Proposal" else stringResource(R.string.btn_save_estimate),
                            onClick = { viewModel.saveCurrentEstimate() },
                            orangeAccent = isProposal,
                            modifier = Modifier.weight(1f),
                            testTag = "save_estimate_button"
                        )
                        ThdrSecondaryButton(
                            text = stringResource(R.string.btn_print_pdf),
                            onClick = { viewModel.printCurrentEstimate(context) },
                            modifier = Modifier.weight(1f),
                            testTag = "print_estimate_button"
                        )
                    }

                    if (state.saveMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.saveMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.brandHighlight,
                            modifier = Modifier.testTag("save_message_text")
                        )
                    }
                }
            }
        }
    }

    proposalToDelete?.let { target ->
        ConfirmActionDialog(
            title = "Delete Saved ${target.docType.title}?",
            message = "Remove \"${target.project.ifBlank { target.customer }.ifBlank { target.docType.title }}\" permanently from this device?",
            confirmText = "Delete",
            onConfirm = { viewModel.deleteSavedProposal(target.id) },
            onDismiss = { proposalToDelete = null }
        )
    }
}

@Composable
private fun SideToggleButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThdrPalette.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) palette.primary.copy(alpha = 0.88f) else palette.surfaceVariant.copy(alpha = 0.80f),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) palette.brandHighlight else palette.outline
        ),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = if (selected) Color.White else palette.textPrimary
            )
        }
    }
}

@Composable
private fun AreaAddButton(
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    val palette = LocalThdrPalette.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = palette.surfaceHigh.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, palette.outline),
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = palette.textPrimary
            )
        }
    }
}
