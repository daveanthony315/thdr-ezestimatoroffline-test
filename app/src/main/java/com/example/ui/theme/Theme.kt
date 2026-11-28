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
import com.example.model.ColorHexCodec
import com.example.model.CustomColorScheme

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
    val cardGradient: List<Color>,
    val navBarAlpha: Float = 0.85f,
    val subCardAlpha: Float = 0.82f
)

private fun customSchemePalette(scheme: CustomColorScheme): ThdrPalette {
    val primaryHex = ColorHexCodec.normalizeHex(scheme.primaryHex, "#2F5D8A")
    val secondaryHex = ColorHexCodec.normalizeHex(scheme.secondaryHex, "#5E8FB9")
    val accentHex = ColorHexCodec.normalizeHex(scheme.accentHex, "#F5832B")
    val bgHex = ColorHexCodec.normalizeHex(scheme.backgroundHex, if (scheme.isDark) "#1B334D" else "#E6E7EA")
    val surfaceHex = ColorHexCodec.normalizeHex(scheme.surfaceHex, if (scheme.isDark) "#233F5E" else "#FFFFFF")
    val textPrimaryHex = ColorHexCodec.normalizeHex(scheme.textPrimaryHex, if (scheme.isDark) "#FFFFFF" else "#1D2B3A")
    val textMutedHex = ColorHexCodec.normalizeHex(scheme.textMutedHex, if (scheme.isDark) "#CDE3F5" else "#4A4A4A")
    val outlineHex = ColorHexCodec.normalizeHex(scheme.outlineHex, secondaryHex)

    val primary = Color(ColorHexCodec.parseArgbLong(primaryHex, 0xFF2F5D8AL))
    val primaryDeepHex = ColorHexCodec.darkenHex(primaryHex, 0.28f)
    val primaryDeep = Color(ColorHexCodec.parseArgbLong(primaryDeepHex, 0xFF22466AL))
    val secondary = Color(ColorHexCodec.parseArgbLong(secondaryHex, 0xFF5E8FB9L))
    val accent = Color(ColorHexCodec.parseArgbLong(accentHex, 0xFFF5832BL))
    val accentSecondary = Color(
        ColorHexCodec.parseArgbLong(
            ColorHexCodec.lightenHex(accentHex, 0.25f),
            0xFFE8A460L
        )
    )
    val bg = Color(ColorHexCodec.parseArgbLong(bgHex, 0xFFE6E7EAL))
    val surface = Color(ColorHexCodec.parseArgbLong(surfaceHex, 0xFFFFFFFFL))
    val surfaceVariantHex = ColorHexCodec.blendHex(surfaceHex, bgHex, 0.38f)
    val surfaceVariant = Color(ColorHexCodec.parseArgbLong(surfaceVariantHex, 0xFFF1F5FAL))
    val surfaceHighHex = if (scheme.isDark) {
        ColorHexCodec.blendHex(surfaceHex, secondaryHex, 0.26f)
    } else {
        ColorHexCodec.blendHex(surfaceHex, secondaryHex, 0.24f)
    }
    val surfaceHigh = Color(ColorHexCodec.parseArgbLong(surfaceHighHex, 0xFFCDE3F5L))
    val textPrimary = Color(ColorHexCodec.parseArgbLong(textPrimaryHex, 0xFF1D2B3AL))
    val textMuted = Color(ColorHexCodec.parseArgbLong(textMutedHex, 0xFF4A4A4AL))
    val outlineColor = Color(ColorHexCodec.parseArgbLong(outlineHex, 0xFF5E8FB9L))

    val brandHighlight = if (scheme.isDark) {
        if (ColorHexCodec.relativeLuminance(secondaryHex) < 0.35f) {
            Color(ColorHexCodec.parseArgbLong(ColorHexCodec.lightenHex(secondaryHex, 0.45f), 0xFFCDE3F5L))
        } else {
            secondary
        }
    } else {
        if (ColorHexCodec.relativeLuminance(primaryHex) > 0.65f) {
            primaryDeep
        } else {
            primary
        }
    }

    val headerIsLight = ColorHexCodec.relativeLuminance(primaryHex) > 0.68f
    val headerText = if (headerIsLight) SlateInk else CleanWhite
    val headerMuted = if (headerIsLight) Charcoal else Color(
        ColorHexCodec.parseArgbLong(ColorHexCodec.lightenHex(secondaryHex, 0.52f), 0xFFCDE3F5L)
    )

    val cardAlpha = (scheme.cardOpacityPct.coerceIn(55, 100) / 100f)
    val navAlpha = (scheme.navOpacityPct.coerceIn(55, 100) / 100f)
    val subAlpha = (cardAlpha - 0.04f).coerceIn(0.50f, 0.96f)

    return ThdrPalette(
        isDark = scheme.isDark,
        bg = bg,
        surface = surface,
        surfaceVariant = surfaceVariant,
        surfaceHigh = surfaceHigh,
        primary = primary,
        primaryDeep = primaryDeep,
        accent = accent,
        accentSecondary = accentSecondary,
        brandHighlight = brandHighlight,
        textPrimary = textPrimary,
        textMuted = textMuted,
        outline = outlineColor.copy(alpha = 0.56f),
        outlineSubtle = outlineColor.copy(alpha = 0.30f),
        danger = if (scheme.isDark) Color(0xFFE0635F) else Color(0xFFC23B36),
        dangerBg = if (scheme.isDark) Color(0xFF4D2124) else Color(0xFFFCEAEA),
        warning = if (scheme.isDark) WarmYellow else Color(0xFF9E5A14),
        warningContainer = if (scheme.isDark) AccentOrangeContainer.copy(alpha = 0.72f) else WarmYellow.copy(alpha = 0.35f),
        success = if (scheme.isDark) SageGreen else Color(0xFF5A7744),
        woodAccent = WoodBrown,
        headerText = headerText,
        headerMuted = headerMuted,
        onAccentContainer = CleanWhite,
        backdropGradient = if (scheme.isDark) {
            listOf(
                bg.copy(alpha = 0.58f),
                primaryDeep.copy(alpha = 0.64f),
                secondary.copy(alpha = 0.20f)
            )
        } else {
            listOf(
                primary.copy(alpha = 0.16f),
                secondary.copy(alpha = 0.28f),
                bg.copy(alpha = 0.64f)
            )
        },
        headerGradient = listOf(
            primaryDeep.copy(alpha = (navAlpha + 0.05f).coerceAtMost(0.96f)),
            primary.copy(alpha = (navAlpha + 0.05f).coerceAtMost(0.96f)),
            secondary.copy(alpha = navAlpha)
        ),
        cardGradient = listOf(
            surface.copy(alpha = cardAlpha),
            surfaceVariant.copy(alpha = (cardAlpha - 0.02f).coerceAtLeast(0.50f)),
            secondary.copy(alpha = if (scheme.isDark) 0.18f else 0.28f)
        ),
        navBarAlpha = navAlpha,
        subCardAlpha = subAlpha
    )
}

