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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppSettings
import com.example.model.ColorHexCodec
import com.example.model.CustomColorScheme
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.SelectablePill
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.LocalThdrPalette
import kotlin.math.roundToInt

private enum class SchemeColorSlot(val label: String) {
    PRIMARY("Primary"),
    SECONDARY("Secondary"),
    ACCENT("Accent"),
    BACKGROUND("Backdrop"),
    SURFACE("Card Surface"),
    TEXT_PRIMARY("Primary Text"),
    TEXT_MUTED("Muted Text"),
    OUTLINE("Outline")
}

private val CURATED_SWATCH_HEXES = listOf(
    "#2F5D8A",
    "#5E8FB9",
    "#CDE3F5",
    "#E6E7EA",
    "#FFFFFF",
    "#F5832B",
    "#E8A460",
    "#FFD88A",
    "#6B8A54",
    "#8FAA77",
    "#A67C52",
    "#4A4A4A",
    "#1D2B3A",
    "#1B334D",
    "#233F5E",
    "#C23B36"
)

@Composable
fun CustomColorSchemesSection(
    schemes: List<CustomColorScheme>,
    activeCustomSchemeId: String,
    onCreateScheme: () -> Unit,
    onImportScheme: () -> Unit,
    onApplyScheme: (CustomColorScheme) -> Unit,
    onEditScheme: (CustomColorScheme) -> Unit,
    onDuplicateScheme: (CustomColorScheme) -> Unit,
    onExportScheme: (CustomColorScheme) -> Unit,
    onDeleteScheme: (CustomColorScheme) -> Unit
) {
    val palette = LocalThdrPalette.current
    var pendingDelete by remember { mutableStateOf<CustomColorScheme?>(null) }

    ThdrSubCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Custom Color Schemes",
                    tint = palette.brandHighlight,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "My Custom Color Schemes",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.textPrimary
                )
            }
            Surface(
                shape = RoundedCornerShape(99.dp),
                color = palette.primary.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, palette.outline)
            ) {
                Text(
                    text = "${schemes.size} SAVED",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.brandHighlight,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Name, design, duplicate, upload, export, or delete your own complete color schemes including card & navigation translucency.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textMuted
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThdrPrimaryButton(
                text = "+ Create Scheme",
                onClick = onCreateScheme,
                testTag = "settings_create_scheme_btn",
                modifier = Modifier.weight(1f)
            )
            ThdrSecondaryButton(
                text = "Upload Scheme",
                onClick = onImportScheme,
                testTag = "settings_import_scheme_btn",
                modifier = Modifier.weight(1f)
            )
        }

        if (schemes.isEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No custom color schemes saved yet. Tap \"+ Create Scheme\" to build one from any starter theme or upload a .json scheme file.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textMuted
            )
        } else {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                schemes.forEach { scheme ->
                    val isActive = scheme.id == activeCustomSchemeId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isActive) {
                            palette.primary.copy(alpha = 0.22f)
                        } else {
                            palette.surface.copy(alpha = 0.80f)
                        },
                        border = BorderStroke(
                            width = if (isActive) 2.dp else 1.dp,
                            color = if (isActive) palette.brandHighlight else palette.outlineSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onApplyScheme(scheme) }
                            .testTag("settings_custom_scheme_${scheme.id}")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = scheme.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = palette.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(99.dp),
                                            color = palette.surfaceHigh.copy(alpha = 0.72f),
                                            border = BorderStroke(1.dp, palette.outlineSubtle)
                                        ) {
                                            Text(
                                                text = if (scheme.isDark) "Dark" else "Light",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = palette.textMuted,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        listOf(
                                            scheme.primaryHex,
                                            scheme.secondaryHex,
                                            scheme.accentHex,
                                            scheme.backgroundHex,
                                            scheme.surfaceHex
                                        ).forEach { hex ->
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Color(ColorHexCodec.parseArgbLong(hex, 0xFF2F5D8AL))
                                                    )
                                                    .border(
                                                        1.dp,
                                                        palette.outline,
                                                        CircleShape
                                                    )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Cards ${scheme.cardOpacityPct}% · Nav ${scheme.navOpacityPct}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.textMuted
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(99.dp),
                                    color = if (isActive) palette.accent.copy(alpha = 0.22f) else palette.primary.copy(alpha = 0.14f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isActive) palette.accent else palette.outline
                                    ),
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 36.dp)
                                        .clip(RoundedCornerShape(99.dp))
                                        .clickable { onApplyScheme(scheme) }
                                        .testTag("scheme_apply_${scheme.id}")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (isActive) "ACTIVE" else "Apply",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isActive) palette.textPrimary else palette.brandHighlight
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onEditScheme(scheme) },
                                    modifier = Modifier.testTag("scheme_edit_${scheme.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit ${scheme.name}",
                                        tint = palette.brandHighlight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDuplicateScheme(scheme) },
                                    modifier = Modifier.testTag("scheme_duplicate_${scheme.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Duplicate ${scheme.name}",
                                        tint = palette.textMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onExportScheme(scheme) },
                                    modifier = Modifier.testTag("scheme_export_${scheme.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Export ${scheme.name}",
                                        tint = palette.textMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { pendingDelete = scheme },
                                    modifier = Modifier.testTag("scheme_delete_${scheme.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete ${scheme.name}",
                                        tint = palette.danger,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { target ->
        ConfirmActionDialog(
            title = "Delete Color Scheme?",
            message = "Remove \"${target.name}\" from your custom color schemes?",
            confirmText = "Delete",
            onConfirm = { onDeleteScheme(target) },
            onDismiss = { pendingDelete = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomColorSchemeEditorDialog(
    initialScheme: CustomColorScheme,
    isEditingExisting: Boolean,
    currentSettingsSnapshot: AppSettings,
    onSave: (CustomColorScheme) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalThdrPalette.current
    var draft by remember(initialScheme.id) { mutableStateOf(initialScheme) }
    var selectedSlot by remember { mutableStateOf(SchemeColorSlot.PRIMARY) }

    fun hexForSlot(scheme: CustomColorScheme, slot: SchemeColorSlot): String = when (slot) {
        SchemeColorSlot.PRIMARY -> scheme.primaryHex
        SchemeColorSlot.SECONDARY -> scheme.secondaryHex
        SchemeColorSlot.ACCENT -> scheme.accentHex
        SchemeColorSlot.BACKGROUND -> scheme.backgroundHex
        SchemeColorSlot.SURFACE -> scheme.surfaceHex
        SchemeColorSlot.TEXT_PRIMARY -> scheme.textPrimaryHex
        SchemeColorSlot.TEXT_MUTED -> scheme.textMutedHex
        SchemeColorSlot.OUTLINE -> scheme.outlineHex
    }

    fun updateSlotHex(scheme: CustomColorScheme, slot: SchemeColorSlot, hex: String): CustomColorScheme =
        when (slot) {
            SchemeColorSlot.PRIMARY -> scheme.copy(primaryHex = hex)
            SchemeColorSlot.SECONDARY -> scheme.copy(secondaryHex = hex)
            SchemeColorSlot.ACCENT -> scheme.copy(accentHex = hex)
            SchemeColorSlot.BACKGROUND -> scheme.copy(backgroundHex = hex)
            SchemeColorSlot.SURFACE -> scheme.copy(surfaceHex = hex)
            SchemeColorSlot.TEXT_PRIMARY -> scheme.copy(textPrimaryHex = hex)
            SchemeColorSlot.TEXT_MUTED -> scheme.copy(textMutedHex = hex)
            SchemeColorSlot.OUTLINE -> scheme.copy(outlineHex = hex)
        }

    val activeHex = hexForSlot(draft, selectedSlot)
    var hexInput by remember(selectedSlot, activeHex) { mutableStateOf(activeHex) }
    val hsv = remember(activeHex) {
        val arr = FloatArray(3)
        val argb = Color(ColorHexCodec.parseArgbLong(activeHex, 0xFF2F5D8AL)).toArgb()
        android.graphics.Color.colorToHSV(argb, arr)
        arr
    }
    var hue by remember(selectedSlot, activeHex) { mutableStateOf(hsv[0]) }
    var saturation by remember(selectedSlot, activeHex) { mutableStateOf(hsv[1]) }
    var brightness by remember(selectedSlot, activeHex) { mutableStateOf(hsv[2]) }

    LaunchedEffect(selectedSlot, activeHex) {
        hexInput = activeHex
        val arr = FloatArray(3)
        val argb = Color(ColorHexCodec.parseArgbLong(activeHex, 0xFF2F5D8AL)).toArgb()
        android.graphics.Color.colorToHSV(argb, arr)
        hue = arr[0]
        saturation = arr[1]
        brightness = arr[2]
    }

    fun applyHsv(h: Float, s: Float, v: Float) {
        hue = h
        saturation = s
        brightness = v
        val intColor = android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))
        val newHex = ColorHexCodec.argbLongToHex(0xFF000000L or (intColor.toLong() and 0xFFFFFFL))
        hexInput = newHex
        draft = updateSlotHex(draft, selectedSlot, newHex)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = palette.surface.copy(alpha = 0.96f),
            border = BorderStroke(1.5.dp, palette.outline),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 680.dp)
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isEditingExisting) "Edit Color Scheme" else "Create Custom Color Scheme",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Name your scheme, pick Light or Dark contrast, and tune every surface, accent, text, and translucency level.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThdrTextField(
                        value = draft.name,
                        onValueChange = { draft = draft.copy(name = it) },
                        label = "Color Scheme Name",
                        placeholder = "e.g. Custom Slate Studio",
                        testTag = "scheme_editor_name_input"
                    )

                    // Base Mode + Quick Starters
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Base Mode & Starter Templates",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textMuted
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SelectablePill(
                                text = "Light Base",
                                selected = !draft.isDark,
                                onClick = { draft = draft.copy(isDark = false) },
                                testTag = "scheme_editor_mode_light"
                            )
                            SelectablePill(
                                text = "Dark Base",
                                selected = draft.isDark,
                                onClick = { draft = draft.copy(isDark = true) },
                                testTag = "scheme_editor_mode_dark"
                            )
                            SelectablePill(
                                text = "Load Active",
                                selected = false,
                                onClick = {
                                    draft = currentSettingsSnapshot.starterSchemeFromCurrent(
                                        newId = draft.id,
                                        name = draft.name.ifBlank { "Custom Studio" }
                                    )
                                },
                                testTag = "scheme_editor_load_active"
                            )
                            SelectablePill(
                                text = "House DR Light",
                                selected = false,
                                onClick = {
                                    draft = draft.copy(
                                        isDark = false,
                                        primaryHex = "#2F5D8A",
                                        secondaryHex = "#5E8FB9",
                                        accentHex = "#F5832B",
                                        backgroundHex = "#E6E7EA",
                                        surfaceHex = "#FFFFFF",
                                        textPrimaryHex = "#1D2B3A",
                                        textMutedHex = "#4A4A4A",
                                        outlineHex = "#5E8FB9",
                                        cardOpacityPct = 86,
                                        navOpacityPct = 85
                                    )
                                },
                                testTag = "scheme_editor_preset_light"
                            )
                            SelectablePill(
                                text = "Blueprint Dark",
                                selected = false,
                                onClick = {
                                    draft = draft.copy(
                                        isDark = true,
                                        primaryHex = "#2F5D8A",
                                        secondaryHex = "#5E8FB9",
                                        accentHex = "#F5832B",
                                        backgroundHex = "#1B334D",
                                        surfaceHex = "#233F5E",
                                        textPrimaryHex = "#FFFFFF",
                                        textMutedHex = "#CDE3F5",
                                        outlineHex = "#5E8FB9",
                                        cardOpacityPct = 85,
                                        navOpacityPct = 85
                                    )
                                },
                                testTag = "scheme_editor_preset_dark"
                            )
                        }
                    }

                    // Live Mini App Preview
                    CustomSchemeMiniPreview(scheme = draft)

                    // Color Slot Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Select Color Channel to Edit",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textMuted
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SchemeColorSlot.entries.forEach { slot ->
                                val selected = selectedSlot == slot
                                val slotColor = Color(
                                    ColorHexCodec.parseArgbLong(hexForSlot(draft, slot), 0xFF2F5D8AL)
                                )
                                Surface(
                                    shape = RoundedCornerShape(99.dp),
                                    color = if (selected) palette.primary.copy(alpha = 0.88f) else palette.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) palette.brandHighlight else palette.outline
                                    ),
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 38.dp)
                                        .clip(RoundedCornerShape(99.dp))
                                        .clickable { selectedSlot = slot }
                                        .testTag("scheme_slot_${slot.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(slotColor)
                                                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                        )
                                        Text(
                                            text = slot.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) Color.White else palette.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Curated Swatches + HSV + Hex Input
                    ThdrSubCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Editing: ${selectedSlot.label}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = palette.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(ColorHexCodec.parseArgbLong(activeHex, 0xFF2F5D8AL)))
                                        .border(1.5.dp, palette.outline, CircleShape)
                                )
                                Text(
                                    text = activeHex,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.brandHighlight
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CURATED_SWATCH_HEXES.forEach { swatchHex ->
                                val isCurrent = activeHex.equals(swatchHex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(ColorHexCodec.parseArgbLong(swatchHex, 0xFF2F5D8AL)))
                                        .border(
                                            width = if (isCurrent) 2.5.dp else 1.dp,
                                            color = if (isCurrent) palette.accent else palette.outline,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            draft = updateSlotHex(draft, selectedSlot, swatchHex)
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Hue (${hue.roundToInt()}°)",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                        Slider(
                            value = hue,
                            onValueChange = { applyHsv(it, saturation, brightness) },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.accent,
                                activeTrackColor = palette.primary
                            )
                        )
                        Text(
                            text = "Saturation (${(saturation * 100f).roundToInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                        Slider(
                            value = saturation,
                            onValueChange = { applyHsv(hue, it, brightness) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.accent,
                                activeTrackColor = palette.primary
                            )
                        )
                        Text(
                            text = "Brightness (${(brightness * 100f).roundToInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                        Slider(
                            value = brightness,
                            onValueChange = { applyHsv(hue, saturation, it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.accent,
                                activeTrackColor = palette.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        ThdrTextField(
                            value = hexInput,
                            onValueChange = { raw ->
                                hexInput = raw
                                if (ColorHexCodec.isValidHex(raw)) {
                                    val normalized = ColorHexCodec.normalizeHex(raw, activeHex)
                                    draft = updateSlotHex(draft, selectedSlot, normalized)
                                }
                            },
                            label = "Hex Color (#RRGGBB)",
                            placeholder = "#2F5D8A",
                            testTag = "scheme_editor_hex_input"
                        )
                    }

                    // Translucency Sliders
                    ThdrSubCard {
                        Text(
                            text = "Card & Box Opacity: ${draft.cardOpacityPct}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary
                        )
                        Slider(
                            value = draft.cardOpacityPct.toFloat(),
                            onValueChange = {
                                draft = draft.copy(cardOpacityPct = it.roundToInt().coerceIn(55, 100))
                            },
                            valueRange = 55f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.accent,
                                activeTrackColor = palette.primary
                            )
                        )
                        Text(
                            text = "Navigation Tabs & Header Opacity: ${draft.navOpacityPct}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary
                        )
                        Slider(
                            value = draft.navOpacityPct.toFloat(),
                            onValueChange = {
                                draft = draft.copy(navOpacityPct = it.roundToInt().coerceIn(55, 100))
                            },
                            valueRange = 55f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.accent,
                                activeTrackColor = palette.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThdrSecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        testTag = "scheme_editor_cancel_btn",
                        modifier = Modifier.weight(1f)
                    )
                    ThdrPrimaryButton(
                        text = "Save & Apply",
                        orangeAccent = true,
                        onClick = {
                            val cleanName = draft.name.trim().ifBlank { "Custom Scheme" }
                            onSave(draft.copy(name = cleanName))
                        },
                        testTag = "scheme_editor_save_btn",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomSchemeMiniPreview(scheme: CustomColorScheme) {
    val primary = Color(ColorHexCodec.parseArgbLong(scheme.primaryHex, 0xFF2F5D8AL))
    val secondary = Color(ColorHexCodec.parseArgbLong(scheme.secondaryHex, 0xFF5E8FB9L))
    val accent = Color(ColorHexCodec.parseArgbLong(scheme.accentHex, 0xFFF5832BL))
    val bg = Color(ColorHexCodec.parseArgbLong(scheme.backgroundHex, 0xFFE6E7EAL))
    val surface = Color(ColorHexCodec.parseArgbLong(scheme.surfaceHex, 0xFFFFFFFFL))
    val textPrimary = Color(ColorHexCodec.parseArgbLong(scheme.textPrimaryHex, 0xFF1D2B3AL))
    val textMuted = Color(ColorHexCodec.parseArgbLong(scheme.textMutedHex, 0xFF4A4A4AL))
    val outline = Color(ColorHexCodec.parseArgbLong(scheme.outlineHex, 0xFF5E8FB9L))
    val cardAlpha = scheme.cardOpacityPct.coerceIn(55, 100) / 100f
    val navAlpha = scheme.navOpacityPct.coerceIn(55, 100) / 100f
    val headerTextColor = if (ColorHexCodec.relativeLuminance(scheme.primaryHex) > 0.68f) {
        Color(0xFF1D2B3A)
    } else {
        Color.White
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, outline, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            primary.copy(alpha = (navAlpha + 0.05f).coerceAtMost(0.96f)),
                            secondary.copy(alpha = navAlpha)
                        )
                    )
                )
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = scheme.name.ifBlank { "Custom Color Scheme" },
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = headerTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (scheme.isDark) "DARK" else "LIGHT",
                style = MaterialTheme.typography.labelSmall,
                color = headerTextColor.copy(alpha = 0.85f)
            )
        }

        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(surface.copy(alpha = cardAlpha))
                .border(1.dp, outline.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Live Card Surface Preview",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = textPrimary
            )
            Text(
                text = "Muted subtitle and line-item preview text on your card surface.",
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(primary)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Primary",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Accent CTA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surface.copy(alpha = navAlpha))
                .border(0.5.dp, outline.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Home", style = MaterialTheme.typography.labelSmall, color = primary)
            Text("Estimate", style = MaterialTheme.typography.labelSmall, color = textMuted)
            Text("Proposals", style = MaterialTheme.typography.labelSmall, color = textMuted)
            Text("Price Book", style = MaterialTheme.typography.labelSmall, color = textMuted)
        }
    }
}
