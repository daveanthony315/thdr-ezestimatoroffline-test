package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CategoryMeta
import com.example.model.PriceBookItem
import com.example.model.SavedProposal
import com.example.model.TimeframeBenchmark

@Entity(tableName = "price_book_categories")
data class PriceBookCategoryEntity(
    @PrimaryKey val name: String,
    val description: String = ""
) {
    fun toModel() = CategoryMeta(name = name, description = description)
}

@Entity(tableName = "price_book_items")
data class PriceBookItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val item: String,
    val unit: String,
    val measurementBasis: String,
    val marketSection: String,
    val laborRate: Double,
    val hourlyRate: Double,
    val estimatedCompletionHours: Double,
    val benchmarkLaborHoursPerUnit: Double,
    val lowHoursPerUnit: Double,
    val highHoursPerUnit: Double,
    val benchmarkUnitPrice: Double,
    val lowUnitPrice: Double,
    val highUnitPrice: Double,
    val benchmarkProductionUnitsPerLaborHour: Double,
    val crewAssumption: String,
    val crewCount: Double,
    val minimumLaborCharge: Double,
    val standardCondition: String,
    val adjustmentEasy: Double,
    val adjustmentStandard: Double,
    val adjustmentDifficult: Double,
    val adjustmentVeryDifficult: Double,
    val scopeNotes: String,
    val materialIncluded: Boolean,
    val equipmentIncluded: Boolean,
    val permitsIncluded: Boolean,
    val cleanupIncluded: String,
    val sourceStatus: String
) {
    fun toModel() = PriceBookItem(
        id = id,
        category = category,
        item = item,
        unit = unit,
        measurementBasis = measurementBasis,
        marketSection = marketSection,
        laborRate = laborRate,
        hourlyRate = hourlyRate,
        estimatedCompletionHours = estimatedCompletionHours,
        benchmarkLaborHoursPerUnit = benchmarkLaborHoursPerUnit,
        lowHoursPerUnit = lowHoursPerUnit,
        highHoursPerUnit = highHoursPerUnit,
        benchmarkUnitPrice = benchmarkUnitPrice,
        lowUnitPrice = lowUnitPrice,
        highUnitPrice = highUnitPrice,
        benchmarkProductionUnitsPerLaborHour = benchmarkProductionUnitsPerLaborHour,
        crewAssumption = crewAssumption,
        crewCount = crewCount,
        minimumLaborCharge = minimumLaborCharge,
        standardCondition = standardCondition,
        adjustmentEasy = adjustmentEasy,
        adjustmentStandard = adjustmentStandard,
        adjustmentDifficult = adjustmentDifficult,
        adjustmentVeryDifficult = adjustmentVeryDifficult,
        scopeNotes = scopeNotes,
        materialIncluded = materialIncluded,
        equipmentIncluded = equipmentIncluded,
        permitsIncluded = permitsIncluded,
        cleanupIncluded = cleanupIncluded,
        sourceStatus = sourceStatus
    )

    companion object {
        fun fromModel(model: PriceBookItem) = PriceBookItemEntity(
            id = model.id,
            category = model.category,
            item = model.item,
            unit = model.unit,
            measurementBasis = model.measurementBasis,
            marketSection = model.marketSection,
            laborRate = model.laborRate,
            hourlyRate = model.hourlyRate,
            estimatedCompletionHours = model.estimatedCompletionHours,
            benchmarkLaborHoursPerUnit = model.benchmarkLaborHoursPerUnit,
            lowHoursPerUnit = model.lowHoursPerUnit,
            highHoursPerUnit = model.highHoursPerUnit,
            benchmarkUnitPrice = model.benchmarkUnitPrice,
            lowUnitPrice = model.lowUnitPrice,
            highUnitPrice = model.highUnitPrice,
            benchmarkProductionUnitsPerLaborHour = model.benchmarkProductionUnitsPerLaborHour,
            crewAssumption = model.crewAssumption,
            crewCount = model.crewCount,
            minimumLaborCharge = model.minimumLaborCharge,
            standardCondition = model.standardCondition,
            adjustmentEasy = model.adjustmentEasy,
            adjustmentStandard = model.adjustmentStandard,
            adjustmentDifficult = model.adjustmentDifficult,
            adjustmentVeryDifficult = model.adjustmentVeryDifficult,
            scopeNotes = model.scopeNotes,
            materialIncluded = model.materialIncluded,
            equipmentIncluded = model.equipmentIncluded,
            permitsIncluded = model.permitsIncluded,
            cleanupIncluded = model.cleanupIncluded,
            sourceStatus = model.sourceStatus
        )
    }
}

@Entity(tableName = "timeframe_benchmarks")
data class TimeframeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectType: String,
    val category: String,
    val typicalScope: String,
    val crewCalendar: String,
    val lowHours: Double,
    val highHours: Double,
    val notes: String
) {
    fun toModel() = TimeframeBenchmark(
        id = id,
        projectType = projectType,
        category = category,
        typicalScope = typicalScope,
        crewCalendar = crewCalendar,
        lowHours = lowHours,
        highHours = highHours,
        notes = notes
    )

    companion object {
        fun fromModel(model: TimeframeBenchmark) = TimeframeEntity(
            id = model.id,
            projectType = model.projectType,
            category = model.category,
            typicalScope = model.typicalScope,
            crewCalendar = model.crewCalendar,
            lowHours = model.lowHours,
            highHours = model.highHours,
            notes = model.notes
        )
    }
}

@Entity(tableName = "saved_estimates")
data class SavedEstimateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customer: String,
    val project: String,
    val date: String,
    val total: Double,
    val savedAt: String,
    val payloadJson: String
) {
    fun toModel() = SavedProposal(
        id = id,
        customer = customer,
        project = project,
        date = date,
        total = total,
        savedAt = savedAt,
        payloadJson = payloadJson
    )
}
