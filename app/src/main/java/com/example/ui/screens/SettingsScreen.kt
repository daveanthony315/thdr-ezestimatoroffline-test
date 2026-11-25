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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppAccentColor
import com.example.model.AppThemePreset
import com.example.model.EstimateSide
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.SelectablePill
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrDangerButton
import com.example.ui.components.ThdrDoubleField
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.LocalThdrPalette
import com.example.viewmodel.EstimatorUiState
import com.example.viewmodel.EstimatorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    onOpenPrivacyPolicy: () -> Unit = {}
) {
    val palette = LocalThdrPalette.current
    val context = LocalContext.current
    val settings = state.settings
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header & Branding
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "App Settings & Customizations",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customize company branding, theme palette, field typography, default tax & labor rates, waste percentages, and proposal terms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
            }
        }

        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "1. Company & Header Branding",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ThdrSubCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.thdr_logo),
                                contentDescription = "Company Emblem Logo",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (palette.isDark) Color.Transparent else Color(0xFF101827))
                                        .padding(
                                            horizontal = if (palette.isDark) 0.dp else 8.dp,
                                            vertical = if (palette.isDark) 0.dp else 4.dp
                                        )
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.thdr_brand_logo),
                                        contentDescription = "The Home Improvement Brand Logo",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .height(38.dp)
                                            .widthIn(max = 150.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${settings.companyName.ifBlank { "EZ ESTIMATOR" }} · ${settings.companyTagline.ifBlank { "HOUSE DR" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThdrTextField(
                            value = settings.companyName,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(companyName = v) } },
                            label = "Company / App Header Name",
                            testTag = "settings_company_name"
                        )
                        ThdrTextField(
                            value = settings.companyTagline,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(companyTagline = v) } },
                            label = "Header Tagline / Trade",
                            testTag = "settings_company_tagline"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThdrTextField(
                                value = settings.companyPhone,
                                onValueChange = { v -> viewModel.updateSettings { it.copy(companyPhone = v) } },
                                label = "Business Phone",
                                modifier = Modifier.weight(1f),
                                testTag = "settings_company_phone"
                            )
                            ThdrTextField(
                                value = settings.companyLicense,
                                onValueChange = { v -> viewModel.updateSettings { it.copy(companyLicense = v) } },
                                label = "License / ROC #",
                                modifier = Modifier.weight(1f),
                                testTag = "settings_company_license"
                            )
                        }
                        ThdrTextField(
                            value = settings.companyEmail,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(companyEmail = v) } },
                            label = "Business Email",
                            testTag = "settings_company_email"
                        )
                        ThdrTextField(
                            value = settings.companyAddress,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(companyAddress = v) } },
                            label = "Business Address (shown on PDFs)",
                            testTag = "settings_company_address"
                        )
                    }
                }
            }
        }

        // 2. Theme & Display
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "2. Theme & Visual Appearance",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Theme Preset",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppThemePreset.entries.forEach { preset ->
                            val selected = settings.themePreset == preset
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) palette.primary.copy(alpha = 0.22f) else palette.surfaceVariant,
                                border = BorderStroke(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) palette.brandHighlight else palette.outlineSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.updateSettings { it.copy(themePreset = preset) }
                                    }
                                    .testTag("settings_theme_${preset.key}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.label,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = palette.textPrimary
                                        )
                                        Text(
                                            text = preset.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.textMuted
                                        )
                                    }
                                    Text(
                                        text = if (selected) "ACTIVE" else if (preset.isDark) "Dark" else "Light",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) palette.brandHighlight else palette.textMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Accent Color",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppAccentColor.entries.forEach { accent ->
                            val selected = settings.accentColor == accent
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = if (selected) palette.primary else palette.surfaceVariant,
                                border = BorderStroke(1.dp, if (selected) palette.brandHighlight else palette.outline),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 44.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .clickable {
                                        viewModel.updateSettings { it.copy(accentColor = accent) }
                                    }
                                    .testTag("settings_accent_${accent.key}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(accent.primaryArgb))
                                            .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                                    )
                                    Text(
                                        text = accent.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) Color.White else palette.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    SettingsSwitchRow(
                        title = "Blueprint Grid Backdrop",
                        subtitle = "Show architectural grid texture behind screens",
                        checked = settings.showBlueprintBackdrop,
                        onCheckedChange = { c -> viewModel.updateSettings { it.copy(showBlueprintBackdrop = c) } },
                        testTag = "settings_switch_blueprint"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsSwitchRow(
                        title = "Large Field Readability Text",
                        subtitle = "Increase text sizes across screens for bright job sites",
                        checked = settings.largeFieldText,
                        onCheckedChange = { c -> viewModel.updateSettings { it.copy(largeFieldText = c) } },
                        testTag = "settings_switch_large_text"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsSwitchRow(
                        title = "Show Internal Job Cost & Profit",
                        subtitle = "Display internal contractor cost & margin line on Estimate Summary",
                        checked = settings.showInternalCostProfit,
                        onCheckedChange = { c -> viewModel.updateSettings { it.copy(showInternalCostProfit = c) } },
                        testTag = "settings_switch_internal_profit"
                    )
                }
            }
        }

        // 3. Pricing & Estimate Defaults
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "3. Pricing & Estimate Defaults",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThdrDoubleField(
                            value = settings.defaultHourlyRate,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(defaultHourlyRate = v) } },
                            label = "Default Hourly Rate",
                            modifier = Modifier.weight(1f),
                            testTag = "settings_hourly_rate"
                        )
                        ThdrDoubleField(
                            value = settings.defaultTaxPercent,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(defaultTaxPercent = v) } },
                            label = "Default Tax (%)",
                            modifier = Modifier.weight(1f),
                            testTag = "settings_default_tax"
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Currency Symbol",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("$", "CA$", "£", "€").forEach { sym ->
                            SelectablePill(
                                text = sym,
                                selected = settings.currencySymbol == sym,
                                onClick = { viewModel.updateSettings { it.copy(currencySymbol = sym) } },
                                testTag = "settings_currency_$sym"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Default Work Side for New Estimates",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectablePill(
                            text = "Interior",
                            selected = settings.defaultEstimateSide == EstimateSide.INTERIOR,
                            onClick = { viewModel.updateSettings { it.copy(defaultEstimateSide = EstimateSide.INTERIOR) } },
                            testTag = "settings_side_interior"
                        )
                        SelectablePill(
                            text = "Exterior",
                            selected = settings.defaultEstimateSide == EstimateSide.EXTERIOR,
                            onClick = { viewModel.updateSettings { it.copy(defaultEstimateSide = EstimateSide.EXTERIOR) } },
                            testTag = "settings_side_exterior"
                        )
                    }
                }
            }
        }

        // 4. Material Waste & Coverage Defaults
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "4. Material Waste & Coverage Defaults",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Applied automatically when computing project takeoffs from room dimensions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThdrDoubleField(
                            value = settings.defaultFlooringWastePct.toDouble(),
                            onValueChange = { v -> viewModel.updateSettings { it.copy(defaultFlooringWastePct = v.toInt().coerceIn(0, 50)) } },
                            label = "Flooring Waste %",
                            isInteger = true,
                            modifier = Modifier.weight(1f),
                            testTag = "settings_waste_flooring"
                        )
                        ThdrDoubleField(
                            value = settings.defaultPaintWastePct.toDouble(),
                            onValueChange = { v -> viewModel.updateSettings { it.copy(defaultPaintWastePct = v.toInt().coerceIn(0, 50)) } },
                            label = "Paint Waste %",
                            isInteger = true,
                            modifier = Modifier.weight(1f),
                            testTag = "settings_waste_paint"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThdrDoubleField(
                            value = settings.defaultDrywallWastePct.toDouble(),
                            onValueChange = { v -> viewModel.updateSettings { it.copy(defaultDrywallWastePct = v.toInt().coerceIn(0, 50)) } },
                            label = "Drywall Waste %",
                            isInteger = true,
                            modifier = Modifier.weight(1f),
                            testTag = "settings_waste_drywall"
                        )
                        ThdrDoubleField(
                            value = settings.defaultTileWastePct.toDouble(),
                            onValueChange = { v -> viewModel.updateSettings { it.copy(defaultTileWastePct = v.toInt().coerceIn(0, 50)) } },
                            label = "Tile Waste %",
                            isInteger = true,
                            modifier = Modifier.weight(1f),
                            testTag = "settings_waste_tile"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThdrDoubleField(
                            value = settings.defaultRoofingWastePct.toDouble(),
                            onValueChange = { v -> viewModel.updateSettings { it.copy(defaultRoofingWastePct = v.toInt().coerceIn(0, 50)) } },
                            label = "Roofing Waste %",
                            isInteger = true,
                            modifier = Modifier.weight(1f),
                            testTag = "settings_waste_roofing"
                        )
                        ThdrDoubleField(
                            value = settings.paintCoverageSqFtPerGal,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(paintCoverageSqFtPerGal = v.coerceAtLeast(50.0)) } },
                            label = "Paint SqFt / Gal",
                            modifier = Modifier.weight(1f),
                            testTag = "settings_paint_coverage"
                        )
                    }
                }
            }
        }

        // 5. Proposal & PDF Defaults
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "5. Proposal & PDF Defaults",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThdrDoubleField(
                            value = settings.defaultProposalValidDays.toDouble(),
                            onValueChange = { v -> viewModel.updateSettings { it.copy(defaultProposalValidDays = v.toInt().coerceIn(1, 365)) } },
                            label = "Proposal Valid (Days)",
                            isInteger = true,
                            modifier = Modifier.weight(1f),
                            testTag = "settings_proposal_days"
                        )
                        ThdrDoubleField(
                            value = settings.defaultProposalDepositPct,
                            onValueChange = { v -> viewModel.updateSettings { it.copy(defaultProposalDepositPct = v.coerceIn(0.0, 100.0)) } },
                            label = "Default Deposit (%)",
                            modifier = Modifier.weight(1f),
                            testTag = "settings_proposal_deposit"
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ThdrTextField(
                        value = settings.defaultProposalTerms,
                        onValueChange = { v -> viewModel.updateSettings { it.copy(defaultProposalTerms = v) } },
                        label = "Default Proposal Terms & Conditions",
                        singleLine = false,
                        minLines = 3,
                        testTag = "settings_proposal_terms"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingsSwitchRow(
                        title = "Include Signature Block on Proposals",
                        subtitle = "Print client & contractor signature lines on printed/PDF proposals",
                        checked = settings.includeSignatureBlockOnProposal,
                        onCheckedChange = { c -> viewModel.updateSettings { it.copy(includeSignatureBlockOnProposal = c) } },
                        testTag = "settings_switch_signature"
                    )
                }
            }
        }

        // 6. Privacy Policy & Backups
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "6. Privacy Policy & Data Handling",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customer records, estimates, and proposals stay locally on your device and are excluded from automatic cloud backups (allowBackup=\"false\").",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ThdrTextField(
                        value = settings.privacyPolicyUrl,
                        onValueChange = { v -> viewModel.updateSettings { it.copy(privacyPolicyUrl = v) } },
                        label = "Published Privacy Policy URL (https://...)",
                        testTag = "settings_privacy_url"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThdrPrimaryButton(
                            text = "View Privacy Policy",
                            onClick = onOpenPrivacyPolicy,
                            testTag = "settings_view_privacy_btn",
                            modifier = Modifier.weight(1f)
                        )
                        if (isValidHttpUrl(settings.privacyPolicyUrl)) {
                            ThdrSecondaryButton(
                                text = "Open Web URL",
                                onClick = { openExternalUrl(context, settings.privacyPolicyUrl) },
                                testTag = "settings_open_privacy_url_btn",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 7. Reset Settings
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "Reset Customizations",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Restore theme, company branding, waste factors, and proposal defaults without deleting saved Estimates, Proposals, Customers, or Price Book.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ThdrDangerButton(
                        text = "Reset All Settings to Defaults",
                        onClick = { showResetDialog = true },
                        testTag = "settings_reset_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        ConfirmActionDialog(
            title = "Reset Settings to Defaults?",
            message = "This will restore default theme, company branding, waste %, and proposal terms. Your saved Estimates, Proposals, Customers, and Price Book will remain safe.",
            confirmText = "Reset",
            onConfirm = { viewModel.resetSettingsToDefaults() },
            onDismiss = { showResetDialog = false }
        )
    }
}

@Composable
private fun SettingsSwitchRow(
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
