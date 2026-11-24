package com.example.domain

import com.example.model.AppSettings
import com.example.model.CalcResultRow
import com.example.model.EstimateRoom
import com.example.model.EstimateSide
import com.example.model.MaterialCalcKind
import com.example.model.MeasurementBasis
import com.example.model.PriceBookItem
import com.example.model.RoomDimensions
import com.example.model.RoomServiceSelection
import com.example.model.TakeoffResult
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.round

object EstimatorEngine {

    private val exteriorPatterns = listOf(
        Regex("\\bexterior\\b", RegexOption.IGNORE_CASE),
        Regex("\\bdeck\\b", RegexOption.IGNORE_CASE),
        Regex("\\bporch\\b", RegexOption.IGNORE_CASE),
        Regex("\\bfence\\b", RegexOption.IGNORE_CASE),
        Regex("\\bsiding\\b", RegexOption.IGNORE_CASE),
        Regex("\\broof\\b", RegexOption.IGNORE_CASE),
        Regex("\\brafter\\b", RegexOption.IGNORE_CASE),
        Regex("\\btruss\\b", RegexOption.IGNORE_CASE),
        Regex("\\bsoffit\\b", RegexOption.IGNORE_CASE),
        Regex("\\bfascia\\b", RegexOption.IGNORE_CASE),
        Regex("\\bgutter\\b", RegexOption.IGNORE_CASE),
        Regex("\\bdriveway\\b", RegexOption.IGNORE_CASE),
        Regex("\\blandscape\\b", RegexOption.IGNORE_CASE),
        Regex("\\bpatio\\b", RegexOption.IGNORE_CASE),
        Regex("\\bstorm door\\b", RegexOption.IGNORE_CASE),
        Regex("\\bentry door\\b", RegexOption.IGNORE_CASE),
        Regex("\\bsliding patio\\b", RegexOption.IGNORE_CASE),
        Regex("\\bwindow flashing\\b", RegexOption.IGNORE_CASE),
        Regex("\\bweatherstripping\\b", RegexOption.IGNORE_CASE),
        Regex("\\bgarage exterior\\b", RegexOption.IGNORE_CASE)
    )

    private val interiorPatterns = listOf(
        Regex("\\binterior\\b", RegexOption.IGNORE_CASE),
        Regex("\\bkitchen\\b", RegexOption.IGNORE_CASE),
        Regex("\\bbathroom\\b", RegexOption.IGNORE_CASE),
        Regex("\\bdrywall\\b", RegexOption.IGNORE_CASE),
        Regex("\\bplaster\\b", RegexOption.IGNORE_CASE),
        Regex("\\bsubfloor\\b", RegexOption.IGNORE_CASE),
        Regex("\\bfloor\\b", RegexOption.IGNORE_CASE),
        Regex("\\bceiling\\b", RegexOption.IGNORE_CASE),
        Regex("\\bwall tile\\b", RegexOption.IGNORE_CASE),
        Regex("\\bvanity\\b", RegexOption.IGNORE_CASE),
        Regex("\\bcabinet\\b", RegexOption.IGNORE_CASE),
        Regex("\\bbaseboard\\b", RegexOption.IGNORE_CASE),
        Regex("\\bcasing\\b", RegexOption.IGNORE_CASE),
        Regex("\\bcrown\\b", RegexOption.IGNORE_CASE),
        Regex("\\btrim\\b", RegexOption.IGNORE_CASE),
        Regex("\\bcloset\\b", RegexOption.IGNORE_CASE),
        Regex("\\bstair\\b", RegexOption.IGNORE_CASE),
        Regex("\\bshower\\b", RegexOption.IGNORE_CASE),
        Regex("\\btub\\b", RegexOption.IGNORE_CASE),
        Regex("\\btoilet\\b", RegexOption.IGNORE_CASE),
        Regex("\\bsink\\b", RegexOption.IGNORE_CASE),
        Regex("\\bfaucet\\b", RegexOption.IGNORE_CASE),
        Regex("\\binterior door\\b", RegexOption.IGNORE_CASE),
        Regex("\\bpartition\\b", RegexOption.IGNORE_CASE),
        Regex("\\bjoist\\b", RegexOption.IGNORE_CASE),
        Regex("\\bblocking\\b", RegexOption.IGNORE_CASE),
        Regex("\\bfire blocking\\b", RegexOption.IGNORE_CASE),
        Regex("\\bfinish carpentry\\b", RegexOption.IGNORE_CASE)
    )

