package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.EstimatorEngine
import com.example.model.DocumentType
import com.example.model.ProposalStatus
import com.example.model.SavedProposal
import com.example.ui.components.SelectablePill
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrDangerButton
import com.example.ui.components.ThdrDoubleField
import com.example.ui.components.ThdrDropdownField
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalThdrPalette
import com.example.viewmodel.EstimatorUiState
import com.example.viewmodel.EstimatorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EstimatorDocumentHeaderSection(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    isWideScreen: Boolean,
    onRequestDeleteSaved: (SavedProposal) -> Unit,
    onOpenSavedList: (DocumentType) -> Unit
) {
    val palette = LocalThdrPalette.current
    val draft = state.draft
    val currency = state.settings.currencySymbol

    ThdrCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (draft.docType == DocumentType.PROPOSAL) "Proposal Builder" else "Estimate Builder",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (draft.docType == DocumentType.PROPOSAL) {
                        "Client-ready quote with validity window, deposit schedule, and terms."
                    } else {
                        "Room-by-room takeoff with Price Book rates and material requirements."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectablePill(
                text = "Estimate Mode",
                selected = draft.docType == DocumentType.ESTIMATE,
                onClick = { viewModel.switchDraftDocumentType(DocumentType.ESTIMATE) },
                testTag = "switch_mode_estimate"
            )
            SelectablePill(
                text = "Proposal Mode",
                selected = draft.docType == DocumentType.PROPOSAL,
                onClick = { viewModel.switchDraftDocumentType(DocumentType.PROPOSAL) },
                testTag = "switch_mode_proposal"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        if (isWideScreen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CreateDocumentTile(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
                SavedDocumentsQuickTile(
                    docType = draft.docType,
                    savedItems = state.savedProposals.filter { it.docType == draft.docType },
                    currencySymbol = currency,
                    onOpen = { viewModel.loadSavedProposal(it) },
                    onRequestDelete = onRequestDeleteSaved,
                    onOpenAll = { onOpenSavedList(draft.docType) },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            CreateDocumentTile(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            SavedDocumentsQuickTile(
                docType = draft.docType,
                savedItems = state.savedProposals.filter { it.docType == draft.docType },
                currencySymbol = currency,
                onOpen = { viewModel.loadSavedProposal(it) },
                onRequestDelete = onRequestDeleteSaved,
                onOpenAll = { onOpenSavedList(draft.docType) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateDocumentTile(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalThdrPalette.current
    val draft = state.draft
    val isProposal = draft.docType == DocumentType.PROPOSAL
    val customerOptions = remember(state.customers) {
        state.customers.map { c ->
            val subtitle = if (c.company.isNotBlank()) "${c.name} (${c.company})" else c.name
            subtitle to c
        }
    }
    val selectedCustomerLabel = remember(draft.customerId, draft.customer, state.customers) {
        state.customers.firstOrNull { it.id == draft.customerId }?.name
            ?: if (customerOptions.isEmpty()) "No saved customers yet" else "Select from Customers directory"
    }

    ThdrSubCard(modifier = modifier) {
        Text(
            text = if (isProposal) "Create Proposal" else "Create Estimate",
            style = MaterialTheme.typography.titleMedium,
            color = palette.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isProposal) "Start a fresh client proposal workspace." else "Start a new estimate with a blank workspace.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(10.dp))
        ThdrPrimaryButton(
            text = if (isProposal) "+ Create Proposal" else "+ Create Estimate",
            onClick = { viewModel.createNewDocument(draft.docType) },
            orangeAccent = isProposal,
            testTag = "create_estimate_button"
        )

        if (draft.detailsVisible) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = palette.outlineSubtle)
            Spacer(modifier = Modifier.height(12.dp))

            if (customerOptions.isNotEmpty()) {
                ThdrDropdownField(
                    label = "Pick Saved Customer",
                    selectedLabel = selectedCustomerLabel,
                    options = customerOptions,
                    onOptionSelected = { viewModel.selectCustomerForDraft(it) },
                    testTag = "pick_saved_customer_dropdown"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            ThdrTextField(
                value = draft.customer,
                onValueChange = viewModel::updateCustomer,
                label = "Customer name",
                placeholder = "Customer name",
                testTag = "customer_name_input"
            )
            if (draft.customer.isNotBlank() && draft.customerId == null) {
                Spacer(modifier = Modifier.height(6.dp))
                ThdrSecondaryButton(
                    text = "Save \"${draft.customer.trim()}\" to Customers",
                    onClick = { viewModel.saveCurrentDraftCustomerToDirectory() },
                    testTag = "save_customer_to_directory_btn",
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
                label = "${draft.docType.title} date (YYYY-MM-DD)",
                placeholder = "2026-11-24",
                testTag = "estimate_date_input"
            )

            if (isProposal) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThdrTextField(
                        value = draft.validUntil,
                        onValueChange = viewModel::updateValidUntil,
                        label = "Valid until (YYYY-MM-DD)",
                        modifier = Modifier.weight(1.2f),
                        testTag = "proposal_valid_until_input"
                    )
                    ThdrDoubleField(
                        value = draft.depositPercentText.toDoubleOrNull() ?: 0.0,
                        onValueChange = { viewModel.updateDepositPercent(it.toString()) },
                        label = "Deposit %",
                        modifier = Modifier.weight(0.8f),
                        testTag = "proposal_deposit_input"
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Proposal Status",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProposalStatus.entries.forEach { st ->
                        SelectablePill(
                            text = st.label,
                            selected = draft.proposalStatus == st,
                            onClick = { viewModel.updateProposalStatus(st) },
                            testTag = "draft_proposal_status_${st.key}"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ThdrTextField(
                    value = draft.termsText,
                    onValueChange = viewModel::updateTermsText,
                    label = "Proposal terms & conditions",
                    singleLine = false,
                    minLines = 2,
                    testTag = "proposal_terms_input"
                )
            }
        }
    }
}

@Composable
private fun SavedDocumentsQuickTile(
    docType: DocumentType,
    savedItems: List<SavedProposal>,
    currencySymbol: String,
    onOpen: (SavedProposal) -> Unit,
    onRequestDelete: (SavedProposal) -> Unit,
    onOpenAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThdrPalette.current
    ThdrSubCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Saved ${docType.pluralTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
                )
                Text(
                    text = "${docType.pluralTitle} saved on this device (${savedItems.size}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )
            }
            if (savedItems.isNotEmpty()) {
                ThdrSecondaryButton(
                    text = "View All",
                    onClick = onOpenAll,
                    testTag = "quick_view_all_saved_btn"
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (savedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, palette.outlineSubtle, RoundedCornerShape(10.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved ${docType.pluralTitle.lowercase()} yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                savedItems.take(3).forEachIndexed { idx, item ->
                    val title = item.project.ifBlank { item.customer }.ifBlank { "${docType.title} ${idx + 1}" }
                    val who = item.customer.ifBlank { "Customer not named" }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.bg.copy(alpha = 0.55f))
                            .border(1.dp, palette.outlineSubtle, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$who · ${item.date}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = EstimatorEngine.formatMoney(item.total, currencySymbol),
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMono),
                                color = palette.brandHighlight
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
