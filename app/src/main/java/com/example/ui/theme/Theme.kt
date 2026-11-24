package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ThdrColorScheme = darkColorScheme(
    primary = BrandCobalt,
    onPrimary = TextPrimary,
    primaryContainer = BrandCyanContainer,
    onPrimaryContainer = TextPrimary,
    secondary = AccentOrange,
    onSecondary = TextPrimary,
    secondaryContainer = AccentOrangeContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = BrandCyan,
    onTertiary = BlueprintBg,
    background = BlueprintBg,
    onBackground = TextPrimary,
    surface = BlueprintSurface,
    onSurface = TextPrimary,
    surfaceVariant = BlueprintSurfaceVariant,
    onSurfaceVariant = TextMuted,
    surfaceContainerHigh = BlueprintSurfaceHigh,
    outline = OutlineBlue,
    outlineVariant = OutlineSubtle,
    error = DangerRed,
    onError = TextPrimary,
    errorContainer = DangerBg,
    onErrorContainer = TextPrimary
)

val ThdrShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

@Composable
fun ThdrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ThdrColorScheme,
        typography = AppTypography,
        shapes = ThdrShapes,
        content = content
    )
}
