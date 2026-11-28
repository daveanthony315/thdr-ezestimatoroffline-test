package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.BrandingImageStore
import com.example.model.HeaderEmblemChoice
import com.example.model.HeroBackdropChoice
import com.example.ui.theme.LocalThdrPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun BrandingImage(
    customPath: String,
    @DrawableRes fallbackResId: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    maxDecodePx: Int = 720
) {
    val customBitmap by produceState<ImageBitmap?>(initialValue = null, customPath, maxDecodePx) {
        value = if (customPath.isNotBlank()) {
            withContext(Dispatchers.IO) {
                BrandingImageStore.loadScaledBitmapFromPath(customPath, maxDecodePx)?.asImageBitmap()
            }
        } else {
            null
        }
    }

    if (customBitmap != null) {
        Image(
            bitmap = customBitmap!!,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else if (fallbackResId != null) {
        Image(
            painter = painterResource(id = fallbackResId),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

@Composable
fun BlueprintBackdrop(
    showBlueprintGrid: Boolean = true,
    heroBackdropChoice: HeroBackdropChoice = HeroBackdropChoice.DEFAULT_BLUEPRINT,
    customBackdropPath: String = "",
    backdropOpacityPct: Int = 18,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = LocalThdrPalette.current
    val effectiveAlpha = (backdropOpacityPct.coerceIn(5, 45) / 100f)
    val enhancedImageAlpha = if (palette.isDark) {
        (effectiveAlpha * 1.45f + 0.08f).coerceIn(0.16f, 0.62f)
    } else {
        (effectiveAlpha * 1.50f).coerceIn(0.14f, 0.58f)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        if (showBlueprintGrid && heroBackdropChoice != HeroBackdropChoice.GRADIENT_ONLY) {
            val useCustom = heroBackdropChoice == HeroBackdropChoice.CUSTOM && customBackdropPath.isNotBlank()
            BrandingImage(
                customPath = if (useCustom) customBackdropPath else "",
                fallbackResId = R.drawable.bg_blueprint,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                maxDecodePx = 1024,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(enhancedImageAlpha)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors = palette.backdropGradient))
        )
        content()
    }
}

@Composable
fun ThdrHeaderBar(
    savedStatus: String,
    companyName: String = "EZ ESTIMATOR",
    companyTagline: String = "HOUSE DR · INTERIOR / EXTERIOR",
    headerEmblemChoice: HeaderEmblemChoice = HeaderEmblemChoice.DEFAULT_EMBLEM,
    customHeaderEmblemPath: String = "",
    onLogoClick: (() -> Unit)? = null,
    onOpenCustomers: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalThdrPalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(colors = palette.headerGradient))
                .border(width = 0.5.dp, color = palette.outline)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .then(
                        if (onLogoClick != null) {
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onLogoClick)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("header_home_btn")
                        } else Modifier
                    )
            ) {
                if (headerEmblemChoice != HeaderEmblemChoice.HIDDEN) {
                    val emblemFallbackRes = when (headerEmblemChoice) {
                        HeaderEmblemChoice.MASCOT_BADGE -> R.drawable.thdr_mascot_badge
                        HeaderEmblemChoice.FULL_LOCKUP -> R.drawable.thdr_brand_logo
                        else -> R.drawable.thdr_logo
                    }
                    val customPath = if (headerEmblemChoice == HeaderEmblemChoice.CUSTOM) customHeaderEmblemPath else ""
                    val logoModifier = if (headerEmblemChoice == HeaderEmblemChoice.FULL_LOCKUP) {
                        Modifier
                            .size(width = 56.dp, height = 38.dp)
                            .clip(RoundedCornerShape(8.dp))
                    } else {
                        Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                    }
                    BrandingImage(
                        customPath = customPath,
                        fallbackResId = emblemFallbackRes,
                        contentDescription = "Company logo",
                        contentScale = ContentScale.Fit,
                        maxDecodePx = 256,
                        modifier = logoModifier
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column {
                    Text(
                        text = companyName.ifBlank { "EZ ESTIMATOR" }.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = palette.headerText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = companyTagline.ifBlank { "HOUSE DR · INTERIOR / EXTERIOR" }.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.1.sp,
                            fontSize = 10.sp
                        ),
                        color = palette.headerMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = palette.headerText.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, palette.headerMuted.copy(alpha = 0.60f))
                ) {
                    Text(
                        text = savedStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.headerText,
                        modifier = Modifier
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("header_saved_status"),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (onOpenCustomers != null) {
                    IconButton(
                        onClick = onOpenCustomers,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("header_customers_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Customers",
                            tint = palette.headerText
                        )
                    }
                }
                if (onOpenSettings != null) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("header_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = palette.headerText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThdrCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalThdrPalette.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(colors = palette.cardGradient))
            .border(1.dp, palette.outline, RoundedCornerShape(18.dp))
            .padding(contentPadding)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun ThdrSubCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalThdrPalette.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surfaceVariant.copy(alpha = palette.subCardAlpha))
            .border(1.dp, palette.outlineSubtle, RoundedCornerShape(14.dp))
            .padding(contentPadding)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun ThdrPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    enabled: Boolean = true,
    orangeAccent: Boolean = false
) {
    val palette = LocalThdrPalette.current
    val brush = Brush.horizontalGradient(
        listOf(
            palette.primary.copy(alpha = 0.94f),
            palette.primaryDeep.copy(alpha = 0.96f)
        )
    )
    val borderStroke = if (orangeAccent) {
        BorderStroke(1.5.dp, palette.accent.copy(alpha = 0.85f))
    } else {
        BorderStroke(1.dp, palette.outline.copy(alpha = 0.65f))
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        border = borderStroke,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = palette.surfaceVariant.copy(alpha = 0.72f)
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(brush)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ThdrSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    enabled: Boolean = true
) {
    val palette = LocalThdrPalette.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, palette.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.surfaceVariant.copy(alpha = 0.78f),
            contentColor = palette.textPrimary
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ThdrDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val palette = LocalThdrPalette.current
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, palette.danger.copy(alpha = 0.7f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = palette.dangerBg.copy(alpha = 0.86f),
            contentColor = if (palette.isDark) palette.textPrimary else palette.danger
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun thdrTextFieldColors() = LocalThdrPalette.current.let { palette ->
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = palette.textPrimary,
        unfocusedTextColor = palette.textPrimary,
        disabledTextColor = palette.textMuted,
        focusedContainerColor = palette.surfaceVariant.copy(alpha = 0.84f),
        unfocusedContainerColor = palette.surfaceVariant.copy(alpha = 0.78f),
        disabledContainerColor = palette.surfaceVariant.copy(alpha = 0.55f),
        focusedBorderColor = palette.brandHighlight,
        unfocusedBorderColor = palette.outline,
        disabledBorderColor = palette.outlineSubtle,
        focusedLabelColor = palette.brandHighlight,
        unfocusedLabelColor = palette.textMuted,
        cursorColor = palette.brandHighlight
    )
}

@Composable
fun ThdrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    testTag: String = ""
) {
    val palette = LocalThdrPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = palette.textMuted.copy(alpha = 0.7f)) }
        } else null,
        singleLine = singleLine,
        minLines = minLines,
        colors = thdrTextFieldColors(),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    )
}

@Composable
fun ThdrDoubleField(
    value: Double,
    onValueChange: (Double) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    testTag: String = "",
    isInteger: Boolean = false
) {
    var text by remember { mutableStateOf(formatEditableNumber(value, isInteger)) }
    var focused by remember { mutableStateOf(false) }

    LaunchedEffect(value, focused) {
        if (!focused) {
            val formatted = formatEditableNumber(value, isInteger)
            if (text.toDoubleOrNull() != value || text.isBlank()) {
                text = formatted
            }
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            val filtered = if (isInteger) {
                raw.filter { it.isDigit() }
            } else {
                val sb = StringBuilder()
                var dotSeen = false
                for (ch in raw) {
                    if (ch.isDigit()) {
                        sb.append(ch)
                    } else if (ch == '.' && !dotSeen) {
                        dotSeen = true
                        sb.append(ch)
                    }
                }
                sb.toString()
            }
            text = filtered
            val parsed = filtered.toDoubleOrNull()
            if (parsed != null) {
                onValueChange(parsed)
            } else if (filtered.isEmpty()) {
                onValueChange(0.0)
            }
        },
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isInteger) KeyboardType.Number else KeyboardType.Decimal
        ),
        colors = thdrTextFieldColors(),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                focused = focusState.isFocused
                if (!focusState.isFocused) {
                    text = formatEditableNumber(value, isInteger)
                }
            }
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    )
}

