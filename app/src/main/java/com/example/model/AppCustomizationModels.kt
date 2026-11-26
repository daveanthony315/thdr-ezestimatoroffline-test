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

data class AppSettings(
    val companyName: String = "EZ ESTIMATOR",
    val companyTagline: String = "HOUSE DR · INTERIOR / EXTERIOR",
    val companyPhone: String = "",
    val companyEmail: String = "",
    val companyAddress: String = "",
    val companyLicense: String = "",
    val themePreset: AppThemePreset = AppThemePreset.HOUSE_DR_STUDIO,
    val accentColor: AppAccentColor = AppAccentColor.COPPER_ORANGE,
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
    val privacyPolicyUrl: String = ""
)
