package com.example.domain

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

    fun computeDimensions(room: EstimateRoom): RoomDimensions {
        val floor = max(0.0, room.lengthFt) * max(0.0, room.widthFt)
        val perim = 2.0 * (max(0.0, room.lengthFt) + max(0.0, room.widthFt))
        val wall = perim * max(0.0, room.heightFt)
        return RoomDimensions(floor = floor, wall = wall, ceiling = floor, perim = perim)
    }

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
            MeasurementBasis.FLOOR_SQUARES -> dims.floor / 100.0
            MeasurementBasis.VOLUME_CF -> dims.floor * room.heightFt
            MeasurementBasis.VOLUME_CY -> (dims.floor * room.heightFt) / 27.0
            MeasurementBasis.DOOR_COUNT -> max(0, room.doors).toDouble()
            MeasurementBasis.WINDOW_COUNT -> max(0, room.windows).toDouble()
            MeasurementBasis.ONE_PER_ROOM -> 1.0
            MeasurementBasis.AUTO -> when (unit.uppercase(Locale.US)) {
                "SF" -> when {
                    room.side == EstimateSide.EXTERIOR && (service == "Decks" || service == "Decks & Porches") -> dims.floor
                    room.side == EstimateSide.EXTERIOR -> dims.wall
                    else -> dims.wall + dims.ceiling
                }
                "LF" -> if (service == "Fencing" && room.side == EstimateSide.EXTERIOR) room.fenceLF else dims.perim
                "SQ" -> dims.floor / 100.0
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
            val basis = chosen.effectiveBasis()
            val rate = if (current.rateInitialized) current.rate else chosen.defaultEstimateRate()
            val qty = if (current.quantityManual) {
                max(0.0, current.quantity)
            } else {
                defaultQty(chosen.unit, d, room, service, basis)
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
        taxPercent: Double
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
                val basis = chosen.effectiveBasis()
                val autoQty = defaultQty(chosen.unit, d, room, service, basis)
                val qty = max(0.0, if (sel.quantityManual) sel.quantity else autoQty)
                val rate = max(0.0, if (sel.rateInitialized) sel.rate else chosen.defaultEstimateRate())
                val lineTotal = qty * rate
                serviceTotal += lineTotal

                val effectiveLaborRate = if (chosen.laborRate > 0.0) chosen.laborRate else 85.0
                val internalPerUnit = if (chosen.unit.equals("HR", ignoreCase = true)) {
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
            }
        }

        val mats = linkedMapOf<String, Double>()
        val boardArea = raw["Drywall board area SF"] ?: 0.0
        val finishArea = raw["Drywall finishing area SF"] ?: 0.0
        if (boardArea > 0.0) {
            mats["1/2-inch drywall sheets (4x8, incl. 10% waste)"] = ceilSnap(boardArea * 1.10 / 32.0)
        }
        if (finishArea > 0.0) {
            mats["Joint compound (4.5 gal)"] = ceilSnap(finishArea / 500.0)
            val sheetSource = if (boardArea > 0.0) boardArea else finishArea
            val drywallSheets = ceilSnap(sheetSource * 1.10 / 32.0)
            mats["Drywall tape (LF)"] = drywallSheets * 24.0
        }
        val paintCoverage = raw["Paint coverage area SF"] ?: 0.0
        if (paintCoverage > 0.0) {
            mats["Paint / primer gallons (350 SF per gallon)"] = ceilSnap(paintCoverage / 350.0)
        }
        val tapeLf = raw["Painter tape LF"] ?: 0.0
        if (tapeLf > 0.0) {
            mats["Painter tape rolls (180 LF per roll)"] = max(1.0, ceilSnap(tapeLf / 180.0))
        }
        val flooringArea = raw["Flooring area SF"] ?: 0.0
        if (flooringArea > 0.0) {
            mats["Flooring (SF incl. 10% waste)"] = ceilSnap(flooringArea * 1.10)
            mats["Underlayment (SF)"] = ceilSnap(flooringArea)
        }
        val tileFloor = raw["Tile floor area SF"] ?: 0.0
        if (tileFloor > 0.0) {
            mats["Tile (SF incl. 15% waste)"] = ceilSnap(tileFloor * 1.15)
            mats["Thinset (bags, approx. 50 SF each)"] = ceilSnap(tileFloor / 50.0)
            mats["Grout (bags, approx. 100 SF each)"] = ceilSnap(tileFloor / 100.0)
        }
        val framingLf = raw["Framing perimeter LF"] ?: 0.0
        if (framingLf > 0.0) {
            mats["2x4 studs (planning allowance)"] = ceilSnap(framingLf / 1.33)
            mats["Framing plates (LF)"] = ceilSnap(framingLf * 3.0)
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
                val tFt = max(0.0, p3) / 12.0
                if (l <= 0.0 || w <= 0.0 || tFt <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter length, width, and thickness greater than zero."))
                } else {
                    val cy = (l * w * tFt / 27.0) * 1.10
                    Result.success(
                        listOf(
                            CalcResultRow("concrete::vol", "Concrete volume incl. 10% waste", cy, "CY", formatQty(cy)),
                            CalcResultRow("concrete::60", "Concrete bags — 60 lb", ceilSnap(cy * 45.0), "bags", formatQty(ceilSnap(cy * 45.0))),
                            CalcResultRow("concrete::80", "Concrete bags — 80 lb", ceilSnap(cy * 34.0), "bags", formatQty(ceilSnap(cy * 34.0)))
                        )
                    )
                }
            }
            MaterialCalcKind.CMU -> {
                val area = max(0.0, p1) * max(0.0, p2)
                if (area <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter wall length and height greater than zero."))
                } else {
                    val blocks = ceilSnap(area / 0.89 * 1.05)
                    Result.success(
                        listOf(
                            CalcResultRow("cmu::blocks", "8 × 8 × 16 CMU blocks incl. 5% allowance", blocks, "blocks", formatQty(blocks))
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
                    val studs = ceilSnap((l / 1.33 + 1.0) * 1.15)
                    val plates = ceilSnap(l * 3.0)
                    val sf = l * h
                    Result.success(
                        listOf(
                            CalcResultRow("framing::studs", "2×4 studs — 16 in. O.C. incl. 15% allowance", studs, "studs", formatQty(studs)),
                            CalcResultRow("framing::plates", "2×4 plates — three runs", plates, "LF", formatQty(plates)),
                            CalcResultRow("framing::sf", "Wall framing surface", sf, "SF", formatQty(sf))
                        )
                    )
                }
            }
            MaterialCalcKind.DRYWALL -> {
                val area = max(0.0, p1) + max(0.0, p2)
                val sheetArea = if (p3 == 48.0) 48.0 else 32.0
                if (area <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter wall and/or ceiling area greater than zero."))
                } else {
                    val sheets = ceilSnap(area * 1.10 / sheetArea)
                    val label = if (sheetArea == 48.0) "4×12" else "4×8"
                    Result.success(
                        listOf(
                            CalcResultRow("drywall::sheets", "Drywall sheets ($label) incl. 10% allowance", sheets, "sheets", formatQty(sheets)),
                            CalcResultRow("drywall::area", "Drywall surface area", area, "SF", formatQty(area))
                        )
                    )
                }
            }
            MaterialCalcKind.PAINT -> {
                val gross = max(0.0, p1)
                val windows = max(0.0, p2)
                val doors = max(0.0, p3)
                val coats = max(1, p4.toInt())
                if (gross <= 0.0) {
                    Result.failure(IllegalArgumentException("Enter paint surface area greater than zero."))
                } else {
                    val net = max(0.0, gross - windows * 15.0 - doors * 20.0)
                    val gallons = ceilSnap((net * coats) / 350.0)
                    Result.success(
                        listOf(
                            CalcResultRow("paint::net", "Net paintable area after opening deductions", net, "SF", formatQty(net)),
                            CalcResultRow("paint::gal", "Paint / primer for $coats coat(s) at 350 SF per gallon", gallons, "gallons", formatQty(gallons))
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
                    val qty = ceilSnap(area * factor)
                    val wastePct = (factor - 1.0) * 100.0
                    Result.success(
                        listOf(
                            CalcResultRow("flooring::qty", "Flooring quantity incl. selected pattern allowance", qty, "SF", formatQty(qty)),
                            CalcResultRow("flooring::net", "Net measured floor area", area, "SF", formatQty(area)),
                            CalcResultRow("flooring::waste", "Waste allowance", wastePct, "%", formatQty(wastePct))
                        )
                    )
                }
            }
        }
    }

    fun ceilSnap(value: Double): Double = ceil(value - 1e-10)

    fun round2(value: Double): Double = round(value * 100.0) / 100.0

    fun formatMoney(amount: Double): String {
        val nf = NumberFormat.getCurrencyInstance(Locale.US)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        return nf.format(if (amount.isNaN() || amount.isInfinite()) 0.0 else amount)
    }

    fun formatQty(qty: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.US)
        nf.maximumFractionDigits = 2
        return nf.format(if (qty.isNaN() || qty.isInfinite()) 0.0 else qty)
    }
}
