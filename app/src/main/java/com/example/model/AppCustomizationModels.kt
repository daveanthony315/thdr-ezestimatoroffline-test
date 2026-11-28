package com.example.model

enum class DocumentType(val key: String, val title: String, val pluralTitle: String) {
    ESTIMATE("estimate", "Estimate", "Estimates"),
    PROPOSAL("proposal", "Proposal", "Proposals");

    companion object {
        fun fromKey(key: String?): DocumentType =
            if (key == PROPOSAL.key) PROPOSAL else ESTIMATE
    }
}

enum class ProposalStatus(val key: String, val label: String) {
    DRAFT("draft", "Draft"),
    SENT("sent", "Sent"),
    APPROVED("approved", "Approved"),
    DECLINED("declined", "Declined");

    companion object {
        fun fromKey(key: String?): ProposalStatus =
            entries.firstOrNull { it.key == key } ?: DRAFT
    }
}

data class Customer(
    val id: Long = 0,
    val name: String,
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val createdAt: String = ""
)

enum class AppThemePreset(
    val key: String,
    val label: String,
    val description: String,
    val isDark: Boolean
) {
    HOUSE_DR_STUDIO(
        key = "house_dr_studio",
        label = "House DR Studio",
        description = "Slate Blue, Sky Blue, Soft Gray, Clean White & Accent Orange",
        isDark = false
    ),
    BLUEPRINT_NAVY(
        key = "blueprint_navy",
        label = "Blueprint Slate Night",
        description = "Deep Slate Blue architectural dark mode",
        isDark = true
    ),
    MIDNIGHT_SLATE(
        key = "midnight_slate",
        label = "Midnight Charcoal",
        description = "Neutral dark charcoal for low glare",
        isDark = true
    ),
    WORKSHOP_PINE(
        key = "workshop_pine",
        label = "Sage & Timber",
        description = "Deep slate forest with Sage Green and Wood Brown accents",
        isDark = true
    );

    companion object {
        fun fromKey(key: String?): AppThemePreset =
            entries.firstOrNull { it.key == key } ?: HOUSE_DR_STUDIO
    }
}

enum class AppAccentColor(
    val key: String,
    val label: String,
    val primaryArgb: Long,
    val secondaryArgb: Long
) {
    COPPER_ORANGE("orange", "Accent Orange", 0xFFF5832B, 0xFFE8A460),
    ELECTRIC_CYAN("cyan", "Steel Blue", 0xFF2F5D8A, 0xFF5E8FB9),
    EMERALD_TEAL("teal", "Sage Green", 0xFF6B8A54, 0xFF8FAA77),
    GOLD_AMBER("amber", "Wood & Amber", 0xFFA67C52, 0xFFFFD88A);

    companion object {
        fun fromKey(key: String?): AppAccentColor =
            entries.firstOrNull { it.key == key } ?: COPPER_ORANGE
    }
}

enum class BrandingSlot(val key: String, val label: String) {
    HEADER_EMBLEM("header_emblem", "Header Emblem"),
    BRAND_LOCKUP("brand_lockup", "Brand Lockup Logo"),
    HERO_BACKDROP("hero_backdrop", "Hero / Backdrop Art"),
    HOME_MASCOT("home_mascot", "Mascot / Badge"),
    PDF_LOGO("pdf_logo", "PDF Header Logo")
}

enum class HeaderEmblemChoice(val key: String, val label: String) {
    DEFAULT_EMBLEM("default_emblem", "House DR Emblem"),
    MASCOT_BADGE("mascot_badge", "Mascot Badge"),
    FULL_LOCKUP("full_lockup", "Full Lockup"),
    CUSTOM("custom", "Custom Upload"),
    HIDDEN("hidden", "Hidden");

    companion object {
        fun fromKey(key: String?): HeaderEmblemChoice =
            entries.firstOrNull { it.key == key } ?: DEFAULT_EMBLEM
    }
}

