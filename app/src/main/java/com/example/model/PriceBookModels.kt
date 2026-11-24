package com.example.model

enum class PriceViewMode(val label: String) {
    BENCHMARK("THDR benchmark"),
    LOW("Low planning rate"),
    HIGH("High planning rate")
}

enum class MeasurementBasis(val key: String, val label: String) {
    AUTO("auto", "Automatic from unit"),
    FLOOR_AREA("floor_area", "Floor area (SF)"),
    WALL_AREA("wall_area", "Wall area (SF)"),
    CEILING_AREA("ceiling_area", "Ceiling area (SF)"),
    WALLS_CEILING_AREA("walls_ceiling_area", "Walls + ceiling area (SF)"),
    PERIMETER("perimeter", "Room perimeter (LF)"),
    FENCE_LENGTH("fence_length", "Fence length (LF)"),
    FLOOR_SQUARES("floor_squares", "Floor area converted to squares (SQ)"),
    VOLUME_CF("volume_cf", "Room volume (CF)"),
    VOLUME_CY("volume_cy", "Room volume (CY)"),
    DOOR_COUNT("door_count", "Door count (EA)"),
    WINDOW_COUNT("window_count", "Window count (EA)"),
    ONE_PER_ROOM("one_per_room", "One per room"),
    MANUAL("manual", "Manual quantity");

    companion object {
        fun fromKey(key: String?): MeasurementBasis =
            entries.firstOrNull { it.key == key } ?: AUTO

        fun optionsForUnit(unit: String): List<MeasurementBasis> {
            val specific = when (unit.uppercase()) {
                "SF" -> listOf(FLOOR_AREA, WALL_AREA, CEILING_AREA, WALLS_CEILING_AREA)
                "LF" -> listOf(PERIMETER, FENCE_LENGTH)
                "SQ" -> listOf(FLOOR_SQUARES)
                "CF" -> listOf(VOLUME_CF)
                "CY" -> listOf(VOLUME_CY)
                "EA" -> listOf(DOOR_COUNT, WINDOW_COUNT, ONE_PER_ROOM)
                "ROOM" -> listOf(ONE_PER_ROOM)
                else -> emptyList()
            }
            return listOf(AUTO) + specific + listOf(MANUAL)
        }
    }
}

data class PriceBookItem(
    val id: Long = 0,
    val category: String,
    val item: String,
    val unit: String = "EA",
    val measurementBasis: String = "auto",
    val marketSection: String = "Interior",
    val laborRate: Double = 85.0,
    val hourlyRate: Double = 85.0,
    val estimatedCompletionHours: Double = 1.0,
    val benchmarkLaborHoursPerUnit: Double = 0.0,
    val lowHoursPerUnit: Double = 0.0,
    val highHoursPerUnit: Double = 0.0,
    val benchmarkUnitPrice: Double = 0.0,
    val lowUnitPrice: Double = 0.0,
    val highUnitPrice: Double = 0.0,
    val benchmarkProductionUnitsPerLaborHour: Double = 0.0,
    val crewAssumption: String = "1-person crew",
    val crewCount: Double = 1.0,
    val minimumLaborCharge: Double = 0.0,
    val standardCondition: String = "Standard",
    val adjustmentEasy: Double = 0.85,
    val adjustmentStandard: Double = 1.0,
    val adjustmentDifficult: Double = 1.25,
    val adjustmentVeryDifficult: Double = 1.5,
    val scopeNotes: String = "",
    val materialIncluded: Boolean = false,
    val equipmentIncluded: Boolean = false,
    val permitsIncluded: Boolean = false,
    val cleanupIncluded: String = "Not specified",
    val sourceStatus: String = "Custom THDR entry"
) {
    fun effectiveBasis(): MeasurementBasis {
        val basis = MeasurementBasis.fromKey(measurementBasis)
        return if (MeasurementBasis.optionsForUnit(unit).contains(basis)) basis else MeasurementBasis.AUTO
    }

    fun defaultEstimateRate(): Double =
        if (unit.equals("HR", ignoreCase = true)) hourlyRate else benchmarkUnitPrice

    fun displayUnitPrice(mode: PriceViewMode): Double = when (mode) {
        PriceViewMode.BENCHMARK -> benchmarkUnitPrice
        PriceViewMode.LOW -> lowUnitPrice
        PriceViewMode.HIGH -> highUnitPrice
    }
}

data class CategoryMeta(
    val name: String,
    val description: String = ""
)

data class TimeframeBenchmark(
    val id: Long = 0,
    val projectType: String,
    val category: String,
    val typicalScope: String,
    val crewCalendar: String,
    val lowHours: Double,
    val highHours: Double,
    val notes: String
)

object PriceBookConstants {
    val STANDARD_UNITS = listOf(
        "EA", "SF", "LF", "CF", "CY", "SQ", "HR", "ROOM", "STEP", "FLIGHT", "FLAT"
    )
    const val FILTER_ALL = ""
    const val FILTER_INTERIOR = "__INTERIOR__"
    const val FILTER_EXTERIOR = "__EXTERIOR__"
}