    private val windowsDoorsFilter = Regex(
        "Exterior|Storm Door|Sliding Patio|Window Replacement|Picture Window|Basement Window|Egress Window|Window Flashing|Weatherstripping|Door & Window Repairs|Custom Door/Window",
        RegexOption.IGNORE_CASE
    )

    private val exteriorPaintingFilter = Regex(
        "Exterior|Deck Staining|Fence Staining",
        RegexOption.IGNORE_CASE
    )

    private val serviceAliases = mapOf(
        "Trim & Millwork" to listOf("Trim", "Millwork"),
        "Windows & Doors" to listOf("Doors & Windows"),
        "Decks" to listOf("Decks & Porches"),
        "Exterior Painting" to listOf("Painting")
    )

    fun inferMarketSection(item: String, category: String): String {
        val text = "$item $category"
        if (exteriorPatterns.any { it.containsMatchIn(text) }) return "Exterior"
        if (interiorPatterns.any { it.containsMatchIn(text) }) return "Interior"
        if (category.startsWith("011") || category.startsWith("012")) return "Exterior"
        return "Interior"
    }

    fun computeDimensions(room: EstimateRoom): RoomDimensions =
        ConstructionMaterialCalculator.measureRoom(room.lengthFt, room.widthFt, room.heightFt)
            .toRoomDimensions()

    fun pbItemsForService(service: String, priceBook: List<PriceBookItem>): List<PriceBookItem> {
        val terms = (serviceAliases[service] ?: listOf(service)).map { it.lowercase(Locale.US) }
        var found = priceBook.filter { entry ->
            val cleanCat = entry.category.replace(Regex("^\\d+\\s*"), "").lowercase(Locale.US)
            terms.any { t -> cleanCat == t || cleanCat.contains(t) }
        }
        if (service == "Windows & Doors") {
            found = found.filter { windowsDoorsFilter.containsMatchIn(it.item) }
        }
        if (service == "Exterior Painting") {
            found = found.filter { exteriorPaintingFilter.containsMatchIn(it.item) }
        }
        return found
    }

    fun effectiveBasisForSelection(
        sel: RoomServiceSelection,
        chosen: PriceBookItem,
        effectiveUnit: String
    ): MeasurementBasis {
        val valid = MeasurementBasis.optionsForUnit(effectiveUnit)
        val override = sel.overrideMeasurementBasis.trim()
        if (override.isNotEmpty()) {
            val parsed = MeasurementBasis.fromKey(override)
            if (valid.contains(parsed)) return parsed
        }
        return if (effectiveUnit.equals(chosen.unit, ignoreCase = true)) {
            chosen.effectiveBasis()
        } else {
            MeasurementBasis.AUTO
        }
    }

    fun defaultRateForUnit(chosen: PriceBookItem, unit: String): Double {
        val upper = unit.trim().uppercase(Locale.US)
        return when {
            upper == "HR" -> if (chosen.hourlyRate > 0.0) chosen.hourlyRate else 85.0
            upper.equals(chosen.unit, ignoreCase = true) -> chosen.benchmarkUnitPrice
            chosen.unit.equals("SF", ignoreCase = true) && upper == "SQ" ->
                round2(chosen.benchmarkUnitPrice * ConstructionMaterialCalculator.ROOFING_SQUARE_SQFT)
            chosen.unit.equals("SQ", ignoreCase = true) && upper == "SF" ->
                round2(chosen.benchmarkUnitPrice / ConstructionMaterialCalculator.ROOFING_SQUARE_SQFT)
            else -> chosen.benchmarkUnitPrice
        }
    }

