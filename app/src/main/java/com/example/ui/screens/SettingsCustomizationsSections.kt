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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.EstimatePrintHelper
import com.example.model.AppSettings
import com.example.model.BrandLogoChoice
import com.example.model.BrandingSlot
import com.example.model.HeaderEmblemChoice
import com.example.model.HeroBackdropChoice
import com.example.model.HeroMascotPosition
import com.example.model.MascotSlotChoice
import com.example.model.PdfColorTheme
import com.example.model.PdfLogoMode
import com.example.ui.components.BrandingImage
import com.example.ui.components.SelectablePill
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.LocalThdrPalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsImagesAndLogosSection(
    draft: AppSettings,
    onUpdateDraft: ((AppSettings) -> AppSettings) -> Unit,
    onPickImageForSlot: (BrandingSlot) -> Unit,
    onClearCustomImage: (BrandingSlot, String) -> Unit
) {
    val palette = LocalThdrPalette.current
    ThdrCard {
        Text(
            text = "2. Pre-Positioned Images & Logos",
            style = MaterialTheme.typography.titleLarge,
            color = palette.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Choose built-in branding or upload custom photos/logos from your device into pre-positioned slots across the Header, Home Hero, Estimator, and PDFs.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Live Pre-Positioned Hero & Header Preview
        PrePositionedLayoutPreview(draft = draft)

        Spacer(modifier = Modifier.height(14.dp))

        // Slot 1: Header Emblem
        BrandingSlotEditorCard(
            title = "Slot A · App Header Emblem",
            subtitle = "Positioned at the top-left of every screen next to your company name.",
            previewCustomPath = if (draft.headerEmblemChoice == HeaderEmblemChoice.CUSTOM) draft.customHeaderEmblemPath else "",
            previewFallbackRes = when (draft.headerEmblemChoice) {
                HeaderEmblemChoice.MASCOT_BADGE -> R.drawable.thdr_mascot_badge
                HeaderEmblemChoice.FULL_LOCKUP -> R.drawable.thdr_brand_logo
                HeaderEmblemChoice.HIDDEN -> null
                else -> R.drawable.thdr_logo
            },
            hasCustomImage = draft.customHeaderEmblemPath.isNotBlank(),
            onUploadClick = { onPickImageForSlot(BrandingSlot.HEADER_EMBLEM) },
            onClearCustomClick = {
                val old = draft.customHeaderEmblemPath
                onClearCustomImage(BrandingSlot.HEADER_EMBLEM, old)
                onUpdateDraft {
                    it.copy(
                        customHeaderEmblemPath = "",
                        headerEmblemChoice = HeaderEmblemChoice.DEFAULT_EMBLEM
                    )
                }
            },
            uploadTag = "settings_upload_header_emblem"
        ) {
            HeaderEmblemChoice.entries.forEach { choice ->
                val enabled = choice != HeaderEmblemChoice.CUSTOM || draft.customHeaderEmblemPath.isNotBlank()
                SelectablePill(
                    text = choice.label,
                    selected = draft.headerEmblemChoice == choice,
                    onClick = {
                        if (choice == HeaderEmblemChoice.CUSTOM && draft.customHeaderEmblemPath.isBlank()) {
                            onPickImageForSlot(BrandingSlot.HEADER_EMBLEM)
                        } else if (enabled) {
                            onUpdateDraft { it.copy(headerEmblemChoice = choice) }
                        }
                    },
                    testTag = "settings_emblem_choice_${choice.key}"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Slot 2: Brand Lockup Logo
        BrandingSlotEditorCard(
            title = "Slot B · Full Brand Lockup Logo",
            subtitle = "Positioned inside the Home dashboard banner and PDF brand header badge.",
            previewCustomPath = if (draft.brandLogoChoice == BrandLogoChoice.CUSTOM) draft.customBrandLogoPath else "",
            previewFallbackRes = when (draft.brandLogoChoice) {
                BrandLogoChoice.DEFAULT_EMBLEM -> R.drawable.thdr_logo
                BrandLogoChoice.MASCOT_BADGE -> R.drawable.thdr_mascot_badge
                BrandLogoChoice.HIDDEN -> null
                else -> R.drawable.thdr_brand_logo
            },
            hasCustomImage = draft.customBrandLogoPath.isNotBlank(),
            onUploadClick = { onPickImageForSlot(BrandingSlot.BRAND_LOCKUP) },
            onClearCustomClick = {
                val old = draft.customBrandLogoPath
                onClearCustomImage(BrandingSlot.BRAND_LOCKUP, old)
                onUpdateDraft {
                    it.copy(
                        customBrandLogoPath = "",
                        brandLogoChoice = BrandLogoChoice.DEFAULT_LOCKUP
                    )
                }
            },
            uploadTag = "settings_upload_brand_logo"
        ) {
            BrandLogoChoice.entries.forEach { choice ->
                SelectablePill(
                    text = choice.label,
                    selected = draft.brandLogoChoice == choice,
                    onClick = {
                        if (choice == BrandLogoChoice.CUSTOM && draft.customBrandLogoPath.isBlank()) {
                            onPickImageForSlot(BrandingSlot.BRAND_LOCKUP)
                        } else {
                            onUpdateDraft { it.copy(brandLogoChoice = choice) }
                        }
                    },
                    testTag = "settings_brand_choice_${choice.key}"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Slot 3: Hero & Backdrop Artwork
        BrandingSlotEditorCard(
            title = "Slot C · Hero Banner & Screen Backdrop",
            subtitle = "Full-width architectural background behind the Home hero card and workspace backdrop.",
            previewCustomPath = if (draft.heroBackdropChoice == HeroBackdropChoice.CUSTOM) draft.customBackdropPath else "",
            previewFallbackRes = if (draft.heroBackdropChoice == HeroBackdropChoice.GRADIENT_ONLY) null else R.drawable.bg_blueprint,
            hasCustomImage = draft.customBackdropPath.isNotBlank(),
            onUploadClick = { onPickImageForSlot(BrandingSlot.HERO_BACKDROP) },
            onClearCustomClick = {
                val old = draft.customBackdropPath
                onClearCustomImage(BrandingSlot.HERO_BACKDROP, old)
                onUpdateDraft {
                    it.copy(
                        customBackdropPath = "",
                        heroBackdropChoice = HeroBackdropChoice.DEFAULT_BLUEPRINT
                    )
                }
            },
            uploadTag = "settings_upload_backdrop"
        ) {
            HeroBackdropChoice.entries.forEach { choice ->
                SelectablePill(
                    text = choice.label,
                    selected = draft.heroBackdropChoice == choice,
                    onClick = {
                        if (choice == HeroBackdropChoice.CUSTOM && draft.customBackdropPath.isBlank()) {
                            onPickImageForSlot(BrandingSlot.HERO_BACKDROP)
                        } else {
                            onUpdateDraft { it.copy(heroBackdropChoice = choice) }
                        }
                    },
                    testTag = "settings_backdrop_choice_${choice.key}"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Slot 4: Mascot / Trade Badge + Position
        BrandingSlotEditorCard(
            title = "Slot D · Mascot / Feature Graphic",
            subtitle = "Positioned in the Home Hero banner and Estimator empty-room helper card.",
            previewCustomPath = if (draft.mascotChoice == MascotSlotChoice.CUSTOM) draft.customMascotPath else "",
            previewFallbackRes = when (draft.mascotChoice) {
                MascotSlotChoice.CIRCULAR_BADGE -> R.drawable.thdr_mascot_badge
                MascotSlotChoice.COMPANY_EMBLEM -> R.drawable.thdr_logo
                MascotSlotChoice.HIDDEN -> null
                else -> R.drawable.thdr_mascot_avatar
            },
            hasCustomImage = draft.customMascotPath.isNotBlank(),
            onUploadClick = { onPickImageForSlot(BrandingSlot.HOME_MASCOT) },
            onClearCustomClick = {
                val old = draft.customMascotPath
                onClearCustomImage(BrandingSlot.HOME_MASCOT, old)
                onUpdateDraft {
                    it.copy(
                        customMascotPath = "",
                        mascotChoice = MascotSlotChoice.STANDING_MASCOT
                    )
                }
            },
            uploadTag = "settings_upload_mascot"
        ) {
            MascotSlotChoice.entries.forEach { choice ->
                SelectablePill(
                    text = choice.label,
                    selected = draft.mascotChoice == choice,
                    onClick = {
                        if (choice == MascotSlotChoice.CUSTOM && draft.customMascotPath.isBlank()) {
                            onPickImageForSlot(BrandingSlot.HOME_MASCOT)
                        } else {
                            onUpdateDraft { it.copy(mascotChoice = choice) }
                        }
                    },
                    testTag = "settings_mascot_choice_${choice.key}"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Home Hero Mascot Placement",
            style = MaterialTheme.typography.labelLarge,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeroMascotPosition.entries.forEach { pos ->
                SelectablePill(
                    text = pos.label,
                    selected = draft.homeMascotPosition == pos,
                    onClick = { onUpdateDraft { it.copy(homeMascotPosition = pos) } },
                    testTag = "settings_mascot_pos_${pos.key}"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Backdrop Texture Opacity",
            style = MaterialTheme.typography.labelLarge,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(10, 18, 26, 35).forEach { pct ->
                SelectablePill(
                    text = "$pct%",
                    selected = draft.backdropOpacityPct == pct,
                    onClick = { onUpdateDraft { it.copy(backdropOpacityPct = pct) } },
                    testTag = "settings_backdrop_opacity_$pct"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        SettingsSwitchRow(
            title = "Show Home Hero Banner Card",
            subtitle = "Display the branded banner with lockup, tagline, and mascot on the Home tab",
            checked = draft.showHomeHeroBanner,
            onCheckedChange = { c -> onUpdateDraft { it.copy(showHomeHeroBanner = c) } },
            testTag = "settings_switch_home_hero"
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsPdfCustomizationSection(
    draft: AppSettings,
    onUpdateDraft: ((AppSettings) -> AppSettings) -> Unit,
    onPickImageForSlot: (BrandingSlot) -> Unit,
    onClearCustomImage: (BrandingSlot, String) -> Unit
) {
    val palette = LocalThdrPalette.current
    ThdrCard {
        Text(
            text = "4. Complete PDF Estimate & Proposal Customizer",
            style = MaterialTheme.typography.titleLarge,
            color = palette.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Customize every region of exported Estimates & Proposals: PDF color palette, header logo layout, document headings, numbering prefix, section visibility, notes, and signature labels.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(12.dp))

        PdfLayoutMiniPreview(draft = draft)

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "PDF Color Theme",
            style = MaterialTheme.typography.labelLarge,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PdfColorTheme.entries.forEach { pdfTheme ->
                val selected = draft.pdfColorTheme == pdfTheme
                val swatchColor = if (pdfTheme == PdfColorTheme.MATCH_APP_ACCENT) {
                    Color(draft.accentColor.primaryArgb)
                } else {
                    Color(android.graphics.Color.parseColor(pdfTheme.primaryHex))
                }
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = if (selected) palette.primary.copy(alpha = 0.90f) else palette.surfaceVariant.copy(alpha = 0.80f),
                    border = BorderStroke(1.dp, if (selected) palette.brandHighlight else palette.outline),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 40.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .clickable { onUpdateDraft { it.copy(pdfColorTheme = pdfTheme) } }
                        .testTag("settings_pdf_theme_${pdfTheme.key}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        )
                        Text(
                            text = pdfTheme.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else palette.textPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "PDF Header Logo Layout",
            style = MaterialTheme.typography.labelLarge,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PdfLogoMode.entries.forEach { mode ->
                SelectablePill(
                    text = mode.label,
                    selected = draft.pdfLogoMode == mode,
                    onClick = {
                        if (mode == PdfLogoMode.CUSTOM_PDF_IMAGE && draft.customPdfLogoPath.isBlank()) {
                            onPickImageForSlot(BrandingSlot.PDF_LOGO)
                        } else {
                            onUpdateDraft { it.copy(pdfLogoMode = mode) }
                        }
                    },
                    testTag = "settings_pdf_logo_mode_${mode.key}"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThdrSecondaryButton(
                text = if (draft.customPdfLogoPath.isBlank()) "Upload Custom PDF Logo" else "Replace PDF Logo",
                onClick = { onPickImageForSlot(BrandingSlot.PDF_LOGO) },
                testTag = "settings_upload_pdf_logo",
                modifier = Modifier.weight(1f)
            )
            if (draft.customPdfLogoPath.isNotBlank()) {
                ThdrSecondaryButton(
                    text = "Remove",
                    onClick = {
                        val old = draft.customPdfLogoPath
                        onClearCustomImage(BrandingSlot.PDF_LOGO, old)
                        onUpdateDraft {
                            it.copy(
                                customPdfLogoPath = "",
                                pdfLogoMode = PdfLogoMode.EMBLEM_AND_LOCKUP
                            )
                        }
                    },
                    testTag = "settings_clear_pdf_logo"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ThdrTextField(
                value = draft.pdfCompanyTitleOverride,
                onValueChange = { v -> onUpdateDraft { it.copy(pdfCompanyTitleOverride = v) } },
                label = "PDF Company Heading Override (blank = Company Name)",
                placeholder = draft.companyName,
                testTag = "settings_pdf_company_override"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThdrTextField(
                    value = draft.pdfEstimateTitle,
                    onValueChange = { v -> onUpdateDraft { it.copy(pdfEstimateTitle = v) } },
                    label = "Estimate Title",
                    modifier = Modifier.weight(1f),
                    testTag = "settings_pdf_est_title"
                )
                ThdrTextField(
                    value = draft.pdfProposalTitle,
                    onValueChange = { v -> onUpdateDraft { it.copy(pdfProposalTitle = v) } },
                    label = "Proposal Title",
                    modifier = Modifier.weight(1f),
                    testTag = "settings_pdf_prop_title"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThdrTextField(
                    value = draft.pdfDocNumberPrefix,
                    onValueChange = { v -> onUpdateDraft { it.copy(pdfDocNumberPrefix = v) } },
                    label = "Doc Number Prefix",
                    modifier = Modifier.weight(1f),
                    testTag = "settings_pdf_prefix"
                )
                ThdrTextField(
                    value = draft.pdfCustomerSectionTitle,
                    onValueChange = { v -> onUpdateDraft { it.copy(pdfCustomerSectionTitle = v) } },
                    label = "Customer Banner Label",
                    modifier = Modifier.weight(1f),
                    testTag = "settings_pdf_customer_title"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThdrTextField(
                    value = draft.pdfProjectSectionTitle,
                    onValueChange = { v -> onUpdateDraft { it.copy(pdfProjectSectionTitle = v) } },
                    label = "Project Box Label",
                    modifier = Modifier.weight(1f),
                    testTag = "settings_pdf_project_title"
                )
                ThdrTextField(
                    value = draft.pdfMaterialsSectionTitle,
                    onValueChange = { v -> onUpdateDraft { it.copy(pdfMaterialsSectionTitle = v) } },
                    label = "Materials Table Label",
                    modifier = Modifier.weight(1f),
                    testTag = "settings_pdf_materials_title"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThdrTextField(
                    value = draft.pdfContractorSignatureLabel,
                    onValueChange = { v -> onUpdateDraft { it.copy(pdfContractorSignatureLabel = v) } },
                    label = "Contractor Signer Label",
                    modifier = Modifier.weight(1f),
                    testTag = "settings_pdf_contractor_sig_label"
                )
                ThdrTextField(
                    value = draft.pdfClientSignatureLabel,
                    onValueChange = { v -> onUpdateDraft { it.copy(pdfClientSignatureLabel = v) } },
                    label = "Client Signer Label",
                    modifier = Modifier.weight(1f),
                    testTag = "settings_pdf_client_sig_label"
                )
            }
            ThdrTextField(
                value = draft.pdfContactSectionTitle,
                onValueChange = { v -> onUpdateDraft { it.copy(pdfContactSectionTitle = v) } },
                label = "Contact Section Heading",
                testTag = "settings_pdf_contact_title"
            )
            ThdrTextField(
                value = draft.pdfCustomEstimateNotes,
                onValueChange = { v -> onUpdateDraft { it.copy(pdfCustomEstimateNotes = v) } },
                label = "Custom Estimate PDF Notes (blank = auto valid days & accuracy note)",
                singleLine = false,
                minLines = 2,
                testTag = "settings_pdf_est_notes"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsSwitchRow(
                title = "Show PDF Top Brand Badge",
                subtitle = "Framed emblem / logo box above the PDF company title",
                checked = draft.pdfShowBrandBadge,
                onCheckedChange = { c -> onUpdateDraft { it.copy(pdfShowBrandBadge = c) } },
                testTag = "settings_pdf_switch_badge"
            )
            SettingsSwitchRow(
                title = "Two-Tone Split Page Backdrop",
                subtitle = "Show studio gray + primary color band behind the white PDF sheet",
                checked = draft.pdfShowSplitBackdrop,
                onCheckedChange = { c -> onUpdateDraft { it.copy(pdfShowSplitBackdrop = c) } },
                testTag = "settings_pdf_switch_backdrop"
            )
            SettingsSwitchRow(
                title = "Geometric Corner X Patterns",
                subtitle = "Decorative corner graphics on top-right and bottom-left of PDF pages",
                checked = draft.pdfShowCornerPattern,
                onCheckedChange = { c -> onUpdateDraft { it.copy(pdfShowCornerPattern = c) } },
                testTag = "settings_pdf_switch_corners"
            )
            SettingsSwitchRow(
                title = "Show Customer Information Grid",
                subtitle = "Include client name, company, email, phone, and site address on PDF",
                checked = draft.pdfShowCustomerSection,
                onCheckedChange = { c -> onUpdateDraft { it.copy(pdfShowCustomerSection = c) } },
                testTag = "settings_pdf_switch_customer"
            )
            SettingsSwitchRow(
                title = "Show Project Description Box",
                subtitle = "Include room dimensions, square footages, conditions, and room notes",
                checked = draft.pdfShowProjectDescription,
                onCheckedChange = { c -> onUpdateDraft { it.copy(pdfShowProjectDescription = c) } },
                testTag = "settings_pdf_switch_project"
            )
            SettingsSwitchRow(
                title = "Show Materials Takeoff Table",
                subtitle = "Include calculated lumber, drywall, paint, and tile quantities on PDF",
                checked = draft.pdfShowMaterialsTable,
                onCheckedChange = { c -> onUpdateDraft { it.copy(pdfShowMaterialsTable = c) } },
                testTag = "settings_pdf_switch_materials"
            )
            SettingsSwitchRow(
                title = "Show Notes & Terms Block",
                subtitle = "Display estimate notes or proposal terms beside totals on PDF",
                checked = draft.pdfShowNotesSection,
                onCheckedChange = { c -> onUpdateDraft { it.copy(pdfShowNotesSection = c) } },
                testTag = "settings_pdf_switch_notes"
            )
            SettingsSwitchRow(
                title = "Show Contact Us Footer",
                subtitle = "Print business email, phone, location, and license number on PDF",
                checked = draft.pdfShowContactSection,
                onCheckedChange = { c -> onUpdateDraft { it.copy(pdfShowContactSection = c) } },
                testTag = "settings_pdf_switch_contact"
            )
            SettingsSwitchRow(
                title = "Signature Block on Estimates",
                subtitle = "Print contractor & client signature lines on Estimate PDFs",
                checked = draft.includeSignatureBlockOnEstimate,
                onCheckedChange = { c -> onUpdateDraft { it.copy(includeSignatureBlockOnEstimate = c) } },
                testTag = "settings_pdf_switch_est_sig"
            )
        }
    }
}

@Composable
private fun PrePositionedLayoutPreview(draft: AppSettings) {
    val palette = LocalThdrPalette.current
    ThdrSubCard {
        Text(
            text = "LIVE PRE-POSITIONED PREVIEW",
            style = MaterialTheme.typography.labelSmall,
            color = palette.brandHighlight
        )
        Spacer(modifier = Modifier.height(6.dp))
        // Mini Header Bar Preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.horizontalGradient(palette.headerGradient))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (draft.headerEmblemChoice != HeaderEmblemChoice.HIDDEN) {
                val res = when (draft.headerEmblemChoice) {
                    HeaderEmblemChoice.MASCOT_BADGE -> R.drawable.thdr_mascot_badge
                    HeaderEmblemChoice.FULL_LOCKUP -> R.drawable.thdr_brand_logo
                    else -> R.drawable.thdr_logo
                }
                val path = if (draft.headerEmblemChoice == HeaderEmblemChoice.CUSTOM) draft.customHeaderEmblemPath else ""
                BrandingImage(
                    customPath = path,
                    fallbackResId = res,
                    contentDescription = "Preview Header Emblem",
                    contentScale = ContentScale.Fit,
                    maxDecodePx = 128,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(5.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.companyName.ifBlank { "EZ ESTIMATOR" }.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = palette.headerText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = draft.companyTagline.ifBlank { "HOUSE DR · INTERIOR / EXTERIOR" }.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = palette.headerMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (draft.showHomeHeroBanner) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, palette.outline, RoundedCornerShape(10.dp))
            ) {
                if (draft.heroBackdropChoice != HeroBackdropChoice.GRADIENT_ONLY) {
                    val path = if (draft.heroBackdropChoice == HeroBackdropChoice.CUSTOM) draft.customBackdropPath else ""
                    BrandingImage(
                        customPath = path,
                        fallbackResId = R.drawable.bg_blueprint,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        maxDecodePx = 512,
                        modifier = Modifier.matchParentSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    palette.primaryDeep.copy(alpha = 0.92f),
                                    palette.primary.copy(alpha = 0.80f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val showMascot = draft.homeMascotPosition != HeroMascotPosition.HIDDEN &&
                        draft.mascotChoice != MascotSlotChoice.HIDDEN
                    val mascotRes = when (draft.mascotChoice) {
                        MascotSlotChoice.CIRCULAR_BADGE -> R.drawable.thdr_mascot_badge
                        MascotSlotChoice.COMPANY_EMBLEM -> R.drawable.thdr_logo
                        else -> R.drawable.thdr_mascot_avatar
                    }
                    val mascotPath = if (draft.mascotChoice == MascotSlotChoice.CUSTOM) draft.customMascotPath else ""

                    if (showMascot && draft.homeMascotPosition == HeroMascotPosition.LEFT) {
                        BrandingImage(
                            customPath = mascotPath,
                            fallbackResId = mascotRes,
                            contentDescription = "Mascot Preview",
                            contentScale = ContentScale.Fit,
                            maxDecodePx = 192,
                            modifier = Modifier
                                .size(width = 44.dp, height = 62.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        if (draft.brandLogoChoice != BrandLogoChoice.HIDDEN) {
                            val lockRes = when (draft.brandLogoChoice) {
                                BrandLogoChoice.DEFAULT_EMBLEM -> R.drawable.thdr_logo
                                BrandLogoChoice.MASCOT_BADGE -> R.drawable.thdr_mascot_badge
                                else -> R.drawable.thdr_brand_logo
                            }
                            val lockPath = if (draft.brandLogoChoice == BrandLogoChoice.CUSTOM) draft.customBrandLogoPath else ""
                            BrandingImage(
                                customPath = lockPath,
                                fallbackResId = lockRes,
                                contentDescription = "Brand Lockup Preview",
                                contentScale = ContentScale.Fit,
                                maxDecodePx = 256,
                                modifier = Modifier
                                    .height(26.dp)
                                    .widthIn(max = 110.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Text(
                            text = draft.companyName.ifBlank { "EZ ESTIMATOR" },
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.headerText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Home Hero Banner Preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.headerMuted
                        )
                    }

                    if (showMascot && draft.homeMascotPosition == HeroMascotPosition.RIGHT) {
                        Spacer(modifier = Modifier.width(8.dp))
                        BrandingImage(
                            customPath = mascotPath,
                            fallbackResId = mascotRes,
                            contentDescription = "Mascot Preview",
                            contentScale = ContentScale.Fit,
                            maxDecodePx = 192,
                            modifier = Modifier
                                .size(width = 44.dp, height = 62.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfLayoutMiniPreview(draft: AppSettings) {
    val pdfColors = EstimatePrintHelper.resolvePdfPalette(draft)
    val primary = Color(android.graphics.Color.parseColor(pdfColors.primaryHex))
    val deep = Color(android.graphics.Color.parseColor(pdfColors.deepHex))
    val steel = Color(android.graphics.Color.parseColor(pdfColors.steelHex))
    val sky = Color(android.graphics.Color.parseColor(pdfColors.skyHex))
    val softGray = Color(android.graphics.Color.parseColor(pdfColors.softGrayHex))
    val accent = Color(android.graphics.Color.parseColor(pdfColors.accentHex))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (draft.pdfShowSplitBackdrop) softGray else Color.White)
            .border(1.dp, steel, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (draft.pdfShowBrandBadge && draft.pdfLogoMode != PdfLogoMode.HIDDEN) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(deep)
                                .border(1.dp, accent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            when (draft.pdfLogoMode) {
                                PdfLogoMode.CUSTOM_PDF_IMAGE -> BrandingImage(
                                    customPath = draft.customPdfLogoPath,
                                    fallbackResId = R.drawable.thdr_brand_logo,
                                    contentDescription = "Custom PDF Logo",
                                    modifier = Modifier.height(18.dp).widthIn(max = 76.dp)
                                )
                                PdfLogoMode.EMBLEM_ONLY -> BrandingImage(
                                    customPath = if (draft.headerEmblemChoice == HeaderEmblemChoice.CUSTOM) draft.customHeaderEmblemPath else "",
                                    fallbackResId = R.drawable.thdr_logo,
                                    contentDescription = "PDF Emblem",
                                    modifier = Modifier.size(18.dp)
                                )
                                PdfLogoMode.LOCKUP_ONLY -> BrandingImage(
                                    customPath = if (draft.brandLogoChoice == BrandLogoChoice.CUSTOM) draft.customBrandLogoPath else "",
                                    fallbackResId = R.drawable.thdr_brand_logo,
                                    contentDescription = "PDF Lockup",
                                    modifier = Modifier.height(18.dp).widthIn(max = 76.dp)
                                )
                                PdfLogoMode.EMBLEM_AND_LOCKUP -> {
                                    BrandingImage(
                                        customPath = if (draft.headerEmblemChoice == HeaderEmblemChoice.CUSTOM) draft.customHeaderEmblemPath else "",
                                        fallbackResId = R.drawable.thdr_logo,
                                        contentDescription = "PDF Emblem",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    BrandingImage(
                                        customPath = if (draft.brandLogoChoice == BrandLogoChoice.CUSTOM) draft.customBrandLogoPath else "",
                                        fallbackResId = R.drawable.thdr_brand_logo,
                                        contentDescription = "PDF Lockup",
                                        modifier = Modifier.height(18.dp).widthIn(max = 72.dp)
                                    )
                                }
                                else -> Text(
                                    text = draft.pdfCompanyTitleOverride.ifBlank { draft.companyName }.ifBlank { "HOUSE DR" },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = draft.pdfCompanyTitleOverride.ifBlank { draft.companyName }.ifBlank { "EZ ESTIMATOR" }.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF1D2B3A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = draft.pdfEstimateTitle.ifBlank { "ESTIMATE" },
                        style = MaterialTheme.typography.labelMedium,
                        color = primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Box(modifier = Modifier.size(width = 36.dp, height = 2.dp).background(accent))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${draft.pdfDocNumberPrefix.ifBlank { "THDR" }.uppercase()}-EST-2026-001",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color(0xFF4A4A4A)
                    )
                    if (draft.pdfShowCornerPattern) {
                        Text(
                            text = "× × ×",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = steel
                        )
                    }
                }
            }

            if (draft.pdfShowCustomerSection) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(99.dp))
                        .background(primary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = draft.pdfCustomerSectionTitle.ifBlank { "Customer Information" },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        // Bottom half of split backdrop + sheet card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (draft.pdfShowSplitBackdrop) primary else Color.White)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, steel, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (draft.pdfShowProjectDescription) {
                    Box(modifier = Modifier.fillMaxWidth().background(sky).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(
                            text = draft.pdfProjectSectionTitle.ifBlank { "Project Description" },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF1D2B3A)
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().background(sky).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(
                        text = "Item/Service Description  |  Qty  |  Total",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF1D2B3A)
                    )
                }
                if (draft.pdfShowMaterialsTable) {
                    Box(modifier = Modifier.fillMaxWidth().background(sky.copy(alpha = 0.65f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(
                            text = draft.pdfMaterialsSectionTitle.ifBlank { "Materials Takeoff Reference" },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = Color(0xFF1D2B3A)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = buildString {
                            if (draft.pdfShowContactSection) append(draft.pdfContactSectionTitle.ifBlank { "Contact Us" })
                            if (draft.includeSignatureBlockOnEstimate) {
                                if (isNotEmpty()) append(" · ")
                                append("${draft.pdfContractorSignatureLabel.ifBlank { "Contractor" }} / ${draft.pdfClientSignatureLabel.ifBlank { "Client" }}")
                            }
                        }.ifBlank { "Totals Only" },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = Color(0xFF4A4A4A)
                    )
                    Text(
                        text = "PDF Sheet Preview",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BrandingSlotEditorCard(
    title: String,
    subtitle: String,
    previewCustomPath: String,
    previewFallbackRes: Int?,
    hasCustomImage: Boolean,
    onUploadClick: () -> Unit,
    onClearCustomClick: () -> Unit,
    uploadTag: String,
    pillsContent: @Composable () -> Unit
) {
    val palette = LocalThdrPalette.current
    ThdrSubCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.primaryDeep.copy(alpha = 0.14f))
                    .border(1.dp, palette.outline, RoundedCornerShape(10.dp))
            ) {
                if (previewCustomPath.isNotBlank() || previewFallbackRes != null) {
                    BrandingImage(
                        customPath = previewCustomPath,
                        fallbackResId = previewFallbackRes,
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        maxDecodePx = 256,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Text(
                        text = "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pillsContent()
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.surfaceHigh.copy(alpha = 0.74f),
                border = BorderStroke(1.dp, palette.outline),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onUploadClick)
                    .testTag(uploadTag)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Upload photo",
                        tint = palette.brandHighlight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasCustomImage) "Replace Photo" else "Upload Photo / Logo",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textPrimary
                    )
                }
            }
            if (hasCustomImage) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = palette.dangerBg.copy(alpha = 0.86f),
                    border = BorderStroke(1.dp, palette.danger.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onClearCustomClick)
                        .testTag("${uploadTag}_clear")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove uploaded image",
                            tint = if (palette.isDark) palette.textPrimary else palette.danger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val palette = LocalThdrPalette.current
    ThdrSubCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clickable { onCheckedChange(!checked) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = palette.primary,
                    uncheckedThumbColor = palette.textMuted,
                    uncheckedTrackColor = palette.surfaceHigh
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
