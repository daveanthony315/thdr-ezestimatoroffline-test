package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.model.AppSettings
import com.example.model.AppThemePreset

@Immutable
data class ThdrPalette(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceHigh: Color,
    val primary: Color,
    val primaryDeep: Color,
    val accent: Color,
    val accentSecondary: Color,
    val brandHighlight: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val outline: Color,
    val outlineSubtle: Color,
    val danger: Color,
    val dangerBg: Color,
    val warning: Color,
    val warningContainer: Color,
    val success: Color,
    val woodAccent: Color,
    val headerText: Color,
    val headerMuted: Color,
    val onAccentContainer: Color,
    val backdropGradient: List<Color>,
    val headerGradient: List<Color>,
    val cardGradient: List<Color>
)

fun paletteFor(settings: AppSettings): ThdrPalette {
    val accent = Color(settings.accentColor.primaryArgb)
    val accentSec = Color(settings.accentColor.secondaryArgb)
    return when (settings.themePreset) {
        AppThemePreset.HOUSE_DR_STUDIO -> ThdrPalette(
            isDark = false,
            bg = SoftGray,
            surface = CleanWhite,
            surfaceVariant = Color(0xFFF3F7FB),
            surfaceHigh = SkyBlue,
            primary = SlateBlue,
            primaryDeep = SlateBlueDeep,
            accent = accent,
            accentSecondary = accentSec,
            brandHighlight = SlateBlue,
            textPrimary = SlateInk,
            textMuted = Charcoal,
            outline = SteelBlue.copy(alpha = 0.55f),
            outlineSubtle = SteelBlue.copy(alpha = 0.28f),
            danger = Color(0xFFC23B36),
            dangerBg = Color(0xFFFCEAEA),
            warning = Color(0xFF9E5A14),
            warningContainer = WarmYellow.copy(alpha = 0.35f),
            success = Color(0xFF5A7744),
            woodAccent = WoodBrown,
            headerText = CleanWhite,
            headerMuted = SkyBlue,
            onAccentContainer = CleanWhite,
            backdropGradient = listOf(
                SoftGray.copy(alpha = 0.88f),
                SkyBlue.copy(alpha = 0.52f),
                SoftGray.copy(alpha = 0.92f)
            ),
            headerGradient = listOf(
                SlateBlue,
                SlateBlueDeep,
                SteelBlue
            ),
            cardGradient = listOf(
                CleanWhite,
                Color(0xFFFAFBFD),
                SkyBlue.copy(alpha = 0.22f)
            )
        )

        AppThemePreset.BLUEPRINT_NAVY -> ThdrPalette(
            isDark = true,
            bg = BlueprintBg,
            surface = BlueprintSurface,
            surfaceVariant = BlueprintSurfaceVariant,
            surfaceHigh = BlueprintSurfaceHigh,
            primary = SlateBlue,
            primaryDeep = SlateBlueDeep,
            accent = accent,
            accentSecondary = accentSec,
            brandHighlight = SkyBlue,
            textPrimary = CleanWhite,
            textMuted = SkyBlue,
            outline = SteelBlue.copy(alpha = 0.65f),
            outlineSubtle = SteelBlue.copy(alpha = 0.35f),
            danger = Color(0xFFE0635F),
            dangerBg = Color(0xFF4D2124),
            warning = WarmYellow,
            warningContainer = AccentOrangeContainer.copy(alpha = 0.72f),
            success = SageGreen,
            woodAccent = WoodBrown,
            headerText = CleanWhite,
            headerMuted = SkyBlue,
            onAccentContainer = CleanWhite,
            backdropGradient = listOf(
                BlueprintBg.copy(alpha = 0.76f),
                SlateBlueDeep.copy(alpha = 0.84f),
                accent.copy(alpha = 0.16f)
            ),
            headerGradient = listOf(
                SlateBlueDeep,
                BlueprintSurface,
                accent.copy(alpha = 0.32f)
            ),
            cardGradient = listOf(
                BlueprintSurface.copy(alpha = 0.96f),
                BlueprintBg.copy(alpha = 0.96f),
                SteelBlue.copy(alpha = 0.16f)
            )
        )

        AppThemePreset.MIDNIGHT_SLATE -> ThdrPalette(
            isDark = true,
            bg = Color(0xFF1D2229),
            surface = Color(0xFF282F38),
            surfaceVariant = Color(0xFF222830),
            surfaceHigh = Color(0xFF343D49),
            primary = SlateBlue,
            primaryDeep = SlateBlueDeep,
            accent = accent,
            accentSecondary = accentSec,
            brandHighlight = SkyBlue,
            textPrimary = CleanWhite,
            textMuted = Color(0xFFC5CCD6),
            outline = SteelBlue.copy(alpha = 0.55f),
            outlineSubtle = Color(0x665E8FB9),
            danger = Color(0xFFE0635F),
            dangerBg = Color(0xFF471E21),
            warning = WarmYellow,
            warningContainer = Color(0xFF42311A),
            success = SageGreen,
            woodAccent = WoodBrown,
            headerText = CleanWhite,
            headerMuted = SkyBlue,
            onAccentContainer = CleanWhite,
            backdropGradient = listOf(
                Color(0xD91D2229),
                Color(0xEB232A33),
                accent.copy(alpha = 0.14f)
            ),
            headerGradient = listOf(
                Color(0xFF2C3643),
                SlateBlueDeep,
                accent.copy(alpha = 0.25f)
            ),
            cardGradient = listOf(
                Color(0xF52A313B),
                Color(0xF5222830),
                SteelBlue.copy(alpha = 0.12f)
            )
        )

        AppThemePreset.WORKSHOP_PINE -> ThdrPalette(
            isDark = true,
            bg = Color(0xFF1A2420),
            surface = Color(0xFF24332C),
            surfaceVariant = Color(0xFF1E2B25),
            surfaceHigh = Color(0xFF2F4238),
            primary = Color(0xFF5D7A4A),
            primaryDeep = Color(0xFF486138),
            accent = accent,
            accentSecondary = accentSec,
            brandHighlight = SageGreen,
            textPrimary = CleanWhite,
            textMuted = Color(0xFFD2DEC8),
            outline = SageGreen.copy(alpha = 0.6f),
            outlineSubtle = SageGreen.copy(alpha = 0.35f),
            danger = Color(0xFFE0635F),
            dangerBg = Color(0xFF491E22),
            warning = WarmYellow,
            warningContainer = Color(0xFF47341B),
            success = SageGreen,
            woodAccent = WoodBrown,
            headerText = CleanWhite,
            headerMuted = Color(0xFFD7E6CC),
            onAccentContainer = CleanWhite,
            backdropGradient = listOf(
                Color(0xD91A2420),
                Color(0xE8212E28),
                WoodBrown.copy(alpha = 0.18f)
            ),
            headerGradient = listOf(
                Color(0xFF27392E),
                SlateBlueDeep,
                WoodBrown.copy(alpha = 0.28f)
            ),
            cardGradient = listOf(
                Color(0xF525352E),
                Color(0xF51E2B25),
                SageGreen.copy(alpha = 0.14f)
            )
        )
    }
}

