package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.EstimatorEngine
import com.example.model.DocumentType
import com.example.model.SavedProposal
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalThdrPalette
import com.example.viewmodel.EstimatorUiState

@Composable
fun HomeScreen(
    state: EstimatorUiState,
    isWideScreen: Boolean,
    onCreateEstimate: () -> Unit,
    onOpenSavedEstimates: () -> Unit,
    onCreateProposal: () -> Unit,
    onOpenSavedProposals: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenPriceBook: () -> Unit,
    onOpenMaterials: () -> Unit,
    onResumeActiveDraft: () -> Unit,
    onOpenSavedDocument: (SavedProposal) -> Unit
) {
    val palette = LocalThdrPalette.current
    val currency = state.settings.currencySymbol
    val estimatesCount = remember(state.savedProposals) {
        state.savedProposals.count { it.docType == DocumentType.ESTIMATE }
    }
    val proposalsCount = remember(state.savedProposals) {
        state.savedProposals.count { it.docType == DocumentType.PROPOSAL }
    }
    val hasActiveDraft = state.draft.rooms.isNotEmpty() ||
        state.draft.id != null ||
        state.draft.customer.isNotBlank() ||
        state.draft.project.isNotBlank()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Dashboard Banner
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (palette.isDark) Color.Transparent else Color(0xFF101827))
                                    .padding(
                                        horizontal = if (palette.isDark) 0.dp else 10.dp,
                                        vertical = if (palette.isDark) 0.dp else 6.dp
                                    )
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.thdr_brand_logo),
                                    contentDescription = "The Home Improvement House DR Logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .height(52.dp)
                                        .widthIn(max = 170.dp)
                                        .testTag("home_brand_logo")
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.settings.companyName.ifBlank { "EZ ESTIMATOR" },
                                style = MaterialTheme.typography.headlineMedium,
                                color = palette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.settings.companyTagline.ifBlank { "HOUSE DR · INTERIOR / EXTERIOR" },
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.brandHighlight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create room-by-room estimates, client-ready proposals, and material takeoffs powered by your Price Book.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Image(
                            painter = painterResource(id = R.drawable.thdr_mascot_avatar),
                            contentDescription = "House DR Estimator Mascot",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(136.dp)
                                .width(82.dp)
                                .testTag("home_mascot_avatar")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTile(
                            label = "Estimates",
                            value = estimatesCount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "Proposals",
                            value = proposalsCount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "Customers",
                            value = state.customers.size.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "Price Book",
                            value = state.priceBookItems.size.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (hasActiveDraft) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.primary.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, palette.brandHighlight.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onResumeActiveDraft)
                                .testTag("home_resume_draft_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Resume Active ${state.draft.docType.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = palette.textPrimary
                                    )
                                    val draftLabel = listOfNotNull(
                                        state.draft.project.takeIf { it.isNotBlank() },
                                        state.draft.customer.takeIf { it.isNotBlank() },
                                        "${state.draft.rooms.size} area(s)"
                                    ).joinToString(" · ")
                                    Text(
                                        text = "$draftLabel · ${EstimatorEngine.formatMoney(state.takeoff.grandTotal, currency)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.brandHighlight,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume draft",
                                    tint = palette.brandHighlight
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Workflow Cards: Estimate (Create / Saved) & Proposal (Create / Saved)
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                if (isWideScreen) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DocumentWorkflowCard(
                            title = "Estimate",
                            subtitle = "Job takeoffs, room measurements, service rates & internal cost/profit.",
                            icon = Icons.Default.Calculate,
                            savedCount = estimatesCount,
                            createText = "Create",
                            savedText = "Saved ($estimatesCount)",
                            onCreate = onCreateEstimate,
                            onSaved = onOpenSavedEstimates,
                            createTag = "home_estimate_create_btn",
                            savedTag = "home_estimate_saved_btn",
                            useAccentGradient = false,
                            modifier = Modifier.weight(1f)
                        )
                        DocumentWorkflowCard(
                            title = "Proposal",
                            subtitle = "Client-ready quotes with valid-until date, deposit %, terms & approval status.",
                            icon = Icons.Default.Description,
                            savedCount = proposalsCount,
                            createText = "Create",
                            savedText = "Saved ($proposalsCount)",
                            onCreate = onCreateProposal,
                            onSaved = onOpenSavedProposals,
                            createTag = "home_proposal_create_btn",
                            savedTag = "home_proposal_saved_btn",
                            useAccentGradient = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DocumentWorkflowCard(
                            title = "Estimate",
                            subtitle = "Job takeoffs, room measurements, service rates & internal cost/profit.",
                            icon = Icons.Default.Calculate,
                            savedCount = estimatesCount,
                            createText = "Create",
                            savedText = "Saved ($estimatesCount)",
                            onCreate = onCreateEstimate,
                            onSaved = onOpenSavedEstimates,
                            createTag = "home_estimate_create_btn",
                            savedTag = "home_estimate_saved_btn",
                            useAccentGradient = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DocumentWorkflowCard(
                            title = "Proposal",
                            subtitle = "Client-ready quotes with valid-until date, deposit %, terms & approval status.",
                            icon = Icons.Default.Description,
                            savedCount = proposalsCount,
                            createText = "Create",
                            savedText = "Saved ($proposalsCount)",
                            onCreate = onCreateProposal,
                            onSaved = onOpenSavedProposals,
                            createTag = "home_proposal_create_btn",
                            savedTag = "home_proposal_saved_btn",
                            useAccentGradient = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Core Management Grid: Customers, Price Book, Settings, Materials
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "Workspace & Tools",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardActionTile(
                            title = "Customers",
                            subtitle = "${state.customers.size} saved",
                            icon = Icons.Default.People,
                            onClick = onOpenCustomers,
                            testTag = "home_customers_btn",
                            modifier = Modifier.weight(1f)
                        )
                        DashboardActionTile(
                            title = "Price Book",
                            subtitle = "${state.priceBookItems.size} items",
                            icon = Icons.AutoMirrored.Filled.ListAlt,
                            onClick = onOpenPriceBook,
                            testTag = "home_pricebook_btn",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardActionTile(
                            title = "Settings",
                            subtitle = "Theme, rates & defaults",
                            icon = Icons.Default.Settings,
                            onClick = onOpenSettings,
                            testTag = "home_settings_btn",
                            modifier = Modifier.weight(1f)
                        )
                        DashboardActionTile(
                            title = "Materials",
                            subtitle = "Calculators & takeoff",
                            icon = Icons.Default.Build,
                            onClick = onOpenMaterials,
                            testTag = "home_materials_btn",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Recent Documents
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Estimates & Proposals",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (state.savedProposals.isEmpty()) {
                        ThdrSubCard {
                            Text(
                                text = "No saved estimates or proposals yet. Tap Create above to start your first project.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMuted
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.savedProposals.take(4).forEach { doc ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = palette.surfaceVariant,
                                    border = BorderStroke(1.dp, palette.outlineSubtle),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onOpenSavedDocument(doc) }
                                        .testTag("home_recent_doc_${doc.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            val title = doc.project.ifBlank { doc.customer }.ifBlank { doc.docType.title }
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = palette.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val subtitle = buildString {
                                                append(doc.docType.title)
                                                if (doc.docType == DocumentType.PROPOSAL) {
                                                    append(" · ${doc.status.label}")
                                                }
                                                if (doc.customer.isNotBlank()) {
                                                    append(" · ${doc.customer}")
                                                }
                                                if (doc.date.isNotBlank()) {
                                                    append(" · ${doc.date}")
                                                }
                                            }
                                            Text(
                                                text = subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.textMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = EstimatorEngine.formatMoney(doc.total, currency),
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                                            color = palette.brandHighlight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalThdrPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceVariant)
            .border(1.dp, palette.outlineSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
            color = palette.brandHighlight
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DocumentWorkflowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    savedCount: Int,
    createText: String,
    savedText: String,
    onCreate: () -> Unit,
    onSaved: () -> Unit,
    createTag: String,
    savedTag: String,
    useAccentGradient: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalThdrPalette.current
    ThdrCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            if (useAccentGradient) listOf(palette.accent, palette.accentSecondary)
                            else listOf(palette.primary, palette.primaryDeep)
                        )
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
                Text(
                    text = "$savedCount saved on device",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.brandHighlight
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThdrPrimaryButton(
                text = "+ $createText",
                onClick = onCreate,
                orangeAccent = useAccentGradient,
                testTag = createTag,
                modifier = Modifier.weight(1f)
            )
            ThdrSecondaryButton(
                text = savedText,
                onClick = onSaved,
                testTag = savedTag,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DashboardActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalThdrPalette.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = palette.surfaceVariant,
        border = BorderStroke(1.dp, palette.outline),
        modifier = modifier
            .defaultMinSize(minHeight = 72.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.surfaceHigh)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = palette.brandHighlight
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
