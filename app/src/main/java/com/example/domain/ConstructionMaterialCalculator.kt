package com.example.domain

import com.example.model.EstimateRoom
import com.example.model.RoomDimensions
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.round

/**
 * Utility class for calculating construction room measurements and material requirements
 * with standard industry waste factors.
 *
 * Supported measurement formulas:
 * - Floor area: `Length × Width`
 * - Ceiling area: `Length × Width`
 * - Wall perimeter / 2D run: `(Length + Width) × 2`
 * - Wall surface area: `(Length + Width) × 2 × Height`
 * - Linear footage: `Sum of measured lengths + (Width × 2)` (or `(Length × 2) + (Width × 2)` for a room)
 * - Roofing squares: `Roof area / 100.0` (one 10×10 ft square = 100 sq ft)
 */
class ConstructionMaterialCalculator {

    enum class FlooringPattern(
        val wasteFactor: Double,
        val label: String
    ) {
        STANDARD_INLINE(0.10, "Standard inline — 10% waste"),
        CARPET_VINYL_ROLL(0.10, "Carpet / vinyl roll goods — 10% allowance"),
        DIAGONAL(0.15, "Diagonal / intricate — 15% waste"),
        HERRINGBONE(0.20, "Complex herringbone / chevron — 20% waste")
    }

    data class RoomMeasurementSummary(
        val floorAreaSqFt: Double,
        val ceilingAreaSqFt: Double,
        val wallPerimeterFt: Double,
        val wallAreaSqFt: Double,
        val linearFootageFt: Double,
        val roofingSquares: Double,
        val volumeCubicFt: Double,
        val volumeCubicYd: Double
    ) {
        fun toRoomDimensions(): RoomDimensions = RoomDimensions(
            floor = floorAreaSqFt,
            wall = wallAreaSqFt,
            ceiling = ceilingAreaSqFt,
            perim = wallPerimeterFt
        )
    }

    data class FlooringRequirement(
        val netAreaSqFt: Double,
        val wasteFactor: Double,
        val grossAreaWithWasteSqFt: Double,
        val recommendedOrderSqFt: Double,
        val underlaymentSqFt: Double
    )

    data class TileRequirement(
        val netAreaSqFt: Double,
        val wasteFactor: Double,
        val recommendedOrderSqFt: Double,
        val thinsetBags: Double,
        val groutBags: Double
    )

    data class PaintRequirement(
        val grossSurfaceSqFt: Double,
        val openingDeductionsSqFt: Double,
        val netPaintableSqFt: Double,
        val coats: Int,
        val wasteFactor: Double,
        val gallonsWithoutWaste: Double,
        val gallonsWithWaste: Double,
        val painterTapeRolls: Double
    )

    data class DrywallRequirement(
        val totalSurfaceSqFt: Double,
        val sheetSizeSqFt: Double,
        val wasteFactor: Double,
        val sheetsNeeded: Double,
        val jointCompoundBuckets: Double,
        val drywallTapeLf: Double
    )

    data class RoofingRequirement(
        val roofAreaSqFt: Double,
        val netSquares: Double,
        val wasteFactor: Double,
        val squaresWithWaste: Double,
        val shingleBundles: Double
    )

    data class ConcreteRequirement(
        val cubicYardsWithWaste: Double,
        val bags60Lb: Double,
        val bags80Lb: Double,
        val wasteFactor: Double
    )

    data class CmuRequirement(
        val wallAreaSqFt: Double,
        val wasteFactor: Double,
        val blocksNeeded: Double
    )

    data class FramingRequirement(
        val wallLengthLf: Double,
        val wallSurfaceSqFt: Double,
        val studWasteFactor: Double,
        val studsNeeded: Double,
        val platesLf: Double
    )

    data class RoomMaterialSummary(
        val measurements: RoomMeasurementSummary,
        val flooring: FlooringRequirement,
        val tile: TileRequirement,
        val paintWallsOnly: PaintRequirement,
        val paintWallsAndCeiling: PaintRequirement,
        val drywall: DrywallRequirement,
        val roofing: RoofingRequirement
    )

    fun measureRoom(room: EstimateRoom): RoomMeasurementSummary =
        measureRoom(room.lengthFt, room.widthFt, room.heightFt)

    fun calculateForRoom(
        room: EstimateRoom,
        flooringPattern: FlooringPattern = FlooringPattern.STANDARD_INLINE,
        paintCoats: Int = 2,
        paintWasteFactor: Double = PAINT_WASTE_FACTOR
    ): RoomMaterialSummary = calculateRoomMaterials(
        lengthFt = room.lengthFt,
        widthFt = room.widthFt,
        heightFt = room.heightFt,
        windows = room.windows,
        doors = room.doors,
        flooringPattern = flooringPattern,
        paintCoats = paintCoats,
        paintWasteFactor = paintWasteFactor
    )

