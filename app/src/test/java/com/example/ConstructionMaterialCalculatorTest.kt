package com.example

import com.example.domain.ConstructionMaterialCalculator
import com.example.domain.ConstructionMaterialCalculator.FlooringPattern
import com.example.domain.EstimatorEngine
import com.example.model.EstimateRoom
import com.example.model.EstimateSide
import com.example.model.MeasurementBasis
import com.example.model.PriceBookItem
import com.example.model.PricingUnitCategory
import com.example.model.RoomServiceSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstructionMaterialCalculatorTest {

    @Test
    fun measurementFormulasMatchSpecification() {
        // Floor & ceiling area: Length × Width
        assertEquals(180.0, ConstructionMaterialCalculator.floorArea(15.0, 12.0), 0.001)
        assertEquals(180.0, ConstructionMaterialCalculator.ceilingArea(15.0, 12.0), 0.001)

        // Wall area / perimeter: (Length + Width) × 2 (and × Height for surface SF)
        assertEquals(54.0, ConstructionMaterialCalculator.wallPerimeter(15.0, 12.0), 0.001)
        assertEquals(54.0, ConstructionMaterialCalculator.wallArea(15.0, 12.0), 0.001)
        assertEquals(486.0, ConstructionMaterialCalculator.wallArea(15.0, 12.0, 9.0), 0.001)

        // Linear footage: Sum of measured lengths + Width × 2
        assertEquals(44.0, ConstructionMaterialCalculator.linearFootage(listOf(10.0, 10.0), 12.0), 0.001)
        assertEquals(54.0, ConstructionMaterialCalculator.linearFootage(15.0, 12.0), 0.001)

        // Roofing squares: Roof area / 100 (10×10 sqft = 100²ft)
        assertEquals(1.0, ConstructionMaterialCalculator.roofingSquares(100.0), 0.001)
        assertEquals(18.5, ConstructionMaterialCalculator.roofingSquares(1850.0), 0.001)
        assertEquals(2.4, ConstructionMaterialCalculator.roofingSquares(20.0, 12.0), 0.001)
    }

    @Test
    fun flooringAndPaintWasteFactorsCalculateAccurately() {
        // 12 x 10 room = 120 SF
        val inline = ConstructionMaterialCalculator.calculateFlooringForRoom(12.0, 10.0, FlooringPattern.STANDARD_INLINE)
        assertEquals(120.0, inline.netAreaSqFt, 0.001)
        assertEquals(132.0, inline.recommendedOrderSqFt, 0.001) // 10% waste

        val diagonal = ConstructionMaterialCalculator.calculateFlooringForRoom(12.0, 10.0, FlooringPattern.DIAGONAL)
        assertEquals(138.0, diagonal.recommendedOrderSqFt, 0.001) // 15% waste

        val herringbone = ConstructionMaterialCalculator.calculateFlooringForRoom(12.0, 10.0, FlooringPattern.HERRINGBONE)
        assertEquals(144.0, herringbone.recommendedOrderSqFt, 0.001) // 20% waste

        // Paint for 15 x 12 x 8 room = wall area 432 SF, 2 windows (-30 SF), 1 door (-20 SF) -> net 382 SF
        // 2 coats = 764 SF; without waste = ceil(764/350)=3 gal; with 10% waste (840.4/350)=3 gal
        val paint = ConstructionMaterialCalculator.calculatePaintForRoom(
            lengthFt = 15.0,
            widthFt = 12.0,
            heightFt = 8.0,
            includeWalls = true,
            includeCeiling = false,
            windows = 2,
            doors = 1,
            coats = 2
        )
        assertEquals(432.0, paint.grossSurfaceSqFt, 0.001)
        assertEquals(50.0, paint.openingDeductionsSqFt, 0.001)
        assertEquals(382.0, paint.netPaintableSqFt, 0.001)
        assertEquals(3.0, paint.gallonsWithoutWaste, 0.001)
        assertEquals(3.0, paint.gallonsWithWaste, 0.001)
        assertEquals(1.0, paint.painterTapeRolls, 0.001)

        // Room summary helper
        val calc = ConstructionMaterialCalculator()
        val room = EstimateRoom(id = "r1", name = "Living", lengthFt = 20.0, widthFt = 15.0, heightFt = 9.0, windows = 2, doors = 1)
        val summary = calc.calculateForRoom(room)
        assertEquals(300.0, summary.measurements.floorAreaSqFt, 0.001)
        assertEquals(630.0, summary.measurements.wallAreaSqFt, 0.001)
        assertEquals(70.0, summary.measurements.linearFootageFt, 0.001)
        assertEquals(3.0, summary.measurements.roofingSquares, 0.001)
        assertEquals(330.0, summary.flooring.recommendedOrderSqFt, 0.001)
        assertEquals(345.0, summary.tile.recommendedOrderSqFt, 0.001)
    }

    @Test
    fun pricingUnitSelectionAndIndividualUnitPricePresetsWorkAcrossModes() {
        val item = PriceBookItem(
            category = "006 Flooring",
            item = "LVP Installation",
            unit = "SF",
            laborRate = 85.0,
            hourlyRate = 95.0,
            lowUnitPrice = 2.50,
            benchmarkUnitPrice = 3.25,
            highUnitPrice = 4.50,
            benchmarkLaborHoursPerUnit = 0.03
        )
        val roomBase = EstimateRoom(
            id = "r1",
            name = "Bedroom",
            side = EstimateSide.INTERIOR,
            lengthFt = 10.0,
            widthFt = 12.0,
            heightFt = 8.0,
            doors = 2,
            services = listOf("Flooring")
        )

        // 1) Measurement unit (SF, floor area = 120 SF @ $3.25)
        assertEquals(PricingUnitCategory.MEASUREMENT, PricingUnitCategory.fromUnit("SF"))
        val syncedSf = EstimatorEngine.syncRoomServiceSelections(roomBase, listOf(item))
        val selSf = syncedSf.serviceSelections["Flooring"]!!
        assertEquals(120.0, selSf.quantity, 0.001)
        assertEquals(3.25, selSf.rate, 0.001)

        // 2) Override to Hourly (HR) -> rate switches to hourlyRate ($95.0), qty = 1.0 hr
        assertEquals(PricingUnitCategory.HOURLY, PricingUnitCategory.fromUnit("HR"))
        val roomHr = roomBase.copy(
            serviceSelections = mapOf(
                "Flooring" to RoomServiceSelection(
                    serviceName = "Flooring",
                    selectedPbItemName = item.item,
                    overrideUnit = "HR"
                )
            )
        )
        val syncedHr = EstimatorEngine.syncRoomServiceSelections(roomHr, listOf(item))
        val selHr = syncedHr.serviceSelections["Flooring"]!!
        assertEquals(1.0, selHr.quantity, 0.001)
        assertEquals(95.0, selHr.rate, 0.001)

        // 3) Override to Individual Unit (EA) with Door Count basis & custom unit price
        assertEquals(PricingUnitCategory.INDIVIDUAL, PricingUnitCategory.fromUnit("EA"))
        val roomEa = roomBase.copy(
            serviceSelections = mapOf(
                "Flooring" to RoomServiceSelection(
                    serviceName = "Flooring",
                    selectedPbItemName = item.item,
                    overrideUnit = "EA",
                    overrideMeasurementBasis = MeasurementBasis.DOOR_COUNT.key,
                    rate = 150.0,
                    rateInitialized = true
                )
            )
        )
        val syncedEa = EstimatorEngine.syncRoomServiceSelections(roomEa, listOf(item))
        val selEa = syncedEa.serviceSelections["Flooring"]!!
        assertEquals(2.0, selEa.quantity, 0.001)
        assertEquals(150.0, selEa.rate, 0.001)
        assertEquals(300.0, selEa.lineTotal, 0.001)

        // 4) Presets check for SF, SQ, HR
        val sfPresets = item.unitPricePresetsFor("SF")
        assertTrue(sfPresets.any { it.label == "Low" && it.amount == 2.50 })
        assertTrue(sfPresets.any { it.label == "Benchmark" && it.amount == 3.25 })
        assertTrue(sfPresets.any { it.label == "High" && it.amount == 4.50 })

        val sqPresets = item.unitPricePresetsFor("SQ")
        assertTrue(sqPresets.any { it.label == "Benchmark" && it.amount == 325.0 })

        val hrPresets = item.unitPricePresetsFor("HR")
        assertTrue(hrPresets.any { it.label == "Hourly" && it.amount == 95.0 })
    }
}
