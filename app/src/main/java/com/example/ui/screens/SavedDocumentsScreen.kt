package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.EstimatorEngine
import com.example.model.DocumentType
import com.example.model.ProposalStatus
import com.example.model.SavedProposal
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.SelectablePill
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrDangerButton
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
fun SavedDocumentsScreen(
    docType: DocumentType,
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    onCreateNew: () -> Unit,
    onOpenDocument: (SavedProposal) -> Unit,
    onConvertedToProposal: () -> Unit
) {
    val palette = LocalThdrPalette.current
    val context = LocalContext.current
    val currency = state.settings.currencySymbol
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<ProposalStatus?>(null) }
    var docToDelete by remember { mutableStateOf<SavedProposal?>(null) }

    val filteredDocs = remember(state.savedProposals, docType, searchQuery, statusFilter) {
        val q = searchQuery.trim()
        state.savedProposals
            .filter { it.docType == docType }
            .filter { doc ->
                docType != DocumentType.PROPOSAL || statusFilter == null || doc.status == statusFilter
            }
            .filter { doc ->
                q.isEmpty() ||
                    doc.customer.contains(q, ignoreCase = true) ||
                    doc.project.contains(q, ignoreCase = true) ||
                    doc.date.contains(q, ignoreCase = true)
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
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Saved ${docType.pluralTitle}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = palette.textPrimary
                            )
                            Text(
                                text = if (docType == DocumentType.ESTIMATE) {
                                    "Manage saved estimates, reopen takeoffs, or convert an estimate into a client proposal."
                                } else {
                                    "Track draft, sent, approved, and declined client proposals and export PDFs."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    ThdrPrimaryButton(
                        text = "+ Create ${docType.title}",
                        onClick = onCreateNew,
                        orangeAccent = docType == DocumentType.PROPOSAL,
                        testTag = "saved_docs_create_btn",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    ThdrTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Search ${docType.pluralTitle.lowercase()} by customer, project, or date",
                        testTag = "saved_docs_search"
                    )

                    if (docType == DocumentType.PROPOSAL) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SelectablePill(
                                text = "All",
                                selected = statusFilter == null,
                                onClick = { statusFilter = null },
                                testTag = "proposal_filter_all"
                            )
                            ProposalStatus.entries.forEach { status ->
                                SelectablePill(
                                    text = status.label,
                                    selected = statusFilter == status,
                                    onClick = { statusFilter = status },
                                    testTag = "proposal_filter_${status.key}"
                                )
                            }
                        }
                    }
                }
            }
        }

        if (filteredDocs.isEmpty()) {
            item {
                Box(modifier = Modifier.widthIn(max = 980.dp)) {
                    ThdrCard {
                        ThdrSubCard {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.thdr_mascot_badge),
                                    contentDescription = "House DR Mascot",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(68.dp)
                                )
                                Text(
                                    text = "No saved ${docType.pluralTitle.lowercase()} match your filter yet. Tap \"+ Create ${docType.title}\" above to start one.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textMuted,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredDocs, key = { it.id }) { doc ->
                Box(modifier = Modifier.widthIn(max = 980.dp)) {
                    ThdrCard {
                        val title = doc.project.ifBlank { doc.customer }.ifBlank { "${docType.title} #${doc.id}" }
                        val who = doc.customer.ifBlank { "Customer not specified" }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$who · ${doc.date.ifBlank { "No date" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textMuted
                                )
                            }
                            Spacer(modifier = Modifier.widthIn(min = 8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = EstimatorEngine.formatMoney(doc.total, currency),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = palette.brandHighlight
                                )
                                if (doc.docType == DocumentType.PROPOSAL) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(99.dp),
                                        color = palette.surfaceHigh.copy(alpha = 0.76f),
                                        border = BorderStroke(1.dp, palette.outline)
                                    ) {
                                        Text(
                                            text = doc.status.label.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.brandHighlight,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (doc.docType == DocumentType.PROPOSAL) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Status",
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
                                        selected = doc.status == st,
                                        onClick = { viewModel.updateSavedProposalStatus(doc, st) },
                                        testTag = "doc_status_${doc.id}_${st.key}"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThdrPrimaryButton(
                                text = "Open",
                                onClick = { onOpenDocument(doc) },
                                testTag = "saved_doc_open_${doc.id}",
                                modifier = Modifier.weight(1f)
                            )
                            ThdrSecondaryButton(
                                text = "Print / PDF",
                                onClick = { viewModel.printSavedDocument(context, doc) },
                                testTag = "saved_doc_print_${doc.id}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (doc.docType == DocumentType.ESTIMATE) {
                                ThdrSecondaryButton(
                                    text = "Convert to Proposal",
                                    onClick = {
                                        viewModel.convertSavedEstimateToProposal(doc)
                                        onConvertedToProposal()
                                    },
                                    testTag = "saved_doc_convert_${doc.id}",
                                    modifier = Modifier.weight(1.4f)
                                )
                            }
                            ThdrDangerButton(
                                text = "Delete",
                                onClick = { docToDelete = doc },
                                testTag = "saved_doc_delete_${doc.id}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    docToDelete?.let { target ->
        ConfirmActionDialog(
            title = "Delete ${target.docType.title}?",
            message = "Remove \"${target.project.ifBlank { target.customer }.ifBlank { target.docType.title }}\" permanently from this device?",
            confirmText = "Delete",
            onConfirm = { viewModel.deleteSavedProposal(target.id) },
            onDismiss = { docToDelete = null }
        )
    }
}
