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
import com.example.model.BrandLogoChoice
import com.example.model.DocumentType
import com.example.model.HeroBackdropChoice
import com.example.model.HeroMascotPosition
import com.example.model.MascotSlotChoice
import com.example.ui.components.BrandingImage
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
    onResumeActiveDraft: () -> Unit
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
                    if (state.settings.showHomeHeroBanner) {
                        val settings = state.settings
                        val showMascot = settings.homeMascotPosition != HeroMascotPosition.HIDDEN &&
                            settings.mascotChoice != MascotSlotChoice.HIDDEN
                        val mascotFallbackRes = when (settings.mascotChoice) {
                            MascotSlotChoice.CIRCULAR_BADGE -> R.drawable.thdr_mascot_badge
                            MascotSlotChoice.COMPANY_EMBLEM -> R.drawable.thdr_logo
                            else -> R.drawable.thdr_mascot_avatar
                        }
                        val mascotCustomPath = if (settings.mascotChoice == MascotSlotChoice.CUSTOM) {
                            settings.customMascotPath
                        } else ""

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, palette.outline, RoundedCornerShape(14.dp))
                        ) {
                            if (settings.heroBackdropChoice != HeroBackdropChoice.GRADIENT_ONLY) {
                                val heroCustomPath = if (settings.heroBackdropChoice == HeroBackdropChoice.CUSTOM) {
                                    settings.customBackdropPath
                                } else ""
                                BrandingImage(
                                    customPath = heroCustomPath,
                                    fallbackResId = R.drawable.bg_blueprint,
                                    contentDescription = "House DR Architectural Blueprint",
                                    contentScale = ContentScale.Crop,
                                    maxDecodePx = 1024,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                palette.primaryDeep.copy(alpha = 0.94f),
                                                palette.primary.copy(alpha = 0.84f),
                                                palette.primaryDeep.copy(alpha = 0.72f)
                                            )
                                        )
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (showMascot && settings.homeMascotPosition == HeroMascotPosition.LEFT) {
                                    BrandingImage(
                                        customPath = mascotCustomPath,
                                        fallbackResId = mascotFallbackRes,
                                        contentDescription = "House DR Estimator Mascot",
                                        contentScale = ContentScale.Fit,
                                        maxDecodePx = 512,
                                        modifier = Modifier
                                            .height(138.dp)
                                            .width(84.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .testTag("home_mascot_avatar")
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    if (settings.brandLogoChoice != BrandLogoChoice.HIDDEN) {
                                        val logoFallbackRes = when (settings.brandLogoChoice) {
                                            BrandLogoChoice.DEFAULT_EMBLEM -> R.drawable.thdr_logo
                                            BrandLogoChoice.MASCOT_BADGE -> R.drawable.thdr_mascot_badge
                                            else -> R.drawable.thdr_brand_logo
                                        }
                                        val logoCustomPath = if (settings.brandLogoChoice == BrandLogoChoice.CUSTOM) {
                                            settings.customBrandLogoPath
                                        } else ""
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(palette.primaryDeep.copy(alpha = 0.90f))
                                                .border(1.dp, palette.headerMuted.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            BrandingImage(
                                                customPath = logoCustomPath,
                                                fallbackResId = logoFallbackRes,
                                                contentDescription = "Company Brand Logo",
                                                contentScale = ContentScale.Fit,
                                                maxDecodePx = 512,
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .widthIn(max = 165.dp)
                                                    .testTag("home_brand_logo")
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    Text(
                                        text = settings.companyName.ifBlank { "EZ ESTIMATOR" },
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = palette.headerText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = settings.companyTagline.ifBlank { "HOUSE DR · INTERIOR / EXTERIOR" },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.headerMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Create room-by-room estimates, client-ready proposals, and material takeoffs powered by your Price Book.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.headerText.copy(alpha = 0.92f)
                                    )
                                }

                                if (showMascot && settings.homeMascotPosition == HeroMascotPosition.RIGHT) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    BrandingImage(
                                        customPath = mascotCustomPath,
                                        fallbackResId = mascotFallbackRes,
                                        contentDescription = "House DR Estimator Mascot",
                                        contentScale = ContentScale.Fit,
                                        maxDecodePx = 512,
                                        modifier = Modifier
                                            .height(138.dp)
                                            .width(84.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .testTag("home_mascot_avatar")
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
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
            .background(palette.surfaceHigh.copy(alpha = 0.72f))
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
                            listOf(palette.primary.copy(alpha = 0.92f), palette.primaryDeep.copy(alpha = 0.96f))
                        )
                    )
                    .border(
                        width = if (useAccentGradient) 1.5.dp else 1.dp,
                        color = if (useAccentGradient) palette.accent.copy(alpha = 0.85f) else palette.outline.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
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
        color = palette.surfaceVariant.copy(alpha = 0.82f),
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
                    .background(palette.surfaceHigh.copy(alpha = 0.76f))
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