enum class BrandLogoChoice(val key: String, val label: String) {
    DEFAULT_LOCKUP("default_lockup", "Full Brand Lockup"),
    DEFAULT_EMBLEM("default_emblem", "House DR Emblem"),
    MASCOT_BADGE("mascot_badge", "Mascot Badge"),
    CUSTOM("custom", "Custom Upload"),
    HIDDEN("hidden", "Hidden");

    companion object {
        fun fromKey(key: String?): BrandLogoChoice =
            entries.firstOrNull { it.key == key } ?: DEFAULT_LOCKUP
    }
}

enum class HeroBackdropChoice(val key: String, val label: String) {
    DEFAULT_BLUEPRINT("default_blueprint", "Blueprint Scene"),
    GRADIENT_ONLY("gradient_only", "Clean Gradient"),
    CUSTOM("custom", "Custom Photo");

    companion object {
        fun fromKey(key: String?): HeroBackdropChoice =
            entries.firstOrNull { it.key == key } ?: DEFAULT_BLUEPRINT
    }
}

enum class MascotSlotChoice(val key: String, val label: String) {
    STANDING_MASCOT("standing_mascot", "Standing Mascot"),
    CIRCULAR_BADGE("circular_badge", "Circular Badge"),
    COMPANY_EMBLEM("company_emblem", "Company Emblem"),
    CUSTOM("custom", "Custom Upload"),
    HIDDEN("hidden", "Hidden");

    companion object {
        fun fromKey(key: String?): MascotSlotChoice =
            entries.firstOrNull { it.key == key } ?: STANDING_MASCOT
    }
}

enum class HeroMascotPosition(val key: String, val label: String) {
    RIGHT("right", "Right"),
    LEFT("left", "Left"),
    HIDDEN("hidden", "Hidden");

    companion object {
        fun fromKey(key: String?): HeroMascotPosition =
            entries.firstOrNull { it.key == key } ?: RIGHT
    }
}

enum class PdfColorTheme(
    val key: String,
    val label: String,
    val primaryHex: String,
    val deepHex: String,
    val steelHex: String,
    val skyHex: String,
    val softGrayHex: String,
    val accentHex: String
) {
    HOUSE_DR_SLATE(
        key = "house_dr_slate",
        label = "House DR Slate",
        primaryHex = "#2F5D8A",
        deepHex = "#22466A",
        steelHex = "#5E8FB9",
        skyHex = "#CDE3F5",
        softGrayHex = "#E6E7EA",
        accentHex = "#F5832B"
    ),
    MATCH_ACTIVE_THEME(
        key = "match_active_theme",
        label = "Match Active App Theme",
        primaryHex = "#2F5D8A",
        deepHex = "#22466A",
        steelHex = "#5E8FB9",
        skyHex = "#CDE3F5",
        softGrayHex = "#E6E7EA",
        accentHex = "#F5832B"
    ),
    MATCH_APP_ACCENT(
        key = "match_app_accent",
        label = "Match App Accent",
        primaryHex = "#2F5D8A",
        deepHex = "#22466A",
        steelHex = "#5E8FB9",
        skyHex = "#CDE3F5",
        softGrayHex = "#E6E7EA",
        accentHex = "#F5832B"
    ),
    CLASSIC_NAVY(
        key = "classic_navy",
        label = "Executive Navy",
        primaryHex = "#1B365D",
        deepHex = "#11243E",
        steelHex = "#486D99",
        skyHex = "#D5E3F3",
        softGrayHex = "#EBEEF2",
        accentHex = "#D97724"
    ),
    CHARCOAL_ORANGE(
        key = "charcoal_orange",
        label = "Charcoal & Orange",
        primaryHex = "#2D323B",
        deepHex = "#1C2026",
        steelHex = "#626B7A",
        skyHex = "#E2E6EC",
        softGrayHex = "#F1F2F4",
        accentHex = "#F5832B"
    ),
    FOREST_TIMBER(
        key = "forest_timber",
        label = "Sage & Timber",
        primaryHex = "#3C5A3E",
        deepHex = "#283E2A",
        steelHex = "#6E8D63",
        skyHex = "#DCE8D5",
        softGrayHex = "#EEF1EC",
        accentHex = "#A67C52"
    ),
    MINIMAL_INK(
        key = "minimal_ink",
        label = "Clean Ink & Slate",
        primaryHex = "#334155",
        deepHex = "#1E293B",
        steelHex = "#64748B",
        skyHex = "#E2E8F0",
        softGrayHex = "#F8FAFC",
        accentHex = "#334155"
    );

    companion object {
        fun fromKey(key: String?): PdfColorTheme =
            entries.firstOrNull { it.key == key } ?: HOUSE_DR_SLATE
    }
}

