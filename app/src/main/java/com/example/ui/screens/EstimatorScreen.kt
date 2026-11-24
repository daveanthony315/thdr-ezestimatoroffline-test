package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.EstimatorEngine
import com.example.model.EstimateCatalog
import com.example.model.EstimateSide
import com.example.model.SavedProposal
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrDangerButton
import com.example.ui.components.ThdrDoubleField
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.BlueprintBg
import com.example.ui.theme.BlueprintSurface
import com.example.ui.theme.BlueprintSurfaceVariant
import com.example.ui.theme.BrandCobalt
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.OutlineBlue
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.EstimatorUiState
import com.example.viewmodel.EstimatorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EstimatorScreen(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    isWideScreen: Boolean
) {
    val context = LocalContext.current
    var proposalToDelete by remember { mutableStateOf<SavedProposal?>(null) }
    val draft = state.draft
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
                ThdrCard {
                    Text(
                        text = "EZ Estimator",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.estimate_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (isWideScreen) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CreateEstimateTile(
                                state = state,
                                viewModel = viewModel,
                                modifier = Modifier.weight(1f)
                            )
                            SavedProposalsTile(
                                proposals = state.savedProposals,
                                onOpen = { viewModel.loadSavedProposal(it) },
                                onRequestDelete = { proposalToDelete = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        CreateEstimateTile(
                            state = state,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SavedProposalsTile(
                            proposals = state.savedProposals,
                            onOpen = { viewModel.loadSavedProposal(it) },
                            onRequestDelete = { proposalToDelete = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Choose Estimate Area + Add Room / Exterior Area
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = stringResource(R.string.choose_estimate_area),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
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
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = stringResource(R.string.add_room_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
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
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = stringResource(R.string.add_exterior_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
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

        // Room Cards for selected side
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
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.materials_needed_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (state.takeoff.materials.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OutlineSubtle, RoundedCornerShape(12.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.materials_needed_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
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
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = EstimatorEngine.formatQty(qty),
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                                    color = BrandCyan,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Text(
                                    text = "calculated",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            HorizontalDivider(color = OutlineSubtle)
                        }
                    }
                }
            }
        }

        // Estimate Summary
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = stringResource(R.string.estimate_summary_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Total highlight box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(BrandCobalt, BlueprintSurface, Color(0xFF713C20))
                                )
                            )
                            .border(1.dp, OutlineBlue, RoundedCornerShape(15.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.customer_estimate_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = EstimatorEngine.formatMoney(state.takeoff.grandTotal),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Black
                            ),
                            color = BrandCyan,
                            modifier = Modifier.testTag("grand_total_text")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.services_label), color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                EstimatorEngine.formatMoney(state.takeoff.serviceTotal),
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                                modifier = Modifier.testTag("service_total_text")
                            )
                        }
                        HorizontalDivider(color = OutlineSubtle, modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.materials_extra_label), color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                EstimatorEngine.formatMoney(0.0),
                                color = TextPrimary,
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
                            label = stringResource(R.string.discount_label),
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
                            "$k ${EstimatorEngine.formatMoney(v)}"
                        }
                    }
                    Text(
                        text = "Service pricing breakdown: $breakdownText",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "INTERNAL ONLY · Estimated job cost: ${EstimatorEngine.formatMoney(state.takeoff.jobCost)} · Gross profit: ${EstimatorEngine.formatMoney(state.takeoff.grossProfit)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextMuted,
                        modifier = Modifier.testTag("internal_cost_profit_text")
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThdrPrimaryButton(
                            text = stringResource(R.string.btn_save_estimate),
                            onClick = { viewModel.saveCurrentEstimate() },
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
                            color = BrandCyan,
                            modifier = Modifier.testTag("save_message_text")
                        )
                    }
                }
            }
        }
    }

    proposalToDelete?.let { target ->
        ConfirmActionDialog(
            title = "Delete Saved Proposal?",
            message = "Remove \"${target.project.ifBlank { target.customer }.ifBlank { "Proposal" }}\" permanently from this device?",
            confirmText = "Delete",
            onConfirm = { viewModel.deleteSavedProposal(target.id) },
            onDismiss = { proposalToDelete = null }
        )
    }
}

@Composable
private fun CreateEstimateTile(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    modifier: Modifier = Modifier
) {
    val draft = state.draft
    ThdrSubCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.create_estimate_title),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.create_estimate_desc),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(10.dp))
        ThdrPrimaryButton(
            text = stringResource(R.string.btn_create_estimate),
            onClick = { viewModel.createNewEstimate() },
            testTag = "create_estimate_button"
        )

        if (draft.detailsVisible) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = OutlineSubtle)
            Spacer(modifier = Modifier.height(12.dp))
            ThdrTextField(
                value = draft.customer,
                onValueChange = viewModel::updateCustomer,
                label = "Customer name",
                placeholder = "Customer name",
                testTag = "customer_name_input"
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThdrTextField(
                value = draft.project,
                onValueChange = viewModel::updateProject,
                label = "Project name",
                placeholder = "Project / address",
                testTag = "project_name_input"
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThdrTextField(
                value = draft.date,
                onValueChange = viewModel::updateDate,
                label = "Estimate date (YYYY-MM-DD)",
                placeholder = "2026-11-24",
                testTag = "estimate_date_input"
            )
        }
    }
}

@Composable
private fun SavedProposalsTile(
    proposals: List<SavedProposal>,
    onOpen: (SavedProposal) -> Unit,
    onRequestDelete: (SavedProposal) -> Unit,
    modifier: Modifier = Modifier
) {
    ThdrSubCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.saved_proposals_title),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.saved_proposals_desc),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (proposals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineSubtle, RoundedCornerShape(10.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_saved_proposals),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                proposals.forEachIndexed { idx, item ->
                    val title = item.project.ifBlank { item.customer }.ifBlank { "Proposal ${idx + 1}" }
                    val who = item.customer.ifBlank { "Customer not named" }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BlueprintBg.copy(alpha = 0.55f))
                            .border(1.dp, OutlineSubtle, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$who · ${item.date}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = EstimatorEngine.formatMoney(item.total),
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMono),
                                color = BrandCyan
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ThdrSecondaryButton(
                                text = "Open",
                                onClick = { onOpen(item) },
                                testTag = "open_proposal_${item.id}"
                            )
                            ThdrDangerButton(
                                text = "Delete",
                                onClick = { onRequestDelete(item) },
                                testTag = "delete_proposal_${item.id}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SideToggleButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) AccentOrange.copy(alpha = 0.24f) else BlueprintSurfaceVariant,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) AccentOrange else OutlineBlue
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
                color = TextPrimary
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
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0x18FFFFFF),
        border = BorderStroke(1.dp, OutlineBlue),
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
                color = TextPrimary
            )
        }
    }
}