val LocalThdrPalette = staticCompositionLocalOf { paletteFor(AppSettings()) }

val ThdrShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

private fun scaledStyle(style: TextStyle, factor: Float): TextStyle =
    style.copy(
        fontSize = style.fontSize * factor,
        lineHeight = style.lineHeight * factor
    )

fun appTypographyFor(largeFieldText: Boolean): Typography {
    if (!largeFieldText) return AppTypography
    val f = 1.12f
    return AppTypography.copy(
        displaySmall = scaledStyle(AppTypography.displaySmall, f),
        headlineMedium = scaledStyle(AppTypography.headlineMedium, f),
        headlineSmall = scaledStyle(AppTypography.headlineSmall, f),
        titleLarge = scaledStyle(AppTypography.titleLarge, f),
        titleMedium = scaledStyle(AppTypography.titleMedium, f),
        titleSmall = scaledStyle(AppTypography.titleSmall, f),
        bodyLarge = scaledStyle(AppTypography.bodyLarge, f),
        bodyMedium = scaledStyle(AppTypography.bodyMedium, f),
        bodySmall = scaledStyle(AppTypography.bodySmall, f),
        labelLarge = scaledStyle(AppTypography.labelLarge, f),
        labelMedium = scaledStyle(AppTypography.labelMedium, f),
        labelSmall = scaledStyle(AppTypography.labelSmall, f)
    )
}

@Composable
fun ThdrTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit
) {
    val palette = remember(settings.themePreset, settings.accentColor) { paletteFor(settings) }
    val typography = remember(settings.largeFieldText) { appTypographyFor(settings.largeFieldText) }
    val colorScheme = remember(palette) {
        if (palette.isDark) {
            darkColorScheme(
                primary = palette.primary,
                onPrimary = palette.onAccentContainer,
                primaryContainer = palette.surfaceHigh,
                onPrimaryContainer = palette.textPrimary,
                secondary = palette.accent,
                onSecondary = palette.onAccentContainer,
                secondaryContainer = palette.surfaceHigh,
                onSecondaryContainer = palette.textPrimary,
                tertiary = palette.brandHighlight,
                onTertiary = palette.bg,
                background = palette.bg,
                onBackground = palette.textPrimary,
                surface = palette.surface,
                onSurface = palette.textPrimary,
                surfaceVariant = palette.surfaceVariant,
                onSurfaceVariant = palette.textMuted,
                surfaceContainerHigh = palette.surfaceHigh,
                outline = palette.outline,
                outlineVariant = palette.outlineSubtle,
                error = palette.danger,
                onError = Color.White,
                errorContainer = palette.dangerBg,
                onErrorContainer = palette.textPrimary
            )
        } else {
            lightColorScheme(
                primary = palette.primary,
                onPrimary = Color.White,
                primaryContainer = palette.surfaceHigh,
                onPrimaryContainer = palette.textPrimary,
                secondary = palette.accent,
                onSecondary = Color.White,
                secondaryContainer = palette.surfaceHigh,
                onSecondaryContainer = palette.textPrimary,
                tertiary = palette.brandHighlight,
                onTertiary = Color.White,
                background = palette.bg,
                onBackground = palette.textPrimary,
                surface = palette.surface,
                onSurface = palette.textPrimary,
                surfaceVariant = palette.surfaceVariant,
                onSurfaceVariant = palette.textMuted,
                surfaceContainerHigh = palette.surfaceHigh,
                outline = palette.outline,
                outlineVariant = palette.outlineSubtle,
                error = palette.danger,
                onError = Color.White,
                errorContainer = palette.dangerBg,
                onErrorContainer = palette.danger
            )
        }
    }

    CompositionLocalProvider(LocalThdrPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = ThdrShapes,
            content = content
        )
    }
}