    fun defaultQty(
        unit: String,
        dims: RoomDimensions,
        room: EstimateRoom,
        service: String,
        basis: MeasurementBasis
    ): Double {
        val raw = when (basis) {
            MeasurementBasis.MANUAL -> 0.0
            MeasurementBasis.FLOOR_AREA -> dims.floor
            MeasurementBasis.WALL_AREA -> dims.wall
            MeasurementBasis.CEILING_AREA -> dims.ceiling
            MeasurementBasis.WALLS_CEILING_AREA -> dims.wall + dims.ceiling
            MeasurementBasis.PERIMETER -> dims.perim
            MeasurementBasis.FENCE_LENGTH -> room.fenceLF
            MeasurementBasis.FLOOR_SQUARES -> ConstructionMaterialCalculator.roofingSquares(dims.floor)
            MeasurementBasis.VOLUME_CF -> dims.floor * room.heightFt
            MeasurementBasis.VOLUME_CY -> (dims.floor * room.heightFt) / 27.0
            MeasurementBasis.DOOR_COUNT -> max(0, room.doors).toDouble()
            MeasurementBasis.WINDOW_COUNT -> max(0, room.windows).toDouble()
            MeasurementBasis.ONE_PER_ROOM -> 1.0
            MeasurementBasis.AUTO -> when (unit.uppercase(Locale.US)) {
                "SF" -> when {
                    room.side == EstimateSide.EXTERIOR && (service == "Decks" || service == "Decks & Porches") -> dims.floor
                    room.side == EstimateSide.EXTERIOR -> dims.wall
                    service == "Flooring" || service == "Roofing" -> dims.floor
                    else -> dims.wall + dims.ceiling
                }
                "LF" -> if (service == "Fencing" && room.side == EstimateSide.EXTERIOR) room.fenceLF else dims.perim
                "SQ" -> ConstructionMaterialCalculator.roofingSquares(dims.floor)
                "CF" -> dims.floor * room.heightFt
                "CY" -> (dims.floor * room.heightFt) / 27.0
                "HR", "ROOM" -> 1.0
                else -> 1.0
            }
        }
        return round2(max(0.0, raw))
    }

    fun syncRoomServiceSelections(
        room: EstimateRoom,
        priceBook: List<PriceBookItem>
    ): EstimateRoom {
        val d = computeDimensions(room)
        val updated = room.serviceSelections.toMutableMap()
        for (service in room.services) {
            val items = pbItemsForService(service, priceBook)
            if (items.isEmpty()) {
                if (!updated.containsKey(service)) {
                    updated[service] = RoomServiceSelection(serviceName = service)
                }
                continue
            }
            val current = updated[service] ?: RoomServiceSelection(serviceName = service)
            val chosen = items.firstOrNull { it.item == current.selectedPbItemName } ?: items.first()
            val effectiveUnit = current.effectiveUnit(chosen.unit)
            val basis = effectiveBasisForSelection(current, chosen, effectiveUnit)
            val rate = if (current.rateInitialized) current.rate else defaultRateForUnit(chosen, effectiveUnit)
            val qty = if (current.quantityManual) {
                max(0.0, current.quantity)
            } else {
                defaultQty(effectiveUnit, d, room, service, basis)
            }
            updated[service] = current.copy(
                selectedPbItemName = chosen.item,
                rate = rate,
                rateInitialized = true,
                quantity = qty
            )
        }
        return room.copy(serviceSelections = updated)
    }