    companion object {
        const val FLOORING_STANDARD_WASTE = 0.10
        const val FLOORING_DIAGONAL_WASTE = 0.15
        const val FLOORING_HERRINGBONE_WASTE = 0.20
        const val TILE_WASTE_FACTOR = 0.15
        const val PAINT_WASTE_FACTOR = 0.10
        const val DRYWALL_WASTE_FACTOR = 0.10
        const val FRAMING_STUD_WASTE_FACTOR = 0.15
        const val ROOFING_WASTE_FACTOR = 0.10
        const val CONCRETE_WASTE_FACTOR = 0.10
        const val CMU_WASTE_FACTOR = 0.05

        const val PAINT_COVERAGE_SF_PER_GALLON = 350.0
        const val WINDOW_DEDUCTION_SF = 15.0
        const val DOOR_DEDUCTION_SF = 20.0
        const val ROOFING_SQUARE_SQFT = 100.0 // 10×10 sq ft

        /** Floor area = Length × Width */
        fun floorArea(lengthFt: Double, widthFt: Double): Double =
            max(0.0, lengthFt) * max(0.0, widthFt)

        /** Ceiling area = Length × Width */
        fun ceilingArea(lengthFt: Double, widthFt: Double): Double =
            max(0.0, lengthFt) * max(0.0, widthFt)

        /** Wall perimeter / horizontal run = (Length + Width) × 2 */
        fun wallPerimeter(lengthFt: Double, widthFt: Double): Double =
            (max(0.0, lengthFt) + max(0.0, widthFt)) * 2.0

        /**
         * Wall surface area = (Length + Width) × 2 × Height.
         * When [heightFt] is omitted (default 1.0), reduces to `(Length + Width) × 2`.
         */
        fun wallArea(lengthFt: Double, widthFt: Double, heightFt: Double = 1.0): Double =
            wallPerimeter(lengthFt, widthFt) * max(0.0, heightFt)

        /** Linear footage = Sum of measured lengths + (Width × 2) */
        fun linearFootage(measuredLengthsFt: List<Double>, widthFt: Double = 0.0): Double =
            measuredLengthsFt.sumOf { max(0.0, it) } + (max(0.0, widthFt) * 2.0)

        /** Linear footage for a rectangular room = (Length × 2) + (Width × 2) */
        fun linearFootage(lengthFt: Double, widthFt: Double): Double =
            (max(0.0, lengthFt) * 2.0) + (max(0.0, widthFt) * 2.0)

        /** Roofing squares = Roof area ÷ 100 (10×10 sq ft per square) */
        fun roofingSquares(roofAreaSqFt: Double): Double =
            max(0.0, roofAreaSqFt) / ROOFING_SQUARE_SQFT

        /** Roofing squares from plan Length × Width and optional pitch factor */
        fun roofingSquares(lengthFt: Double, widthFt: Double, pitchMultiplier: Double = 1.0): Double =
            roofingSquares(floorArea(lengthFt, widthFt) * max(0.0, pitchMultiplier))

        fun measureRoom(
            lengthFt: Double,
            widthFt: Double,
            heightFt: Double
        ): RoomMeasurementSummary {
            val l = max(0.0, lengthFt)
            val w = max(0.0, widthFt)
            val h = max(0.0, heightFt)
            val floor = floorArea(l, w)
            val ceiling = ceilingArea(l, w)
            val perim = wallPerimeter(l, w)
            val wall = wallArea(l, w, h)
            val linear = linearFootage(l, w)
            val squares = roofingSquares(floor)
            val cf = floor * h
            val cy = cf / 27.0
            return RoomMeasurementSummary(
                floorAreaSqFt = floor,
                ceilingAreaSqFt = ceiling,
                wallPerimeterFt = perim,
                wallAreaSqFt = wall,
                linearFootageFt = linear,
                roofingSquares = squares,
                volumeCubicFt = cf,
                volumeCubicYd = cy
            )
        }

        fun calculateFlooring(
            netAreaSqFt: Double,
            wasteFactor: Double = FLOORING_STANDARD_WASTE,
            underlaymentWasteFactor: Double = 0.0
        ): FlooringRequirement {
            val net = max(0.0, netAreaSqFt)
            val safeWaste = max(0.0, wasteFactor)
            val gross = net * (1.0 + safeWaste)
            return FlooringRequirement(
                netAreaSqFt = net,
                wasteFactor = safeWaste,
                grossAreaWithWasteSqFt = gross,
                recommendedOrderSqFt = ceilSnap(gross),
                underlaymentSqFt = ceilSnap(net * (1.0 + max(0.0, underlaymentWasteFactor)))
            )
        }

        fun calculateFlooringForRoom(
            lengthFt: Double,
            widthFt: Double,
            pattern: FlooringPattern = FlooringPattern.STANDARD_INLINE
        ): FlooringRequirement = calculateFlooring(
            netAreaSqFt = floorArea(lengthFt, widthFt),
            wasteFactor = pattern.wasteFactor
        )

        fun calculateTile(
            netAreaSqFt: Double,
            wasteFactor: Double = TILE_WASTE_FACTOR
        ): TileRequirement {
            val net = max(0.0, netAreaSqFt)
            val safeWaste = max(0.0, wasteFactor)
            return TileRequirement(
                netAreaSqFt = net,
                wasteFactor = safeWaste,
                recommendedOrderSqFt = ceilSnap(net * (1.0 + safeWaste)),
                thinsetBags = ceilSnap(net / 50.0),
                groutBags = ceilSnap(net / 100.0)
            )
        }

        fun calculatePaint(
            surfaceAreaSqFt: Double,
            windows: Int = 0,
            doors: Int = 0,
            coats: Int = 2,
            wasteFactor: Double = PAINT_WASTE_FACTOR,
            coverageSqFtPerGallon: Double = PAINT_COVERAGE_SF_PER_GALLON,
            perimeterLf: Double = 0.0
        ): PaintRequirement {
            val gross = max(0.0, surfaceAreaSqFt)
            val safeWindows = max(0, windows)
            val safeDoors = max(0, doors)
            val deductions = safeWindows * WINDOW_DEDUCTION_SF + safeDoors * DOOR_DEDUCTION_SF
            val net = max(0.0, gross - deductions)
            val safeCoats = max(1, coats)
            val safeWaste = max(0.0, wasteFactor)
            val safeCoverage = if (coverageSqFtPerGallon > 0.0) coverageSqFtPerGallon else PAINT_COVERAGE_SF_PER_GALLON
            val totalCoatSf = net * safeCoats
            val gallonsNoWaste = if (totalCoatSf > 0.0) ceilSnap(totalCoatSf / safeCoverage) else 0.0
            val gallonsWaste = if (totalCoatSf > 0.0) ceilSnap((totalCoatSf * (1.0 + safeWaste)) / safeCoverage) else 0.0
            val tapeRolls = if (perimeterLf > 0.0 && net > 0.0) max(1.0, ceilSnap(perimeterLf / 180.0)) else 0.0
            return PaintRequirement(
                grossSurfaceSqFt = gross,
                openingDeductionsSqFt = deductions,
                netPaintableSqFt = net,
                coats = safeCoats,
                wasteFactor = safeWaste,
                gallonsWithoutWaste = gallonsNoWaste,
                gallonsWithWaste = gallonsWaste,
                painterTapeRolls = tapeRolls
            )
        }

        fun calculatePaintForRoom(
            lengthFt: Double,
            widthFt: Double,
            heightFt: Double,
            includeWalls: Boolean = true,
            includeCeiling: Boolean = false,
            windows: Int = 0,
            doors: Int = 0,
            coats: Int = 2,
            wasteFactor: Double = PAINT_WASTE_FACTOR
        ): PaintRequirement {
            val dims = measureRoom(lengthFt, widthFt, heightFt)
            val surface = (if (includeWalls) dims.wallAreaSqFt else 0.0) +
                (if (includeCeiling) dims.ceilingAreaSqFt else 0.0)
            return calculatePaint(
                surfaceAreaSqFt = surface,
                windows = if (includeWalls) windows else 0,
                doors = if (includeWalls) doors else 0,
                coats = coats,
                wasteFactor = wasteFactor,
                perimeterLf = dims.linearFootageFt
            )
        }

        fun calculateDrywall(
            wallAreaSqFt: Double,
            ceilingAreaSqFt: Double = 0.0,
            sheetSizeSqFt: Double = 32.0,
            wasteFactor: Double = DRYWALL_WASTE_FACTOR
        ): DrywallRequirement {
            val total = max(0.0, wallAreaSqFt) + max(0.0, ceilingAreaSqFt)
            val sheet = if (sheetSizeSqFt == 48.0) 48.0 else 32.0
            val safeWaste = max(0.0, wasteFactor)
            val sheets = if (total > 0.0) ceilSnap((total * (1.0 + safeWaste)) / sheet) else 0.0
            val mud = if (total > 0.0) ceilSnap(total / 500.0) else 0.0
            val tape = sheets * 24.0
            return DrywallRequirement(
                totalSurfaceSqFt = total,
                sheetSizeSqFt = sheet,
                wasteFactor = safeWaste,
                sheetsNeeded = sheets,
                jointCompoundBuckets = mud,
                drywallTapeLf = tape
            )
        }

        fun calculateRoofing(
            roofAreaSqFt: Double,
            wasteFactor: Double = ROOFING_WASTE_FACTOR
        ): RoofingRequirement {
            val area = max(0.0, roofAreaSqFt)
            val safeWaste = max(0.0, wasteFactor)
            val netSq = roofingSquares(area)
            val grossSq = netSq * (1.0 + safeWaste)
            val bundles = if (grossSq > 0.0) ceilSnap(grossSq * 3.0) else 0.0
            return RoofingRequirement(
                roofAreaSqFt = area,
                netSquares = netSq,
                wasteFactor = safeWaste,
                squaresWithWaste = grossSq,
                shingleBundles = bundles
            )
        }

        fun calculateConcrete(
            lengthFt: Double,
            widthFt: Double,
            thicknessInches: Double,
            wasteFactor: Double = CONCRETE_WASTE_FACTOR
        ): ConcreteRequirement {
            val area = floorArea(lengthFt, widthFt)
            val tFt = max(0.0, thicknessInches) / 12.0
            val safeWaste = max(0.0, wasteFactor)
            val cy = (area * tFt / 27.0) * (1.0 + safeWaste)
            return ConcreteRequirement(
                cubicYardsWithWaste = cy,
                bags60Lb = ceilSnap(cy * 45.0),
                bags80Lb = ceilSnap(cy * 34.0),
                wasteFactor = safeWaste
            )
        }

        fun calculateCmu(
            wallLengthFt: Double,
            wallHeightFt: Double,
            wasteFactor: Double = CMU_WASTE_FACTOR
        ): CmuRequirement {
            val area = max(0.0, wallLengthFt) * max(0.0, wallHeightFt)
            val safeWaste = max(0.0, wasteFactor)
            return CmuRequirement(
                wallAreaSqFt = area,
                wasteFactor = safeWaste,
                blocksNeeded = if (area > 0.0) ceilSnap((area / 0.89) * (1.0 + safeWaste)) else 0.0
            )
        }

        fun calculateFraming(
            wallLengthLf: Double,
            wallHeightFt: Double,
            studWasteFactor: Double = FRAMING_STUD_WASTE_FACTOR
        ): FramingRequirement {
            val l = max(0.0, wallLengthLf)
            val h = max(0.0, wallHeightFt)
            val safeWaste = max(0.0, studWasteFactor)
            val studs = if (l > 0.0 && h > 0.0) ceilSnap((l / 1.33 + 1.0) * (1.0 + safeWaste)) else 0.0
            return FramingRequirement(
                wallLengthLf = l,
                wallSurfaceSqFt = l * h,
                studWasteFactor = safeWaste,
                studsNeeded = studs,
                platesLf = ceilSnap(l * 3.0)
            )
        }

        fun calculateRoomMaterials(
            lengthFt: Double,
            widthFt: Double,
            heightFt: Double,
            windows: Int = 0,
            doors: Int = 0,
            flooringPattern: FlooringPattern = FlooringPattern.STANDARD_INLINE,
            paintCoats: Int = 2,
            paintWasteFactor: Double = PAINT_WASTE_FACTOR
        ): RoomMaterialSummary {
            val m = measureRoom(lengthFt, widthFt, heightFt)
            val flooring = calculateFlooring(m.floorAreaSqFt, flooringPattern.wasteFactor)
            val tile = calculateTile(m.floorAreaSqFt)
            val paintWalls = calculatePaintForRoom(
                lengthFt = lengthFt,
                widthFt = widthFt,
                heightFt = heightFt,
                includeWalls = true,
                includeCeiling = false,
                windows = windows,
                doors = doors,
                coats = paintCoats,
                wasteFactor = paintWasteFactor
            )
            val paintAll = calculatePaintForRoom(
                lengthFt = lengthFt,
                widthFt = widthFt,
                heightFt = heightFt,
                includeWalls = true,
                includeCeiling = true,
                windows = windows,
                doors = doors,
                coats = paintCoats,
                wasteFactor = paintWasteFactor
            )
            val drywall = calculateDrywall(m.wallAreaSqFt, m.ceilingAreaSqFt)
            val roofing = calculateRoofing(m.floorAreaSqFt)
            return RoomMaterialSummary(
                measurements = m,
                flooring = flooring,
                tile = tile,
                paintWallsOnly = paintWalls,
                paintWallsAndCeiling = paintAll,
                drywall = drywall,
                roofing = roofing
            )
        }

        fun ceilSnap(value: Double): Double = ceil(value - 1e-10)

        fun round2(value: Double): Double = round(value * 100.0) / 100.0
    }
}
