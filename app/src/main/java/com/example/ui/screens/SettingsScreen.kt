package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppAccentColor
import com.example.model.AppSettings
import com.example.model.AppThemePreset
import com.example.model.BrandLogoChoice
import com.example.model.BrandingSlot
import com.example.model.EstimateSide
import com.example.model.HeaderEmblemChoice
import com.example.model.HeroBackdropChoice
import com.example.model.MascotSlotChoice
import com.example.model.PdfLogoMode
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
import com.example.ui.theme.ThdrTheme
import com.example.viewmodel.EstimatorUiState
import com.example.viewmodel.EstimatorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    onOpenPrivacyPolicy: () -> Unit = {}
) {
    val context = LocalContext.current
    var draftSettings by remember { mutableStateOf(state.settings) }
    var lastCommittedSettings by remember { mutableStateOf(state.settings) }
    var pendingImageSlot by remember { mutableStateOf<BrandingSlot?>(null) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var backupStatusText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showPrivacySections by remember { mutableStateOf(false) }

    val exportSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            viewModel.exportSettingsBundleToUri(uri, draftSettings) { msg ->
                backupStatusText = msg
            }
        }
    }

    val importSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
        }
    }

    val companyLabel = draftSettings.companyName.ifBlank { "The Home Improvement / House DR" }
    val contactEmail = draftSettings.companyEmail.ifBlank { "thehousedrcny@gmail.com" }
    val exportHtmlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(
                        PrivacyPolicyDocument.buildStandaloneHtml(companyLabel, contactEmail)
                            .toByteArray(Charsets.UTF_8)
                    )
                }
                Toast.makeText(context, "Saved HTML Privacy Policy", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(context, "Could not save HTML file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(state.settings) {
        if (state.settings != lastCommittedSettings) {
            val wasClean = draftSettings == lastCommittedSettings
            lastCommittedSettings = state.settings
            if (wasClean) {
                draftSettings = state.settings
            }
        }
    }

    val hasUnsavedChanges = draftSettings != state.settings

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val slot = pendingImageSlot ?: return@rememberLauncherForActivityResult
        pendingImageSlot = null
        if (uri != null) {
            viewModel.importBrandingImage(slot, uri) { newPath ->
                if (!newPath.isNullOrBlank()) {
                    draftSettings = applyImportedImageToDraft(
                        draft = draftSettings,
                        saved = state.settings,
                        slot = slot,
                        newPath = newPath,
                        deleteOrphan = viewModel::deleteBrandingImage
                    )
                }
            }
        }
    }

    fun launchPickerFor(slot: BrandingSlot) {
        pendingImageSlot = slot
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun clearCustomImageFor(slot: BrandingSlot, path: String) {
        val savedPathForSlot = savedPathFor(state.settings, slot)
        if (path.isNotBlank() && path != savedPathForSlot) {
            viewModel.deleteBrandingImage(path)
        }
    }

    fun cleanupDraftOrphanImages() {
        listOf(
            BrandingSlot.HEADER_EMBLEM to draftSettings.customHeaderEmblemPath,
            BrandingSlot.BRAND_LOCKUP to draftSettings.customBrandLogoPath,
            BrandingSlot.HERO_BACKDROP to draftSettings.customBackdropPath,
            BrandingSlot.HOME_MASCOT to draftSettings.customMascotPath,
            BrandingSlot.PDF_LOGO to draftSettings.customPdfLogoPath
        ).forEach { (slot, path) ->
            if (path.isNotBlank() && path != savedPathFor(state.settings, slot)) {
                viewModel.deleteBrandingImage(path)
            }
        }
    }

    fun discardDraftChanges() {
        cleanupDraftOrphanImages()
        draftSettings = state.settings
    }

    fun commitDraftSettings() {
        lastCommittedSettings = draftSettings
        viewModel.saveSettings(draftSettings)
    }

    ThdrTheme(settings = draftSettings) {
        val palette = LocalThdrPalette.current

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header & Save Status Card
                item {
                    Box(modifier = Modifier.widthIn(max = 980.dp)) {
                        ThdrCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Settings & Customizations",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = palette.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(99.dp),
                                    color = if (hasUnsavedChanges) palette.accent.copy(alpha = 0.18f) else palette.primary.copy(alpha = 0.16f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (hasUnsavedChanges) palette.accent else palette.outline
                                    )
                                ) {
                                    Text(
                                        text = if (hasUnsavedChanges) "UNSAVED" else "SAVED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (hasUnsavedChanges) palette.textPrimary else palette.brandHighlight,
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                            .testTag("settings_dirty_badge")
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Customize company info, pre-positioned images & logos, app theme, and every section of your PDF Estimates & Proposals. Tap Save Customizations to apply.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textMuted
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ThdrSubCard {
                                Text(
                                    text = "Backup & Restore Configuration",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = palette.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Import a previously saved .zip or .json settings backup to restore your company details, theme, PDF layout, and custom branding images, or export a portable backup.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textMuted
                                )
                                if (backupStatusText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = backupStatusText,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = palette.brandHighlight,
                                        modifier = Modifier.testTag("settings_backup_status")
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ThdrPrimaryButton(
                                        text = "Import / Restore",
                                        onClick = {
                                            importSettingsLauncher.launch(
                                                arrayOf(
                                                    "application/zip",
                                                    "application/x-zip-compressed",
                                                    "application/json",
                                                    "application/octet-stream",
                                                    "*/*"
                                                )
                                            )
                                        },
                                        testTag = "settings_import_backup_btn",
                                        modifier = Modifier.weight(1f)
                                    )
                                    ThdrSecondaryButton(
                                        text = "Export Backup",
                                        onClick = { exportSettingsLauncher.launch("THDR_Settings_Backup.zip") },
                                        testTag = "settings_export_backup_btn",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 1. Company & Contact Info
                item {
                    Box(modifier = Modifier.widthIn(max = 980.dp)) {
                        ThdrCard {
                            Text(
                                text = "1. Company & Contact Info",
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThdrTextField(
                                    value = draftSettings.companyName,
                                    onValueChange = { v -> draftSettings = draftSettings.copy(companyName = v) },
                                    label = "Company / App Header Name",
                                    testTag = "settings_company_name"
                                )
                                ThdrTextField(
                                    value = draftSettings.companyTagline,
                                    onValueChange = { v -> draftSettings = draftSettings.copy(companyTagline = v) },
                                    label = "Header Tagline / Trade",
                                    testTag = "settings_company_tagline"
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ThdrTextField(
                                        value = draftSettings.companyPhone,
                                        onValueChange = { v -> draftSettings = draftSettings.copy(companyPhone = v) },
                                        label = "Business Phone",
                                        modifier = Modifier.weight(1f),
                                        testTag = "settings_company_phone"
                                    )
                                    ThdrTextField(
                                        value = draftSettings.companyLicense,
                                        onValueChange = { v -> draftSettings = draftSettings.copy(companyLicense = v) },
                                        label = "License / ROC #",
                                        modifier = Modifier.weight(1f),
                                        testTag = "settings_company_license"
                                    )
                                }
                                ThdrTextField(
                                    value = draftSettings.companyEmail,
                                    onValueChange = { v -> draftSettings = draftSettings.copy(companyEmail = v) },
                                    label = "Business Email",
                                    testTag = "settings_company_email"
                                )
                                ThdrTextField(
                                    value = draftSettings.companyAddress,
                                    onValueChange = { v -> draftSettings = draftSettings.copy(companyAddress = v) },
                                    label = "Business Address (shown on PDFs)",
                                    testTag = "settings_company_address"
                                )
                            }
                        }
                    }
                }

                // 2. Pre-Positioned Images & Logos
                item {
                    Box(modifier = Modifier.widthIn(max = 980.dp)) {
                        SettingsImagesAndLogosSection(
                            draft = draftSettings,
                            onUpdateDraft = { transform -> draftSettings = transform(draftSettings) },
                            onPickImageForSlot = ::launchPickerFor,
                            onClearCustomImage = ::clearCustomImageFor
                        )
                    }
                }

                // 3. Theme & Display
                item {
                    Box(modifier = Modifier.widthIn(max = 980.dp)) {
                        ThdrCard {
                            Text(
                                text = "3. Theme & Visual Appearance",
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
                                    val selected = draftSettings.themePreset == preset
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) palette.primary.copy(alpha = 0.22f) else palette.surfaceVariant.copy(alpha = 0.82f),
                                        border = BorderStroke(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) palette.brandHighlight else palette.outlineSubtle
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                draftSettings = draftSettings.copy(themePreset = preset)
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
                                    val selected = draftSettings.accentColor == accent
                                    Surface(
                                        shape = RoundedCornerShape(99.dp),
                                        color = if (selected) palette.primary.copy(alpha = 0.90f) else palette.surfaceVariant.copy(alpha = 0.80f),
                                        border = BorderStroke(1.dp, if (selected) palette.brandHighlight else palette.outline),
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 44.dp)
                                            .clip(RoundedCornerShape(99.dp))
                                            .clickable {
                                                draftSettings = draftSettings.copy(accentColor = accent)
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
                                subtitle = "Show architectural grid or custom photo behind workspace screens",
                                checked = draftSettings.showBlueprintBackdrop,
                                onCheckedChange = { c -> draftSettings = draftSettings.copy(showBlueprintBackdrop = c) },
                                testTag = "settings_switch_blueprint"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SettingsSwitchRow(
                                title = "Large Field Readability Text",
                                subtitle = "Increase text sizes across screens for bright job sites",
                                checked = draftSettings.largeFieldText,
                                onCheckedChange = { c -> draftSettings = draftSettings.copy(largeFieldText = c) },
                                testTag = "settings_switch_large_text"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SettingsSwitchRow(
                                title = "Show Internal Job Cost & Profit",
                                subtitle = "Display internal contractor cost & margin line on Estimate Summary",
                                checked = draftSettings.showInternalCostProfit,
                                onCheckedChange = { c -> draftSettings = draftSettings.copy(showInternalCostProfit = c) },
                                testTag = "settings_switch_internal_profit"
                            )
                        }
                    }
                }

                // 4. Complete PDF Estimate & Proposal Customizer
                item {
                    Box(modifier = Modifier.widthIn(max = 980.dp)) {
                        SettingsPdfCustomizationSection(
                            draft = draftSettings,
                            onUpdateDraft = { transform -> draftSettings = transform(draftSettings) },
                            onPickImageForSlot = ::launchPickerFor,
                            onClearCustomImage = ::clearCustomImageFor
                        )
                    }
                }

                // 5. Pricing & Estimate Defaults
                item {
                    Box(modifier = Modifier.widthIn(max = 980.dp)) {
                        ThdrCard {
                            Text(
                                text = "5. Pricing & Estimate Defaults",
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ThdrDoubleField(
                                    value = draftSettings.defaultHourlyRate,
                                    onValueChange = { v -> draftSettings = draftSettings.copy(defaultHourlyRate = v) },
                                    label = "Default Hourly Rate",
                                    modifier = Modifier.weight(1f),
                                    testTag = "settings_hourly_rate"
                                )
                                ThdrDoubleField(
                                    value = draftSettings.defaultTaxPercent,
                                    onValueChange = { v -> draftSettings = draftSettings.copy(defaultTaxPercent = v) },
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
                                        selected = draftSettings.currencySymbol == sym,
                                        onClick = { draftSettings = draftSettings.copy(currencySymbol = sym) },
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
                                    selected = draftSettings.defaultEstimateSide == EstimateSide.INTERIOR,
                                    onClick = { draftSettings = draftSettings.copy(defaultEstimateSide = EstimateSide.INTERIOR) },
                                    testTag = "settings_side_interior"
                                )
                                SelectablePill(
                                    text = "Exterior",
                                    selected = draftSettings.defaultEstimateSide == EstimateSide.EXTERIOR,
                                    onClick = { draftSettings = draftSettings.copy(defaultEstimateSide = EstimateSide.EXTERIOR) },
                                    testTag = "settings_side_exterior"
                                )
                            }
                        }
                    }
                }

                // 6. Material Waste & Coverage Defaults
                item {
                    Box(modifier = Modifier.widthIn(max = 980.dp)) {
                        ThdrCard {
                            Text(
                                text = "6. Material Waste & Coverage Defaults",
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
                                    value = draftSettings.defaultFlooringWastePct.toDouble(),
                                    onValueChange = { v -> draftSettings = draftSettings.copy(defaultFlooringWastePct = v.toInt().coerceIn(0, 50)) },
                                    label = "Flooring Waste %",
                                    isInteger = true,
                                    modifier = Modifier.weight(1f),
                                    testTag = "settings_waste_flooring"
                                )
                                ThdrDoubleField(
                                    value = draftSettings.defaultPaintWastePct.toDouble(),
                                    onValueChange = { v -> draftSettings = draftSettings.copy(defaultPaintWastePct = v.toInt().coerceIn(0, 50)) },
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
                                    value = draftSettings.defaultDrywallWastePct.toDouble(),
                                    onValueChange = { v -> draftSettings = draftSettings.copy(defaultDrywallWastePct = v.toInt().coerceIn(0, 50)) },
                                    label = "Drywall Waste %",
                                    isInteger = true,
                                    modifier = Modifier.weight(1f),
                                    testTag = "settings_waste_drywall"
                                )
                                ThdrDoubleField(
                                    value = draftSettings.defaultTileWastePct.toDouble(),
                                    onValueChange = { v -> draftSettings = draftSettings.copy(defaultTileWastePct = v.toInt().coerceIn(0, 50)) },
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
                                    value = draftSettings.defaultRoofingWastePct.toDouble(),
                                    onValueChange = { v -> draftSettings = draftSettings.copy(defaultRoofingWastePct = v.toInt().coerceIn(0, 50)) },
                                    label = "Roofing Waste %",
                                    isInteger = true,
                                    modifier = Modifier.weight(1f),
                                    testTag = "settings_waste_roofing"
                                )
                                ThdrDoubleField(
                                    value = draftSettings.paintCoverageSqFtPerGal,
                                    onValueChange = { v -> draftSettings = draftSettings.copy(paintCoverageSqFtPerGal = v.coerceAtLeast(50.0)) },
                                    label = "Paint SqFt / Gal",
                                    modifier = Modifier.weight(1f),
                                    testTag = "settings_paint_coverage"
                                )
                            }
                        }
                    }
                }

                // 7. Proposal Terms & Signatures
                item {
                    Box(modifier = Modifier.widthIn(max = 980.dp)) {
                        ThdrCard {
                            Text(
                                text = "7. Proposal Terms & Deposit Defaults",
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ThdrDoubleField(
                                    value = draftSettings.defaultProposalValidDays.toDouble(),
                                    onValueChange = { v -> draftSettings = draftSettings.copy(defaultProposalValidDays = v.toInt().coerceIn(1, 365)) },
                                    label = "Proposal Valid (Days)",
                                    isInteger = true,
                                    modifier = Modifier.weight(1f),
                                    testTag = "settings_proposal_days"
                                )
                                ThdrDoubleField(
                                    value = draftSettings.defaultProposalDepositPct,
                                    onValueChange = { v -> draftSettings = draftSettings.copy(defaultProposalDepositPct = v.coerceIn(0.0, 100.0)) },
                                    label = "Default Deposit (%)",
                                    modifier = Modifier.weight(1f),
                                    testTag = "settings_proposal_deposit"
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ThdrTextField(
                                value = draftSettings.defaultProposalTerms,
                                onValueChange = { v -> draftSettings = draftSettings.copy(defaultProposalTerms = v) },
                                label = "Default Proposal Terms & Conditions",
                                singleLine = false,
                                minLines = 3,
                                testTag = "settings_proposal_terms"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            SettingsSwitchRow(
                                title = "Include Signature Block on Proposals",
                                subtitle = "Print client & contractor signature lines on printed/PDF proposals",
                                checked = draftSettings.includeSignatureBlockOnProposal,
                                onCheckedChange = { c -> draftSettings = draftSettings.copy(includeSignatureBlockOnProposal = c) },
                                testTag = "settings_switch_signature"
                            )
                        }
                    }
                }

                // 8. Integrated Privacy Policy, Data Controls & Reset
                item {
                    Box(modifier = Modifier.widthIn(max = 980.dp)) {
                        ThdrCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = palette.primary.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, palette.outline)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Privacy Policy & Data Controls",
                                        tint = palette.brandHighlight,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "8. Privacy Policy & Data Controls",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = "Effective ${PrivacyPolicyDocument.EFFECTIVE_DATE} · On-Device Storage · allowBackup=\"false\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.brandHighlight
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Customer records, estimates, proposals, Price Book items, and uploaded logos remain strictly on your device and are excluded from automatic cloud backups.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = palette.surfaceVariant.copy(alpha = 0.82f),
                                border = BorderStroke(1.dp, palette.outlineSubtle),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showPrivacySections = !showPrivacySections }
                                    .testTag("settings_toggle_privacy_details")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (showPrivacySections) "Hide Full Policy Details" else "Read Full Privacy Policy & Data Controls",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = palette.textPrimary
                                        )
                                        Text(
                                            text = "Covers local storage, customer data, retention, backups & deletion",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.textMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (showPrivacySections) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (showPrivacySections) "Collapse privacy details" else "Expand privacy details",
                                        tint = palette.brandHighlight
                                    )
                                }
                            }

                            if (showPrivacySections) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PrivacyPolicyDocument.sections(contactEmail).forEach { sec ->
                                        ThdrSubCard {
                                            Text(
                                                text = sec.title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = palette.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = sec.body,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.textMuted
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            ThdrTextField(
                                value = draftSettings.privacyPolicyUrl,
                                onValueChange = { v -> draftSettings = draftSettings.copy(privacyPolicyUrl = v) },
                                label = "Published Privacy Policy URL (https://...)",
                                testTag = "settings_privacy_url"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ThdrPrimaryButton(
                                    text = "Open Full Screen",
                                    onClick = onOpenPrivacyPolicy,
                                    testTag = "settings_view_privacy_btn",
                                    modifier = Modifier.weight(1f)
                                )
                                if (isValidHttpUrl(draftSettings.privacyPolicyUrl)) {
                                    ThdrSecondaryButton(
                                        text = "Open Web URL",
                                        onClick = { openExternalUrl(context, draftSettings.privacyPolicyUrl) },
                                        testTag = "settings_open_privacy_url_btn",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ThdrSecondaryButton(
                                    text = "Export HTML Policy",
                                    onClick = { exportHtmlLauncher.launch("thdr-privacy-policy.html") },
                                    testTag = "settings_export_privacy_html_btn",
                                    modifier = Modifier.weight(1f)
                                )
                                ThdrSecondaryButton(
                                    text = "Copy Policy Text",
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(
                                            ClipData.newPlainText(
                                                "THDR Privacy Policy",
                                                PrivacyPolicyDocument.buildPlainText(companyLabel, contactEmail)
                                            )
                                        )
                                        Toast.makeText(context, "Privacy Policy copied", Toast.LENGTH_SHORT).show()
                                    },
                                    testTag = "settings_copy_privacy_text_btn",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            ThdrDangerButton(
                                text = "Reset All Settings & Logos to Defaults",
                                onClick = { showResetDialog = true },
                                testTag = "settings_reset_btn",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Sticky Bottom Save Bar
            HorizontalDivider(thickness = 1.dp, color = palette.outline)
            Surface(
                color = palette.surface.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.widthIn(max = 980.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasUnsavedChanges) {
                            ThdrSecondaryButton(
                                text = "Discard",
                                onClick = ::discardDraftChanges,
                                testTag = "settings_discard_btn"
                            )
                        }
                        ThdrPrimaryButton(
                            text = if (hasUnsavedChanges) "Save Customizations" else "Saved",
                            onClick = ::commitDraftSettings,
                            orangeAccent = hasUnsavedChanges,
                            testTag = "settings_save_btn",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (showResetDialog) {
            ConfirmActionDialog(
                title = "Reset Settings to Defaults?",
                message = "This will restore default theme, company branding, uploaded logos, PDF customization, waste %, and proposal terms. Your saved Estimates, Proposals, Customers, and Price Book will remain safe.",
                confirmText = "Reset",
                onConfirm = {
                    val defaults = AppSettings()
                    lastCommittedSettings = defaults
                    draftSettings = defaults
                    viewModel.resetSettingsToDefaults()
                },
                onDismiss = { showResetDialog = false }
            )
        }

        pendingRestoreUri?.let { restoreUri ->
            ConfirmActionDialog(
                title = "Restore Settings & Images?",
                message = "Importing this backup will overwrite your current Settings, theme, PDF layout, and custom branding images. Your saved Estimates, Proposals, Customers, and Price Book will not be changed.",
                confirmText = "Restore",
                onConfirm = {
                    viewModel.importSettingsBundleFromUri(
                        uri = restoreUri,
                        onRestored = { restored, imgCount ->
                            cleanupDraftOrphanImages()
                            lastCommittedSettings = restored
                            draftSettings = restored
                            backupStatusText = if (imgCount > 0) {
                                "Restored settings & $imgCount custom image(s)"
                            } else {
                                "Restored settings configuration"
                            }
                        },
                        onError = { err ->
                            backupStatusText = err
                        }
                    )
                },
                onDismiss = { pendingRestoreUri = null }
            )
        }
    }
}

private fun savedPathFor(settings: AppSettings, slot: BrandingSlot): String = when (slot) {
    BrandingSlot.HEADER_EMBLEM -> settings.customHeaderEmblemPath
    BrandingSlot.BRAND_LOCKUP -> settings.customBrandLogoPath
    BrandingSlot.HERO_BACKDROP -> settings.customBackdropPath
    BrandingSlot.HOME_MASCOT -> settings.customMascotPath
    BrandingSlot.PDF_LOGO -> settings.customPdfLogoPath
}

private fun applyImportedImageToDraft(
    draft: AppSettings,
    saved: AppSettings,
    slot: BrandingSlot,
    newPath: String,
    deleteOrphan: (String) -> Unit
): AppSettings {
    val previousDraftPath = savedPathFor(draft, slot)
    if (previousDraftPath.isNotBlank() && previousDraftPath != savedPathFor(saved, slot)) {
        deleteOrphan(previousDraftPath)
    }
    return when (slot) {
        BrandingSlot.HEADER_EMBLEM -> draft.copy(
            customHeaderEmblemPath = newPath,
            headerEmblemChoice = HeaderEmblemChoice.CUSTOM
        )
        BrandingSlot.BRAND_LOCKUP -> draft.copy(
            customBrandLogoPath = newPath,
            brandLogoChoice = BrandLogoChoice.CUSTOM
        )
        BrandingSlot.HERO_BACKDROP -> draft.copy(
            customBackdropPath = newPath,
            heroBackdropChoice = HeroBackdropChoice.CUSTOM
        )
        BrandingSlot.HOME_MASCOT -> draft.copy(
            customMascotPath = newPath,
            mascotChoice = MascotSlotChoice.CUSTOM
        )
        BrandingSlot.PDF_LOGO -> draft.copy(
            customPdfLogoPath = newPath,
            pdfLogoMode = PdfLogoMode.CUSTOM_PDF_IMAGE
        )
    }
}