    fun computeTakeoff(
        rooms: List<EstimateRoom>,
        priceBook: List<PriceBookItem>,
        manualMaterials: Map<String, Double>,
        discount: Double,
        taxPercent: Double,
        settings: AppSettings = AppSettings()
    ): TakeoffResult {
        val raw = linkedMapOf<String, Double>()
        var serviceTotal = 0.0
        var totalCost = 0.0
        val breakdown = linkedMapOf<String, Double>()

        fun addRaw(name: String, qty: Double) {
            if (qty > 0.0) {
                raw[name] = (raw[name] ?: 0.0) + qty
            }
        }

        for (room in rooms) {
            val d = computeDimensions(room)
            for (service in room.services) {
                val items = pbItemsForService(service, priceBook)
                if (items.isEmpty()) continue
                val sel = room.serviceSelections[service] ?: RoomServiceSelection(serviceName = service)
                val chosen = items.firstOrNull { it.item == sel.selectedPbItemName } ?: items.first()
                val effectiveUnit = sel.effectiveUnit(chosen.unit)
                val basis = effectiveBasisForSelection(sel, chosen, effectiveUnit)
                val autoQty = defaultQty(effectiveUnit, d, room, service, basis)
                val qty = max(0.0, if (sel.quantityManual) sel.quantity else autoQty)
                val rate = max(0.0, if (sel.rateInitialized) sel.rate else defaultRateForUnit(chosen, effectiveUnit))
                val lineTotal = qty * rate
                serviceTotal += lineTotal

                val effectiveLaborRate = if (chosen.laborRate > 0.0) chosen.laborRate else settings.defaultHourlyRate
                val internalPerUnit = if (effectiveUnit.equals("HR", ignoreCase = true)) {
                    effectiveLaborRate
                } else {
                    effectiveLaborRate * chosen.benchmarkLaborHoursPerUnit
                }
                totalCost += qty * internalPerUnit
                breakdown[service] = (breakdown[service] ?: 0.0) + lineTotal

                val subs = sel.selectedSubcategories

                if (service == "Drywall") {
                    val finishOnly = subs.isNotEmpty() && subs.all {
                        it == "Finish existing drywall" || it == "Patch and repair"
                    }
                    if (!finishOnly) addRaw("Drywall board area SF", d.wall + d.ceiling)
                    if (!subs.contains("Hang only")) addRaw("Drywall finishing area SF", d.wall + d.ceiling)
                }

                if (service == "Painting") {
                    val paintSubs = subs.ifEmpty { listOf("Walls only") }
                    val wallArea = if (
                        paintSubs.contains("Walls only") ||
                        paintSubs.contains("Walls and ceiling") ||
                        paintSubs.contains("Walls, ceiling and trim")
                    ) d.wall else 0.0
                    val ceilArea = if (
                        paintSubs.contains("Walls and ceiling") ||
                        paintSubs.contains("Walls, ceiling and trim") ||
                        paintSubs.contains("Ceiling only")
                    ) d.ceiling else 0.0
                    val area = wallArea + ceilArea
                    if (area > 0.0) {
                        val coatsText = sel.questionAnswers["Painting_2"]?.firstOrNull()
                        val coats = coatsText?.let { Regex("\\d+").find(it)?.value?.toIntOrNull() } ?: 2
                        addRaw("Paint coverage area SF", area * coats)
                        addRaw("Painter tape LF", d.perim)
                    }
                }

                if (service == "Flooring" || (service == "Tile" && subs.contains("Floor tile"))) {
                    addRaw(if (service == "Tile") "Tile floor area SF" else "Flooring area SF", d.floor)
                }

                if (service == "Framing") {
                    addRaw("Framing perimeter LF", d.perim)
                }

                if (service == "Roofing") {
                    addRaw("Roofing area SF", d.floor)
                }
            }
        }

        val drywallWaste = settings.defaultDrywallWastePct.coerceIn(0, 50) / 100.0
        val flooringWaste = settings.defaultFlooringWastePct.coerceIn(0, 50) / 100.0
        val tileWaste = settings.defaultTileWastePct.coerceIn(0, 50) / 100.0
        val roofingWaste = settings.defaultRoofingWastePct.coerceIn(0, 50) / 100.0
        val paintWaste = settings.defaultPaintWastePct.coerceIn(0, 50) / 100.0
        val paintCov = settings.paintCoverageSqFtPerGal.takeIf { it > 0.0 } ?: 350.0

        val mats = linkedMapOf<String, Double>()
        val boardArea = raw["Drywall board area SF"] ?: 0.0
        val finishArea = raw["Drywall finishing area SF"] ?: 0.0
        if (boardArea > 0.0) {
            val drywall = ConstructionMaterialCalculator.calculateDrywall(boardArea, wasteFactor = drywallWaste)
            mats["1/2-inch drywall sheets (4x8, incl. ${settings.defaultDrywallWastePct}% waste)"] = drywall.sheetsNeeded
        }
        if (finishArea > 0.0) {
            val finishCalc = ConstructionMaterialCalculator.calculateDrywall(finishArea, wasteFactor = drywallWaste)
            val sheetSource = if (boardArea > 0.0) boardArea else finishArea
            val drywallSheets = ConstructionMaterialCalculator.calculateDrywall(sheetSource, wasteFactor = drywallWaste).sheetsNeeded
            mats["Joint compound (4.5 gal)"] = finishCalc.jointCompoundBuckets
            mats["Drywall tape (LF)"] = drywallSheets * 24.0
        }
        val paintCoverage = raw["Paint coverage area SF"] ?: 0.0
        if (paintCoverage > 0.0) {
            val paintReq = ConstructionMaterialCalculator.calculatePaint(
                surfaceAreaSqFt = paintCoverage,
                coats = 1,
                wasteFactor = paintWaste,
                coverageSqFtPerGallon = paintCov
            )
            mats["Paint / primer gallons (${paintCov.toInt()} SF per gallon)"] = paintReq.gallonsWithoutWaste
        }
        val tapeLf = raw["Painter tape LF"] ?: 0.0
        if (tapeLf > 0.0) {
            mats["Painter tape rolls (180 LF per roll)"] = max(1.0, ceilSnap(tapeLf / 180.0))
        }
        val flooringArea = raw["Flooring area SF"] ?: 0.0
        if (flooringArea > 0.0) {
            val floorReq = ConstructionMaterialCalculator.calculateFlooring(flooringArea, wasteFactor = flooringWaste)
            mats["Flooring (SF incl. ${settings.defaultFlooringWastePct}% waste)"] = floorReq.recommendedOrderSqFt
            mats["Underlayment (SF)"] = floorReq.underlaymentSqFt
        }
        val tileFloor = raw["Tile floor area SF"] ?: 0.0
        if (tileFloor > 0.0) {
            val tileReq = ConstructionMaterialCalculator.calculateTile(tileFloor, wasteFactor = tileWaste)
            mats["Tile (SF incl. ${settings.defaultTileWastePct}% waste)"] = tileReq.recommendedOrderSqFt
            mats["Thinset (bags, approx. 50 SF each)"] = tileReq.thinsetBags
            mats["Grout (bags, approx. 100 SF each)"] = tileReq.groutBags
        }
        val framingLf = raw["Framing perimeter LF"] ?: 0.0
        if (framingLf > 0.0) {
            val framingReq = ConstructionMaterialCalculator.calculateFraming(framingLf, 8.0)
            mats["2x4 studs (planning allowance)"] = ceilSnap(framingLf / 1.33)
            mats["Framing plates (LF)"] = framingReq.platesLf
        }
        val roofArea = raw["Roofing area SF"] ?: 0.0
        if (roofArea > 0.0) {
            val roofReq = ConstructionMaterialCalculator.calculateRoofing(roofArea, wasteFactor = roofingWaste)
            mats["Roofing squares (incl. ${settings.defaultRoofingWastePct}% waste)"] = round2(roofReq.squaresWithWaste)
            mats["Shingle bundles (3 per square)"] = roofReq.shingleBundles
        }

        manualMaterials.forEach { (name, qty) ->
            if (qty > 0.0) {
                mats[name] = (mats[name] ?: 0.0) + qty
            }
        }

        val safeDiscount = max(0.0, discount)
        val safeTax = max(0.0, taxPercent)
        val grandTotal = max(0.0, serviceTotal - safeDiscount) * (1.0 + safeTax / 100.0)
        val grossProfit = grandTotal - totalCost

        return TakeoffResult(
            materials = mats,
            serviceTotal = serviceTotal,
            jobCost = totalCost,
            grandTotal = grandTotal,
            grossProfit = grossProfit,
            serviceBreakdown = breakdown
        )
    }