enum class PdfLogoMode(val key: String, val label: String) {
    EMBLEM_AND_LOCKUP("emblem_and_lockup", "Emblem + Full Logo"),
    EMBLEM_ONLY("emblem_only", "Emblem Only"),
    LOCKUP_ONLY("lockup_only", "Full Logo Only"),
    CUSTOM_PDF_IMAGE("custom_pdf_image", "Custom PDF Upload"),
    TEXT_BADGE("text_badge", "Text Badge Only"),
    HIDDEN("hidden", "Hidden");

    companion object {
        fun fromKey(key: String?): PdfLogoMode =
            entries.firstOrNull { it.key == key } ?: EMBLEM_AND_LOCKUP
    }
}

data class CustomColorScheme(
    val id: String,
    val name: String,
    val isDark: Boolean = false,
    val primaryHex: String = "#2F5D8A",
    val secondaryHex: String = "#5E8FB9",
    val accentHex: String = "#F5832B",
    val backgroundHex: String = "#E6E7EA",
    val surfaceHex: String = "#FFFFFF",
    val textPrimaryHex: String = "#1D2B3A",
    val textMutedHex: String = "#4A4A4A",
    val outlineHex: String = "#5E8FB9",
    val cardOpacityPct: Int = 86,
    val navOpacityPct: Int = 85
)

object ColorHexCodec {
    fun isValidHex(raw: String): Boolean {
        val clean = raw.trim().removePrefix("#")
        return (clean.length == 6 || clean.length == 3) && clean.all {
            it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F'
        }
    }

    fun normalizeHex(raw: String, fallback: String): String {
        val clean = raw.trim().removePrefix("#").uppercase()
        val expanded = when {
            clean.length == 3 && clean.all { it in '0'..'9' || it in 'A'..'F' } ->
                "${clean[0]}${clean[0]}${clean[1]}${clean[1]}${clean[2]}${clean[2]}"
            clean.length == 6 && clean.all { it in '0'..'9' || it in 'A'..'F' } ->
                clean
            else -> return fallback.uppercase()
        }
        return "#$expanded"
    }

    fun parseArgbLong(hex: String, fallbackArgb: Long): Long {
        val norm = normalizeHex(hex, "")
        if (norm.length != 7) return fallbackArgb
        val rgb = norm.substring(1).toLongOrNull(16) ?: return fallbackArgb
        return 0xFF000000L or rgb
    }

    fun argbLongToHex(argb: Long): String {
        val r = ((argb shr 16) and 0xFFL).toInt()
        val g = ((argb shr 8) and 0xFFL).toInt()
        val b = (argb and 0xFFL).toInt()
        return "#" +
            r.toString(16).padStart(2, '0').uppercase() +
            g.toString(16).padStart(2, '0').uppercase() +
            b.toString(16).padStart(2, '0').uppercase()
    }