fun paletteFor(settings: AppSettings): ThdrPalette {
    settings.activeCustomScheme()?.let { custom ->
        return customSchemePalette(custom)
    }
    val accent = Color(settings.accentColor.primaryArgb)
    val accentSec = Color(settings.accentColor.secondaryArgb)
    return when (settings.themePreset) {
        AppThemePreset.HOUSE_DR_STUDIO -> ThdrPalette(
            isDark = false,
            bg = SoftGray,
            surface = CleanWhite,
            surfaceVariant = Color(0xFFF1F5FA),
            surfaceHigh = SkyBlue,
            primary = SlateBlue,
            primaryDeep = SlateBlueDeep,
            accent = accent,
            accentSecondary = accentSec,
            brandHighlight = SlateBlue,
            textPrimary = SlateInk,
            textMuted = Charcoal,
            outline = SteelBlue.copy(alpha = 0.52f),
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
                SlateBlue.copy(alpha = 0.16f),
                SkyBlue.copy(alpha = 0.38f),
                SoftGray.copy(alpha = 0.64f)
            ),
            headerGradient = listOf(
                SlateBlueDeep.copy(alpha = 0.90f),
                SlateBlue.copy(alpha = 0.90f),
                SteelBlue.copy(alpha = 0.88f)
            ),
            cardGradient = listOf(
                CleanWhite.copy(alpha = 0.86f),
                Color(0xFFF3F7FC).copy(alpha = 0.84f),
                SkyBlue.copy(alpha = 0.34f)
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
            outline = SteelBlue.copy(alpha = 0.60f),
            outlineSubtle = SteelBlue.copy(alpha = 0.34f),
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
                BlueprintBg.copy(alpha = 0.56f),
                SlateBlueDeep.copy(alpha = 0.64f),
                SteelBlue.copy(alpha = 0.18f)
            ),
            headerGradient = listOf(
                SlateBlueDeep.copy(alpha = 0.90f),
                BlueprintSurface.copy(alpha = 0.90f),
                SlateBlue.copy(alpha = 0.86f)
            ),
            cardGradient = listOf(
                BlueprintSurface.copy(alpha = 0.85f),
                BlueprintBg.copy(alpha = 0.85f),
                SteelBlue.copy(alpha = 0.18f)
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
                Color(0x9E1D2229),
                Color(0xB3232A33),
                SlateBlue.copy(alpha = 0.18f)
            ),
            headerGradient = listOf(
                Color(0xE6252C36),
                SlateBlueDeep.copy(alpha = 0.90f),
                Color(0xE02C3643)
            ),
            cardGradient = listOf(
                Color(0xD92A313B),
                Color(0xDB222830),
                SteelBlue.copy(alpha = 0.14f)
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
            outline = SageGreen.copy(alpha = 0.58f),
            outlineSubtle = SageGreen.copy(alpha = 0.34f),
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
                Color(0x9E1A2420),
                Color(0xB3212E28),
                SageGreen.copy(alpha = 0.16f)
            ),
            headerGradient = listOf(
                Color(0xE6213028),
                Color(0xE6344936),
                Color(0xE027392E)
            ),
            cardGradient = listOf(
                Color(0xD925352E),
                Color(0xDB1E2B25),
                SageGreen.copy(alpha = 0.16f)
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
    val palette = remember(
        settings.themePreset,
        settings.accentColor,
        settings.activeCustomSchemeId,
        settings.customColorSchemes
    ) { paletteFor(settings) }
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
