package com.example

import com.example.domain.EstimatorEngine
import com.example.model.EstimateRoom
import com.example.model.EstimateSide
import com.example.model.MaterialCalcKind
import com.example.model.PriceBookItem
import com.example.model.RoomServiceSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EstimatorEngineTest {

    @Test
    fun roomDimensionsAndTakeoffMatchOriginalFormulas() {
        val pb = listOf(
            PriceBookItem(
                category = "004 Drywall",
                item = "Hang & Finish Drywall",
                unit = "SF",
                laborRate = 85.0,
                benchmarkLaborHoursPerUnit = 0.02,
                benchmarkUnitPrice = 4.0
            ),
            PriceBookItem(
                category = "005 Painting",
                item = "Interior Walls Painting",
                unit = "SF",
                laborRate = 85.0,
                benchmarkLaborHoursPerUnit = 0.01,
                benchmarkUnitPrice = 2.0
            )
        )
        val room = EstimateRoom(
            id = "r1",
            name = "Bedroom",
            side = EstimateSide.INTERIOR,
            lengthFt = 10.0,
            widthFt = 12.0,
            heightFt = 8.0,
            services = listOf("Drywall", "Painting"),
            serviceSelections = mapOf(
                "Drywall" to RoomServiceSelection(
                    serviceName = "Drywall",
                    selectedPbItemName = "Hang & Finish Drywall",
                    quantity = 472.0,
                    rate = 4.0,
                    rateInitialized = true
                ),
                "Painting" to RoomServiceSelection(
                    serviceName = "Painting",
                    selectedPbItemName = "Interior Walls Painting",
                    quantity = 472.0,
                    rate = 2.0,
                    rateInitialized = true
                )
            )
        )

        val dims = EstimatorEngine.computeDimensions(room)
        assertEquals(120.0, dims.floor, 0.001)
        assertEquals(352.0, dims.wall, 0.001)
        assertEquals(120.0, dims.ceiling, 0.001)
        assertEquals(44.0, dims.perim, 0.001)

        val takeoff = EstimatorEngine.computeTakeoff(
            rooms = listOf(room),
            priceBook = pb,
            manualMaterials = emptyMap(),
            discount = 100.0,
            taxPercent = 8.0
        )

        assertEquals(2832.0, takeoff.serviceTotal, 0.001)
        assertEquals(2950.56, takeoff.grandTotal, 0.01)
        assertEquals(17.0, takeoff.materials["1/2-inch drywall sheets (4x8, incl. 10% waste)"] ?: 0.0, 0.001)
        assertEquals(408.0, takeoff.materials["Drywall tape (LF)"] ?: 0.0, 0.001)
        assertEquals(3.0, takeoff.materials["Paint / primer gallons (350 SF per gallon)"] ?: 0.0, 0.001)
    }

    @Test
    fun materialCalculatorsComputeExpectedQuantities() {
        val conc = EstimatorEngine.calculateMaterialTool(MaterialCalcKind.CONCRETE, 10.0, 10.0, 6.0).getOrThrow()
        assertEquals(92.0, conc.first { it.key == "concrete::60" }.qty, 0.001)
        assertEquals(70.0, conc.first { it.key == "concrete::80" }.qty, 0.001)

        val cmu = EstimatorEngine.calculateMaterialTool(MaterialCalcKind.CMU, 20.0, 8.0).getOrThrow()
        assertEquals(189.0, cmu.first().qty, 0.001)

        val paint = EstimatorEngine.calculateMaterialTool(MaterialCalcKind.PAINT, 400.0, 2.0, 1.0, 2.0).getOrThrow()
        assertEquals(350.0, paint.first { it.key == "paint::net" }.qty, 0.001)
        assertEquals(2.0, paint.first { it.key == "paint::gal" }.qty, 0.001)
    }

    @Test
    fun exteriorServiceFilteringMatchesOriginalRules() {
        val pb = listOf(
            PriceBookItem(category = "008 Doors & Windows", item = "Interior Prehung Door", unit = "EA"),
            PriceBookItem(category = "008 Doors & Windows", item = "Exterior Entry Door", unit = "EA"),
            PriceBookItem(category = "005 Painting", item = "Interior Walls Repaint", unit = "SF"),
            PriceBookItem(category = "005 Painting", item = "Deck Staining", unit = "SF")
        )
        val winDoors = EstimatorEngine.pbItemsForService("Windows & Doors", pb)
        assertEquals(1, winDoors.size)
        assertEquals("Exterior Entry Door", winDoors.first().item)

        val extPaint = EstimatorEngine.pbItemsForService("Exterior Painting", pb)
        assertEquals(1, extPaint.size)
        assertTrue(extPaint.first().item == "Deck Staining")
    }
}