    fun blendHex(hexA: String, hexB: String, ratioB: Float): String {
        val t = ratioB.coerceIn(0f, 1f)
        val a = parseArgbLong(hexA, 0xFF2F5D8AL)
        val b = parseArgbLong(hexB, 0xFFFFFFFFL)
        val ar = ((a shr 16) and 0xFF).toFloat()
        val ag = ((a shr 8) and 0xFF).toFloat()
        val ab = (a and 0xFF).toFloat()
        val br = ((b shr 16) and 0xFF).toFloat()
        val bg = ((b shr 8) and 0xFF).toFloat()
        val bb = (b and 0xFF).toFloat()
        val r = (ar + (br - ar) * t).toInt().coerceIn(0, 255)
        val g = (ag + (bg - ag) * t).toInt().coerceIn(0, 255)
        val bl = (ab + (bb - ab) * t).toInt().coerceIn(0, 255)
        val combined = 0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or bl.toLong()
        return argbLongToHex(combined)
    }

    fun darkenHex(hex: String, amount: Float): String = blendHex(hex, "#000000", amount)

    fun lightenHex(hex: String, amount: Float): String = blendHex(hex, "#FFFFFF", amount)

    fun relativeLuminance(hex: String): Float {
        val argb = parseArgbLong(hex, 0xFF000000L)
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }
}

