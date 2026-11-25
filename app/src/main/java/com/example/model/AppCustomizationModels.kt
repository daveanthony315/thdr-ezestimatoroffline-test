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
    BLUEPRINT_NAVY(
        key = "blueprint_navy",
        label = "Blueprint Navy",
        description = "Signature THDR architectural dark blue",
        isDark = true
    ),
    MIDNIGHT_SLATE(
        key = "midnight_slate",
        label = "Midnight Slate",
        description = "Neutral dark graphite for low glare",
        isDark = true
    ),
    STEEL_DAYLIGHT(
        key = "steel_daylight",
        label = "Steel Daylight",
        description = "High-contrast light mode for outdoor sunlight",
        isDark = false
    ),
    WORKSHOP_PINE(
        key = "workshop_pine",
        label = "Workshop Pine",
        description = "Deep forest slate with crisp contrast",
        isDark = true
    );

    companion object {
        fun fromKey(key: String?): AppThemePreset =
            entries.firstOrNull { it.key == key } ?: BLUEPRINT_NAVY
    }
}

enum class AppAccentColor(
    val key: String,
    val label: String,
    val primaryArgb: Long,
    val secondaryArgb: Long
) {
    COPPER_ORANGE("orange", "Copper Orange", 0xFFE26C1A, 0xFFEA9636),
    ELECTRIC_CYAN("cyan", "Electric Cyan", 0xFF0A68D3, 0xFF49B2F3),
    EMERALD_TEAL("teal", "Emerald Teal", 0xFF0E8E88, 0xFF31D5C8),
    GOLD_AMBER("amber", "Amber Gold", 0xFFD98218, 0xFFFFB74D);

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
    val themePreset: AppThemePreset = AppThemePreset.BLUEPRINT_NAVY,
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
