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
import com.example.model.AppAccentColor
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
    val success: Color,
    val onAccentContainer: Color,
    val backdropGradient: List<Color>,
    val headerGradient: List<Color>,
    val cardGradient: List<Color>
)

fun paletteFor(settings: AppSettings): ThdrPalette {
    val accent = Color(settings.accentColor.primaryArgb)
    val accentSec = Color(settings.accentColor.secondaryArgb)
    return when (settings.themePreset) {
        AppThemePreset.BLUEPRINT_NAVY -> ThdrPalette(
            isDark = true,
            bg = BlueprintBg,
            surface = BlueprintSurface,
            surfaceVariant = BlueprintSurfaceVariant,
            surfaceHigh = BlueprintSurfaceHigh,
            primary = BrandCobalt,
            primaryDeep = BrandCobaltDeep,
            accent = accent,
            accentSecondary = accentSec,
            brandHighlight = BrandCyan,
            textPrimary = TextPrimary,
            textMuted = TextMuted,
            outline = OutlineBlue,
            outlineSubtle = OutlineSubtle,
            danger = DangerRed,
            dangerBg = DangerBg,
            warning = WarningAmber,
            success = SuccessTeal,
            onAccentContainer = Color.White,
            backdropGradient = listOf(
                Color(0xB800286B),
                Color(0xCC021436),
                accent.copy(alpha = 0.25f)
            ),
            headerGradient = listOf(
                BlueprintSurface.copy(alpha = 0.96f),
                BlueprintBg.copy(alpha = 0.96f),
                accent.copy(alpha = 0.35f)
            ),
            cardGradient = listOf(
                BlueprintSurface.copy(alpha = 0.97f),
                BlueprintBg.copy(alpha = 0.96f),
                accent.copy(alpha = 0.18f)
            )
        )

        AppThemePreset.MIDNIGHT_SLATE -> ThdrPalette(
            isDark = true,
            bg = Color(0xFF0F131A),
            surface = Color(0xFF1A222E),
            surfaceVariant = Color(0xFF141A24),
            surfaceHigh = Color(0xFF242F40),
            primary = Color(0xFF1D6FD8),
            primaryDeep = Color(0xFF1553A6),
            accent = accent,
            accentSecondary = accentSec,
            brandHighlight = accentSec,
            textPrimary = Color(0xFFF3F6FA),
            textMuted = Color(0xFFAAB8CC),
            outline = Color(0xFF3E4E63),
            outlineSubtle = Color(0x663E4E63),
            danger = Color(0xFFE05D5A),
            dangerBg = Color(0xFF471E21),
            warning = Color(0xFFFFD18B),
            success = Color(0xFF31D5C8),
            onAccentContainer = Color.White,
            backdropGradient = listOf(
                Color(0xE60F131A),
                Color(0xF0151C26),
                accent.copy(alpha = 0.16f)
            ),
            headerGradient = listOf(
                Color(0xFF1B2330),
                Color(0xFF121720),
                accent.copy(alpha = 0.28f)
            ),
            cardGradient = listOf(
                Color(0xF51D2634),
                Color(0xF5141B25),
                accent.copy(alpha = 0.14f)
            )
        )

        AppThemePreset.STEEL_DAYLIGHT -> ThdrPalette(
            isDark = false,
            bg = Color(0xFFEBF1F8),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF4F7FC),
            surfaceHigh = Color(0xFFE2EBF5),
            primary = Color(0xFF0D5BBF),
            primaryDeep = Color(0xFF094391),
            accent = accent,
            accentSecondary = accentSec,
            brandHighlight = Color(0xFF0958C7),
            textPrimary = Color(0xFF0E1B2E),
            textMuted = Color(0xFF475B75),
            outline = Color(0xFF9BB2CC),
            outlineSubtle = Color(0xFFD0DCEB),
            danger = Color(0xFFC93B37),
            dangerBg = Color(0xFFFCE8E8),
            warning = Color(0xFF9C5700),
            success = Color(0xFF0B7B72),
            onAccentContainer = Color.White,
            backdropGradient = listOf(
                Color(0xE6E9F0F8),
                Color(0xF0F4F7FC),
                accent.copy(alpha = 0.10f)
            ),
            headerGradient = listOf(
                Color(0xFFFDFEFF),
                Color(0xFFEEF4FB),
                accent.copy(alpha = 0.14f)
            ),
            cardGradient = listOf(
                Color(0xFCFFFFFF),
                Color(0xFAF4F8FD),
                accent.copy(alpha = 0.08f)
            )
        )

        AppThemePreset.WORKSHOP_PINE -> ThdrPalette(
            isDark = true,
            bg = Color(0xFF0B1C19),
            surface = Color(0xFF122C28),
            surfaceVariant = Color(0xFF0E221E),
            surfaceHigh = Color(0xFF1A3B36),
            primary = Color(0xFF137C6D),
            primaryDeep = Color(0xFF0D5D51),
            accent = accent,
            accentSecondary = accentSec,
            brandHighlight = Color(0xFF56E0C9),
            textPrimary = Color(0xFFF1FBF8),
            textMuted = Color(0xFFA7C9C2),
            outline = Color(0xFF2D635A),
            outlineSubtle = Color(0x662D635A),
            danger = Color(0xFFE05D5A),
            dangerBg = Color(0xFF491E22),
            warning = Color(0xFFFFD18B),
            success = Color(0xFF56E0C9),
            onAccentContainer = Color.White,
            backdropGradient = listOf(
                Color(0xD80B1C19),
                Color(0xE6102925),
                accent.copy(alpha = 0.18f)
            ),
            headerGradient = listOf(
                Color(0xFF13302B),
                Color(0xFF0B1C19),
                accent.copy(alpha = 0.30f)
            ),
            cardGradient = listOf(
                Color(0xF514322D),
                Color(0xF50E231F),
                accent.copy(alpha = 0.14f)
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