    fun calculateMaterialTool(
        kind: MaterialCalcKind,
        p1: Double = 0.0,
        p2: Double = 0.0,
        p3: Double = 0.0,
        p4: Double = 0.0
    ): Result<List<CalcResultRow>> {
        return when (kind) {
            MaterialCalcKind.CONCRETE -> {
                val l = max(0.0, p1)
                val w = max(0.0, p2)
                val t = max(0.0, p3)
                if (l <= 0.0 || w <= 0.0 || t <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter length, width, and thickness greater than zero."))
                } else {
                    val req = ConstructionMaterialCalculator.calculateConcrete(l, w, t)
                    Result.success(
                        listOf(
                            CalcResultRow("concrete::vol", "Concrete volume incl. 10% waste", req.cubicYardsWithWaste, "CY", formatQty(req.cubicYardsWithWaste)),
                            CalcResultRow("concrete::60", "Concrete bags — 60 lb", req.bags60Lb, "bags", formatQty(req.bags60Lb)),
                            CalcResultRow("concrete::80", "Concrete bags — 80 lb", req.bags80Lb, "bags", formatQty(req.bags80Lb))
                        )
                    )
                }
            }
            MaterialCalcKind.CMU -> {
                val l = max(0.0, p1)
                val h = max(0.0, p2)
                if (l <= 0.0 || h <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter wall length and height greater than zero."))
                } else {
                    val req = ConstructionMaterialCalculator.calculateCmu(l, h)
                    Result.success(
                        listOf(
                            CalcResultRow("cmu::blocks", "8 × 8 × 16 CMU blocks incl. 5% allowance", req.blocksNeeded, "blocks", formatQty(req.blocksNeeded))
                        )
                    )
                }
            }
            MaterialCalcKind.FRAMING -> {
                val l = max(0.0, p1)
                val h = max(0.0, p2)
                if (l <= 0.0 || h <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter wall length and height greater than zero."))
                } else {
                    val req = ConstructionMaterialCalculator.calculateFraming(l, h)
                    Result.success(
                        listOf(
                            CalcResultRow("framing::studs", "2×4 studs — 16 in. O.C. incl. 15% allowance", req.studsNeeded, "studs", formatQty(req.studsNeeded)),
                            CalcResultRow("framing::plates", "2×4 plates — three runs", req.platesLf, "LF", formatQty(req.platesLf)),
                            CalcResultRow("framing::sf", "Wall framing surface", req.wallSurfaceSqFt, "SF", formatQty(req.wallSurfaceSqFt))
                        )
                    )
                }
            }
            MaterialCalcKind.DRYWALL -> {
                val w = max(0.0, p1)
                val c = max(0.0, p2)
                val sheetArea = if (p3 == 48.0) 48.0 else 32.0
                if (w + c <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter wall and/or ceiling area greater than zero."))
                } else {
                    val req = ConstructionMaterialCalculator.calculateDrywall(w, c, sheetArea)
                    val label = if (sheetArea == 48.0) "4×12" else "4×8"
                    Result.success(
                        listOf(
                            CalcResultRow("drywall::sheets", "Drywall sheets ($label) incl. 10% allowance", req.sheetsNeeded, "sheets", formatQty(req.sheetsNeeded)),
                            CalcResultRow("drywall::area", "Drywall surface area", req.totalSurfaceSqFt, "SF", formatQty(req.totalSurfaceSqFt))
                        )
                    )
                }
            }
            MaterialCalcKind.PAINT -> {
                val gross = max(0.0, p1)
                val windows = max(0, p2.toInt())
                val doors = max(0, p3.toInt())
                val coats = max(1, p4.toInt())
                if (gross <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter paint surface area greater than zero."))
                } else {
                    val req = ConstructionMaterialCalculator.calculatePaint(
                        surfaceAreaSqFt = gross,
                        windows = windows,
                        doors = doors,
                        coats = coats
                    )
                    Result.success(
                        listOf(
                            CalcResultRow("paint::net", "Net paintable area after opening deductions", req.netPaintableSqFt, "SF", formatQty(req.netPaintableSqFt)),
                            CalcResultRow("paint::gal", "Paint / primer for $coats coat(s) at 350 SF per gallon", req.gallonsWithoutWaste, "gallons", formatQty(req.gallonsWithoutWaste)),
                            CalcResultRow("paint::gal_waste", "Paint w/ standard 10% touch-up/cut-in waste", req.gallonsWithWaste, "gallons", formatQty(req.gallonsWithWaste))
                        )
                    )
                }
            }
            MaterialCalcKind.FLOORING -> {
                val area = max(0.0, p1)
                val factor = if (p2 > 0.0) p2 else 1.10
                if (area <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter floor area greater than zero."))
                } else {
                    val wasteFactor = max(0.0, factor - 1.0)
                    val req = ConstructionMaterialCalculator.calculateFlooring(area, wasteFactor)
                    val wastePct = wasteFactor * 100.0
                    Result.success(
                        listOf(
                            CalcResultRow("flooring::qty", "Flooring quantity incl. selected pattern allowance", req.recommendedOrderSqFt, "SF", formatQty(req.recommendedOrderSqFt)),
                            CalcResultRow("flooring::net", "Net measured floor area", req.netAreaSqFt, "SF", formatQty(req.netAreaSqFt)),
                            CalcResultRow("flooring::waste", "Waste allowance", wastePct, "%", formatQty(wastePct))
                        )
                    )
                }
            }
            MaterialCalcKind.ROOFING -> {
                val l = max(0.0, p1)
                val w = max(0.0, p2)
                val pitch = if (p3 > 0.0) p3 else 1.0
                if (l <= 0.0 || w <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter length and width greater than zero."))
                } else {
                    val area = ConstructionMaterialCalculator.floorArea(l, w) * pitch
                    val req = ConstructionMaterialCalculator.calculateRoofing(area)
                    val perim = ConstructionMaterialCalculator.linearFootage(l, w)
                    Result.success(
                        listOf(
                            CalcResultRow("roofing::area", "Roof / footprint area (L × W × pitch)", req.roofAreaSqFt, "SF", formatQty(req.roofAreaSqFt)),
                            CalcResultRow("roofing::net_sq", "Roofing squares (100 sq ft / square)", req.netSquares, "SQ", formatQty(req.netSquares)),
                            CalcResultRow("roofing::waste_sq", "Roofing squares incl. 10% waste", req.squaresWithWaste, "SQ", formatQty(req.squaresWithWaste)),
                            CalcResultRow("roofing::bundles", "Shingle bundles (3 per square)", req.shingleBundles, "bundles", formatQty(req.shingleBundles)),
                            CalcResultRow("roofing::perim", "Linear perimeter ((L + W) × 2)", perim, "LF", formatQty(perim))
                        )
                    )
                }
            }
        }
    }

    fun ceilSnap(value: Double): Double = ConstructionMaterialCalculator.ceilSnap(value)

    fun round2(value: Double): Double = ConstructionMaterialCalculator.round2(value)

    fun formatMoney(amount: Double, currencySymbol: String = "$"): String {
        val clean = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
        val nf = NumberFormat.getCurrencyInstance(Locale.US)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        val formatted = nf.format(clean)
        return if (currencySymbol == "$" || currencySymbol.isBlank()) {
            formatted
        } else {
            formatted.replace("$", currencySymbol)
        }
    }

    fun formatQty(qty: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.US)
        nf.maximumFractionDigits = 2
        return nf.format(if (qty.isNaN() || qty.isInfinite()) 0.0 else qty)
    }
}