private fun formatEditableNumber(v: Double, isInteger: Boolean): String {
    if (isInteger) return v.toInt().toString()
    if (v == 0.0) return "0"
    return if (v % 1.0 == 0.0) v.toInt().toString() else String.format(java.util.Locale.US, "%.2f", v).trimEnd('0').trimEnd('.')
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ThdrDropdownField(
    label: String,
    selectedLabel: String,
    options: List<Pair<String, T>>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val palette = LocalThdrPalette.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = thdrTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = palette.surfaceVariant
        ) {
            options.forEach { (itemLabel, itemValue) ->
                DropdownMenuItem(
                    text = { Text(itemLabel, color = palette.textPrimary) },
                    onClick = {
                        expanded = false
                        onOptionSelected(itemValue)
                    }
                )
            }
        }
    }
}

@Composable
fun SelectablePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val palette = LocalThdrPalette.current
    Surface(
        shape = RoundedCornerShape(99.dp),
        color = if (selected) palette.primary.copy(alpha = 0.90f) else palette.surfaceVariant.copy(alpha = 0.80f),
        border = BorderStroke(1.dp, if (selected) palette.brandHighlight else palette.outline),
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clip(RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White else palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalThdrPalette.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        titleContentColor = palette.textPrimary,
        textContentColor = palette.textMuted,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.textMuted)
            }
        }
    )
}

@Composable
fun PromptInputDialog(
    title: String,
    label: String,
    initialValue: String = "",
    confirmText: String = "Save",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalThdrPalette.current
    var input by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        titleContentColor = palette.textPrimary,
        textContentColor = palette.textPrimary,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            ThdrTextField(
                value = input,
                onValueChange = { input = it },
                label = label,
                testTag = "dialog_text_input"
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        onConfirm(input.trim())
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.textMuted)
            }
        }
    )
}