data class AppSettings(
    val companyName: String = "EZ ESTIMATOR",
    val companyTagline: String = "HOUSE DR · INTERIOR / EXTERIOR",
    val companyPhone: String = "",
    val companyEmail: String = "",
    val companyAddress: String = "",
    val companyLicense: String = "",
    val themePreset: AppThemePreset = AppThemePreset.HOUSE_DR_STUDIO,
    val accentColor: AppAccentColor = AppAccentColor.COPPER_ORANGE,
    val customColorSchemes: List<CustomColorScheme> = emptyList(),
    val activeCustomSchemeId: String = "",
    val showBlueprintBackdrop: Boolean = true,
    val largeFieldText: Boolean = false,
    val showInternalCostProfit: Boolean = true,
    val defaultHourlyRate: Double = 85.0,
    val defaultTaxPercent: Double = 0.0,
    val currencySymbol: String = "$",
    val defaultEstimateSide: EstimateSide = EstimateSide.INTERIOR,
    val defaultFlooringWastePct: Int = 10,
    val defaultPaintWastePct: Int = 10,
    val defaultDrywallWastePct: Int = 10,
    val defaultTileWastePct: Int = 15,
    val defaultRoofingWastePct: Int = 10,
    val paintCoverageSqFtPerGal: Double = 350.0,
    val defaultProposalValidDays: Int = 30,
    val defaultProposalDepositPct: Double = 25.0,
    val defaultProposalTerms: String = "Pricing valid for 30 days. Work begins upon written approval and deposit. Concealed damage or scope changes require written change order.",
    val includeSignatureBlockOnProposal: Boolean = true,
    val privacyPolicyUrl: String = "",
    // Pre-positioned App Branding Images & Placement
    val headerEmblemChoice: HeaderEmblemChoice = HeaderEmblemChoice.DEFAULT_EMBLEM,
    val customHeaderEmblemPath: String = "",
    val brandLogoChoice: BrandLogoChoice = BrandLogoChoice.DEFAULT_LOCKUP,
    val customBrandLogoPath: String = "",
    val heroBackdropChoice: HeroBackdropChoice = HeroBackdropChoice.DEFAULT_BLUEPRINT,
    val customBackdropPath: String = "",
    val mascotChoice: MascotSlotChoice = MascotSlotChoice.STANDING_MASCOT,
    val customMascotPath: String = "",
    val homeMascotPosition: HeroMascotPosition = HeroMascotPosition.RIGHT,
    val showHomeHeroBanner: Boolean = true,
    val backdropOpacityPct: Int = 18,
    // Complete PDF Customizations
    val pdfColorTheme: PdfColorTheme = PdfColorTheme.HOUSE_DR_SLATE,
    val pdfLogoMode: PdfLogoMode = PdfLogoMode.EMBLEM_AND_LOCKUP,
    val customPdfLogoPath: String = "",
    val pdfShowBrandBadge: Boolean = true,
    val pdfShowCornerPattern: Boolean = true,
    val pdfShowSplitBackdrop: Boolean = true,
    val pdfCompanyTitleOverride: String = "",
    val pdfEstimateTitle: String = "ESTIMATE",
    val pdfProposalTitle: String = "PROPOSAL",
    val pdfDocNumberPrefix: String = "THDR",
    val pdfShowCustomerSection: Boolean = true,
    val pdfCustomerSectionTitle: String = "Customer Information",
    val pdfShowProjectDescription: Boolean = true,
    val pdfProjectSectionTitle: String = "Project Description",
    val pdfShowMaterialsTable: Boolean = true,
    val pdfMaterialsSectionTitle: String = "Materials Takeoff Reference",
    val pdfShowNotesSection: Boolean = true,
    val pdfCustomEstimateNotes: String = "",
    val pdfShowContactSection: Boolean = true,
    val pdfContactSectionTitle: String = "Contact Us",
    val includeSignatureBlockOnEstimate: Boolean = true,
    val pdfContractorSignatureLabel: String = "Contractor",
    val pdfClientSignatureLabel: String = "Client"
) {
    fun activeCustomScheme(): CustomColorScheme? =
        if (activeCustomSchemeId.isBlank()) null
        else customColorSchemes.firstOrNull { it.id == activeCustomSchemeId }

    fun starterSchemeFromCurrent(newId: String, name: String): CustomColorScheme {
        activeCustomScheme()?.let { active ->
            return active.copy(id = newId, name = name)
        }
        val accentHex = ColorHexCodec.argbLongToHex(accentColor.primaryArgb)
        val accentSecHex = ColorHexCodec.argbLongToHex(accentColor.secondaryArgb)
        return when (themePreset) {
            AppThemePreset.HOUSE_DR_STUDIO -> CustomColorScheme(
                id = newId,
                name = name,
                isDark = false,
                primaryHex = "#2F5D8A",
                secondaryHex = "#5E8FB9",
                accentHex = accentHex,
                backgroundHex = "#E6E7EA",
                surfaceHex = "#FFFFFF",
                textPrimaryHex = "#1D2B3A",
                textMutedHex = "#4A4A4A",
                outlineHex = "#5E8FB9",
                cardOpacityPct = 86,
                navOpacityPct = 85
            )
            AppThemePreset.BLUEPRINT_NAVY -> CustomColorScheme(
                id = newId,
                name = name,
                isDark = true,
                primaryHex = "#2F5D8A",
                secondaryHex = "#5E8FB9",
                accentHex = accentHex,
                backgroundHex = "#1B334D",
                surfaceHex = "#233F5E",
                textPrimaryHex = "#FFFFFF",
                textMutedHex = "#CDE3F5",
                outlineHex = "#5E8FB9",
                cardOpacityPct = 85,
                navOpacityPct = 85
            )
            AppThemePreset.MIDNIGHT_SLATE -> CustomColorScheme(
                id = newId,
                name = name,
                isDark = true,
                primaryHex = "#2F5D8A",
                secondaryHex = accentSecHex,
                accentHex = accentHex,
                backgroundHex = "#1D2229",
                surfaceHex = "#282F38",
                textPrimaryHex = "#FFFFFF",
                textMutedHex = "#C5CCD6",
                outlineHex = "#5E8FB9",
                cardOpacityPct = 85,
                navOpacityPct = 85
            )
            AppThemePreset.WORKSHOP_PINE -> CustomColorScheme(
                id = newId,
                name = name,
                isDark = true,
                primaryHex = "#5D7A4A",
                secondaryHex = "#8FAA77",
                accentHex = accentHex,
                backgroundHex = "#1A2420",
                surfaceHex = "#24332C",
                textPrimaryHex = "#FFFFFF",
                textMutedHex = "#D2DEC8",
                outlineHex = "#8FAA77",
                cardOpacityPct = 85,
                navOpacityPct = 85
            )
        }
    }
}
