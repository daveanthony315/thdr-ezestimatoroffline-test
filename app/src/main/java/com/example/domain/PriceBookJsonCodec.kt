package com.example.domain

import com.example.model.CategoryMeta
import com.example.model.EstimateDraft
import com.example.model.EstimateRoom
import com.example.model.EstimateSide
import com.example.model.PriceBookItem
import com.example.model.RoomServiceSelection
import com.example.model.TimeframeBenchmark
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class PriceBookBackupPayload(
    val categories: List<CategoryMeta>,
    val items: List<PriceBookItem>,
    val timeframes: List<TimeframeBenchmark>
)

object PriceBookJsonCodec {

    fun parsePriceBookItems(jsonString: String): List<PriceBookItem> {
        val array = JSONArray(jsonString)
        val result = ArrayList<PriceBookItem>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            result.add(parsePriceBookItem(obj))
        }
        return result
    }

    fun parsePriceBookItem(obj: JSONObject): PriceBookItem {
        val category = obj.optString("category", "")
        val item = obj.optString("item", "")
        val marketSectionRaw = obj.optString("market_section", "")
        val marketSection = if (marketSectionRaw == "Interior" || marketSectionRaw == "Exterior") {
            marketSectionRaw
        } else {
            EstimatorEngine.inferMarketSection(item, category)
        }
        val hourlyRate = obj.optDouble("hourly_rate", Double.NaN)
        val estHours = obj.optDouble("estimated_completion_hours", Double.NaN)
        return PriceBookItem(
            category = category,
            item = item,
            unit = obj.optString("unit", "EA"),
            measurementBasis = obj.optString("measurement_basis", "auto"),
            marketSection = marketSection,
            laborRate = obj.optDouble("labor_rate", 85.0).takeIf { it.isFinite() } ?: 85.0,
            hourlyRate = if (hourlyRate.isFinite()) hourlyRate else 85.0,
            estimatedCompletionHours = if (estHours.isFinite()) estHours else 1.0,
            benchmarkLaborHoursPerUnit = obj.optDouble("benchmark_labor_hours_per_unit", 0.0).finiteOrZero(),
            lowHoursPerUnit = obj.optDouble("low_hours_per_unit", 0.0).finiteOrZero(),
            highHoursPerUnit = obj.optDouble("high_hours_per_unit", 0.0).finiteOrZero(),
            benchmarkUnitPrice = obj.optDouble("benchmark_unit_price", 0.0).finiteOrZero(),
            lowUnitPrice = obj.optDouble("low_unit_price", 0.0).finiteOrZero(),
            highUnitPrice = obj.optDouble("high_unit_price", 0.0).finiteOrZero(),
            benchmarkProductionUnitsPerLaborHour = obj.optDouble("benchmark_production_units_per_labor_hour", 0.0).finiteOrZero(),
            crewAssumption = obj.optString("crew_assumption", "1-person crew"),
            crewCount = obj.optDouble("crew_count", 1.0).takeIf { it.isFinite() } ?: 1.0,
            minimumLaborCharge = obj.optDouble("minimum_labor_charge", 0.0).finiteOrZero(),
            standardCondition = obj.optString("standard_condition", "Standard"),
            adjustmentEasy = obj.optDouble("adjustment_easy", 0.85).takeIf { it.isFinite() } ?: 0.85,
            adjustmentStandard = obj.optDouble("adjustment_standard", 1.0).takeIf { it.isFinite() } ?: 1.0,
            adjustmentDifficult = obj.optDouble("adjustment_difficult", 1.25).takeIf { it.isFinite() } ?: 1.25,
            adjustmentVeryDifficult = obj.optDouble("adjustment_very_difficult", 1.5).takeIf { it.isFinite() } ?: 1.5,
            scopeNotes = obj.optString("scope_notes", ""),
            materialIncluded = obj.optBoolean("material_included", false),
            equipmentIncluded = obj.optBoolean("equipment_included", false),
            permitsIncluded = obj.optBoolean("permits_included", false),
            cleanupIncluded = obj.optString("cleanup_included", "Not specified"),
            sourceStatus = obj.optString("source_status", "Custom THDR entry")
        )
    }

    fun parseTimeframes(jsonString: String): List<TimeframeBenchmark> {
        val array = JSONArray(jsonString)
        val result = ArrayList<TimeframeBenchmark>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            result.add(
                TimeframeBenchmark(
                    projectType = obj.optString("project_type", ""),
                    category = obj.optString("category", "Custom"),
                    typicalScope = obj.optString("typical_scope", ""),
                    crewCalendar = obj.optString("crew_calendar", ""),
                    lowHours = obj.optDouble("low_hours", 0.0).finiteOrZero(),
                    highHours = obj.optDouble("high_hours", 0.0).finiteOrZero(),
                    notes = obj.optString("notes", "")
                )
            )
        }
        return result
    }

    fun encodeBackup(
        categories: List<CategoryMeta>,
        items: List<PriceBookItem>,
        timeframes: List<TimeframeBenchmark>
    ): String {
        val root = JSONObject()
        root.put("version", 8)

        val catArr = JSONArray()
        val descObj = JSONObject()
        categories.forEach { meta ->
            catArr.put(meta.name)
            if (meta.description.isNotEmpty()) {
                descObj.put(meta.name, meta.description)
            }
        }
        root.put("categories", catArr)
        root.put("category_descriptions", descObj)

        val pbArr = JSONArray()
        items.forEach { item ->
            pbArr.put(JSONObject().apply {
                put("category", item.category)
                put("item", item.item)
                put("unit", item.unit)
                put("measurement_basis", item.measurementBasis)
                put("market_section", item.marketSection)
                put("labor_rate", item.laborRate)
                put("hourly_rate", item.hourlyRate)
                put("estimated_completion_hours", item.estimatedCompletionHours)
                put("benchmark_labor_hours_per_unit", item.benchmarkLaborHoursPerUnit)
                put("low_hours_per_unit", item.lowHoursPerUnit)
                put("high_hours_per_unit", item.highHoursPerUnit)
                put("benchmark_unit_price", item.benchmarkUnitPrice)
                put("low_unit_price", item.lowUnitPrice)
                put("high_unit_price", item.highUnitPrice)
                put("benchmark_production_units_per_labor_hour", item.benchmarkProductionUnitsPerLaborHour)
                put("crew_assumption", item.crewAssumption)
                put("crew_count", item.crewCount)
                put("minimum_labor_charge", item.minimumLaborCharge)
                put("standard_condition", item.standardCondition)
                put("adjustment_easy", item.adjustmentEasy)
                put("adjustment_standard", item.adjustmentStandard)
                put("adjustment_difficult", item.adjustmentDifficult)
                put("adjustment_very_difficult", item.adjustmentVeryDifficult)
                put("scope_notes", item.scopeNotes)
                put("material_included", item.materialIncluded)
                put("equipment_included", item.equipmentIncluded)
                put("permits_included", item.permitsIncluded)
                put("cleanup_included", item.cleanupIncluded)
                put("source_status", item.sourceStatus)
            })
        }
        root.put("pricebook", pbArr)

        val tfArr = JSONArray()
        timeframes.forEach { tf ->
            tfArr.put(JSONObject().apply {
                put("project_type", tf.projectType)
                put("category", tf.category)
                put("typical_scope", tf.typicalScope)
                put("crew_calendar", tf.crewCalendar)
                put("low_hours", tf.lowHours)
                put("high_hours", tf.highHours)
                put("notes", tf.notes)
            })
        }
        root.put("timeframes", tfArr)
        return root.toString(2)
    }

    fun decodeBackup(jsonString: String): PriceBookBackupPayload {
        val root = JSONObject(jsonString)
        val pbArr = root.optJSONArray("pricebook")
            ?: throw IllegalArgumentException("Invalid backup structure: missing pricebook array.")
        val tfArr = root.optJSONArray("timeframes")
            ?: throw IllegalArgumentException("Invalid backup structure: missing timeframes array.")

        val items = parsePriceBookItems(pbArr.toString())
        val timeframes = parseTimeframes(tfArr.toString())

        val descObj = root.optJSONObject("category_descriptions") ?: JSONObject()
        val catNames = linkedSetOf<String>()
        val catsArr = root.optJSONArray("categories")
        if (catsArr != null) {
            for (i in 0 until catsArr.length()) {
                val c = catsArr.optString(i, "").trim()
                if (c.isNotEmpty()) catNames.add(c)
            }
        } else {
            items.forEach { if (it.category.isNotBlank()) catNames.add(it.category) }
        }
        val categories = catNames.map { name ->
            CategoryMeta(name = name, description = descObj.optString(name, ""))
        }
        return PriceBookBackupPayload(categories = categories, items = items, timeframes = timeframes)
    }

    fun encodeEstimateDraft(draft: EstimateDraft, total: Double): String {
        val root = JSONObject()
        root.put("docType", draft.docType.key)
        draft.customerId?.let { root.put("customerId", it) }
        root.put("customer", draft.customer)
        root.put("project", draft.project)
        root.put("date", draft.date)
        root.put("validUntil", draft.validUntil)
        root.put("proposalStatus", draft.proposalStatus.key)
        root.put("depositPercent", draft.depositPercentText)
        root.put("termsText", draft.termsText)
        root.put("estimateSide", draft.estimateSide.key)
        root.put("discount", draft.discountText)
        root.put("tax", draft.taxText)
        root.put("totals", total)

        val matsObj = JSONObject()
        draft.manualMaterials.forEach { (k, v) -> matsObj.put(k, v) }
        root.put("manualMaterials", matsObj)

        val roomsArr = JSONArray()
        draft.rooms.forEach { room ->
            val rObj = JSONObject()
            rObj.put("id", room.id)
            rObj.put("name", room.name)
            rObj.put("side", room.side.key)
            rObj.put("l", room.lengthFt)
            rObj.put("w", room.widthFt)
            rObj.put("h", room.heightFt)
            rObj.put("fenceLF", room.fenceLF)
            rObj.put("doors", room.doors)
            rObj.put("windows", room.windows)
            rObj.put("condition", room.condition)
            rObj.put("roomNotes", room.roomNotes)
            rObj.put("expanded", room.expanded)

            val svcArr = JSONArray()
            room.services.forEach { svcArr.put(it) }
            rObj.put("services", svcArr)

            val selObj = JSONObject()
            room.serviceSelections.forEach { (svc, sel) ->
                selObj.put(svc, JSONObject().apply {
                    put("selectedPbItemName", sel.selectedPbItemName)
                    put("overrideUnit", sel.overrideUnit)
                    put("overrideMeasurementBasis", sel.overrideMeasurementBasis)
                    put("quantity", sel.quantity)
                    put("quantityManual", sel.quantityManual)
                    put("rate", sel.rate)
                    put("rateInitialized", sel.rateInitialized)
                    put("scopeNotes", sel.scopeNotes)
                    put("selectedSubcategories", JSONArray(sel.selectedSubcategories))
                    val qObj = JSONObject()
                    sel.questionAnswers.forEach { (qKey, vals) -> qObj.put(qKey, JSONArray(vals)) }
                    put("questionAnswers", qObj)
                })
            }
            rObj.put("serviceSelections", selObj)
            roomsArr.put(rObj)
        }
        root.put("rooms", roomsArr)
        return root.toString()
    }

    fun decodeEstimateDraft(id: Long?, jsonString: String): EstimateDraft {
        val root = JSONObject(jsonString)
        val matsObj = root.optJSONObject("manualMaterials") ?: JSONObject()
        val manualMaterials = linkedMapOf<String, Double>()
        matsObj.keys().forEach { k -> manualMaterials[k] = matsObj.optDouble(k, 0.0).finiteOrZero() }

        val roomsArr = root.optJSONArray("rooms") ?: JSONArray()
        val rooms = mutableListOf<EstimateRoom>()
        for (i in 0 until roomsArr.length()) {
            val rObj = roomsArr.optJSONObject(i) ?: continue
            val svcArr = rObj.optJSONArray("services") ?: JSONArray()
            val services = (0 until svcArr.length()).mapNotNull { svcArr.optString(it, "").takeIf { s -> s.isNotBlank() } }
            val selObj = rObj.optJSONObject("serviceSelections") ?: JSONObject()
            val selections = linkedMapOf<String, RoomServiceSelection>()
            selObj.keys().forEach { svc ->
                val sObj = selObj.optJSONObject(svc) ?: return@forEach
                val subArr = sObj.optJSONArray("selectedSubcategories") ?: JSONArray()
                val subs = (0 until subArr.length()).map { subArr.optString(it, "") }.filter { it.isNotBlank() }
                val qObj = sObj.optJSONObject("questionAnswers") ?: JSONObject()
                val qAnswers = linkedMapOf<String, List<String>>()
                qObj.keys().forEach { qKey ->
                    val arr = qObj.optJSONArray(qKey) ?: JSONArray()
                    qAnswers[qKey] = (0 until arr.length()).map { arr.optString(it, "") }.filter { it.isNotBlank() }
                }
                selections[svc] = RoomServiceSelection(
                    serviceName = svc,
                    selectedPbItemName = sObj.optString("selectedPbItemName", ""),
                    overrideUnit = sObj.optString("overrideUnit", ""),
                    overrideMeasurementBasis = sObj.optString("overrideMeasurementBasis", ""),
                    quantity = sObj.optDouble("quantity", 0.0).finiteOrZero(),
                    quantityManual = sObj.optBoolean("quantityManual", false),
                    rate = sObj.optDouble("rate", 0.0).finiteOrZero(),
                    rateInitialized = sObj.optBoolean("rateInitialized", false),
                    scopeNotes = sObj.optString("scopeNotes", ""),
                    selectedSubcategories = subs,
                    questionAnswers = qAnswers
                )
            }
            rooms.add(
                EstimateRoom(
                    id = rObj.optString("id", UUID.randomUUID().toString()),
                    name = rObj.optString("name", "Room ${i + 1}"),
                    side = EstimateSide.fromKey(rObj.optString("side", "interior")),
                    lengthFt = rObj.optDouble("l", 0.0).finiteOrZero(),
                    widthFt = rObj.optDouble("w", 0.0).finiteOrZero(),
                    heightFt = rObj.optDouble("h", 0.0).finiteOrZero(),
                    fenceLF = rObj.optDouble("fenceLF", 0.0).finiteOrZero(),
                    doors = rObj.optInt("doors", 0),
                    windows = rObj.optInt("windows", 0),
                    condition = rObj.optString("condition", ""),
                    roomNotes = rObj.optString("roomNotes", ""),
                    expanded = rObj.optBoolean("expanded", true),
                    services = services,
                    serviceSelections = selections
                )
            )
        }

        val customerIdVal = if (root.has("customerId")) root.optLong("customerId").takeIf { it > 0L } else null
        return EstimateDraft(
            id = id,
            docType = com.example.model.DocumentType.fromKey(root.optString("docType", "estimate")),
            customerId = customerIdVal,
            customer = root.optString("customer", ""),
            project = root.optString("project", ""),
            date = root.optString("date", ""),
            validUntil = root.optString("validUntil", ""),
            proposalStatus = com.example.model.ProposalStatus.fromKey(root.optString("proposalStatus", "draft")),
            depositPercentText = root.optString("depositPercent", "25"),
            termsText = root.optString("termsText", ""),
            estimateSide = EstimateSide.fromKey(root.optString("estimateSide", "interior")),
            detailsVisible = true,
            rooms = rooms,
            manualMaterials = manualMaterials,
            discountText = root.optString("discount", "0"),
            taxText = root.optString("tax", "0")
        )
    }

    fun encodeAppSettings(settings: com.example.model.AppSettings): String {
        return JSONObject().apply {
            put("companyName", settings.companyName)
            put("companyTagline", settings.companyTagline)
            put("companyPhone", settings.companyPhone)
            put("companyEmail", settings.companyEmail)
            put("companyAddress", settings.companyAddress)
            put("companyLicense", settings.companyLicense)
            put("themePreset", settings.themePreset.key)
            put("accentColor", settings.accentColor.key)
            put("activeCustomSchemeId", settings.activeCustomSchemeId)
            put("customColorSchemes", JSONArray().apply {
                settings.customColorSchemes.forEach { scheme ->
                    put(encodeCustomColorScheme(scheme))
                }
            })
            put("showBlueprintBackdrop", settings.showBlueprintBackdrop)
            put("largeFieldText", settings.largeFieldText)
            put("showInternalCostProfit", settings.showInternalCostProfit)
            put("defaultHourlyRate", settings.defaultHourlyRate)
            put("defaultTaxPercent", settings.defaultTaxPercent)
            put("currencySymbol", settings.currencySymbol)
            put("defaultEstimateSide", settings.defaultEstimateSide.key)
            put("defaultFlooringWastePct", settings.defaultFlooringWastePct)
            put("defaultPaintWastePct", settings.defaultPaintWastePct)
            put("defaultDrywallWastePct", settings.defaultDrywallWastePct)
            put("defaultTileWastePct", settings.defaultTileWastePct)
            put("defaultRoofingWastePct", settings.defaultRoofingWastePct)
            put("paintCoverageSqFtPerGal", settings.paintCoverageSqFtPerGal)
            put("defaultProposalValidDays", settings.defaultProposalValidDays)
            put("defaultProposalDepositPct", settings.defaultProposalDepositPct)
            put("defaultProposalTerms", settings.defaultProposalTerms)
            put("includeSignatureBlockOnProposal", settings.includeSignatureBlockOnProposal)
            put("privacyPolicyUrl", settings.privacyPolicyUrl)
            put("headerEmblemChoice", settings.headerEmblemChoice.key)
            put("customHeaderEmblemPath", settings.customHeaderEmblemPath)
            put("brandLogoChoice", settings.brandLogoChoice.key)
            put("customBrandLogoPath", settings.customBrandLogoPath)
            put("heroBackdropChoice", settings.heroBackdropChoice.key)
            put("customBackdropPath", settings.customBackdropPath)
            put("mascotChoice", settings.mascotChoice.key)
            put("customMascotPath", settings.customMascotPath)
            put("homeMascotPosition", settings.homeMascotPosition.key)
            put("showHomeHeroBanner", settings.showHomeHeroBanner)
            put("backdropOpacityPct", settings.backdropOpacityPct)
            put("pdfColorTheme", settings.pdfColorTheme.key)
            put("pdfLogoMode", settings.pdfLogoMode.key)
            put("customPdfLogoPath", settings.customPdfLogoPath)
            put("pdfShowBrandBadge", settings.pdfShowBrandBadge)
            put("pdfShowCornerPattern", settings.pdfShowCornerPattern)
            put("pdfShowSplitBackdrop", settings.pdfShowSplitBackdrop)
            put("pdfCompanyTitleOverride", settings.pdfCompanyTitleOverride)
            put("pdfEstimateTitle", settings.pdfEstimateTitle)
            put("pdfProposalTitle", settings.pdfProposalTitle)
            put("pdfDocNumberPrefix", settings.pdfDocNumberPrefix)
            put("pdfShowCustomerSection", settings.pdfShowCustomerSection)
            put("pdfCustomerSectionTitle", settings.pdfCustomerSectionTitle)
            put("pdfShowProjectDescription", settings.pdfShowProjectDescription)
            put("pdfProjectSectionTitle", settings.pdfProjectSectionTitle)
            put("pdfShowMaterialsTable", settings.pdfShowMaterialsTable)
            put("pdfMaterialsSectionTitle", settings.pdfMaterialsSectionTitle)
            put("pdfShowNotesSection", settings.pdfShowNotesSection)
            put("pdfCustomEstimateNotes", settings.pdfCustomEstimateNotes)
            put("pdfShowContactSection", settings.pdfShowContactSection)
            put("pdfContactSectionTitle", settings.pdfContactSectionTitle)
            put("includeSignatureBlockOnEstimate", settings.includeSignatureBlockOnEstimate)
            put("pdfContractorSignatureLabel", settings.pdfContractorSignatureLabel)
            put("pdfClientSignatureLabel", settings.pdfClientSignatureLabel)
        }.toString()
    }

    fun decodeAppSettings(jsonString: String): com.example.model.AppSettings {
        val defaults = com.example.model.AppSettings()
        if (jsonString.isBlank()) return defaults
        val root = JSONObject(jsonString)
        return com.example.model.AppSettings(
            companyName = root.optString("companyName", defaults.companyName),
            companyTagline = root.optString("companyTagline", defaults.companyTagline),
            companyPhone = root.optString("companyPhone", defaults.companyPhone),
            companyEmail = root.optString("companyEmail", defaults.companyEmail),
            companyAddress = root.optString("companyAddress", defaults.companyAddress),
            companyLicense = root.optString("companyLicense", defaults.companyLicense),
            themePreset = com.example.model.AppThemePreset.fromKey(root.optString("themePreset", defaults.themePreset.key)),
            accentColor = com.example.model.AppAccentColor.fromKey(root.optString("accentColor", defaults.accentColor.key)),
            customColorSchemes = decodeCustomColorSchemes(root.optJSONArray("customColorSchemes")),
            activeCustomSchemeId = root.optString("activeCustomSchemeId", defaults.activeCustomSchemeId),
            showBlueprintBackdrop = root.optBoolean("showBlueprintBackdrop", defaults.showBlueprintBackdrop),
            largeFieldText = root.optBoolean("largeFieldText", defaults.largeFieldText),
            showInternalCostProfit = root.optBoolean("showInternalCostProfit", defaults.showInternalCostProfit),
            defaultHourlyRate = root.optDouble("defaultHourlyRate", defaults.defaultHourlyRate).finiteOrZero().takeIf { it > 0 } ?: defaults.defaultHourlyRate,
            defaultTaxPercent = root.optDouble("defaultTaxPercent", defaults.defaultTaxPercent).finiteOrZero(),
            currencySymbol = root.optString("currencySymbol", defaults.currencySymbol).ifBlank { "$" },
            defaultEstimateSide = EstimateSide.fromKey(root.optString("defaultEstimateSide", defaults.defaultEstimateSide.key)),
            defaultFlooringWastePct = root.optInt("defaultFlooringWastePct", defaults.defaultFlooringWastePct),
            defaultPaintWastePct = root.optInt("defaultPaintWastePct", defaults.defaultPaintWastePct),
            defaultDrywallWastePct = root.optInt("defaultDrywallWastePct", defaults.defaultDrywallWastePct),
            defaultTileWastePct = root.optInt("defaultTileWastePct", defaults.defaultTileWastePct),
            defaultRoofingWastePct = root.optInt("defaultRoofingWastePct", defaults.defaultRoofingWastePct),
            paintCoverageSqFtPerGal = root.optDouble("paintCoverageSqFtPerGal", defaults.paintCoverageSqFtPerGal).finiteOrZero().takeIf { it > 0 } ?: defaults.paintCoverageSqFtPerGal,
            defaultProposalValidDays = root.optInt("defaultProposalValidDays", defaults.defaultProposalValidDays),
            defaultProposalDepositPct = root.optDouble("defaultProposalDepositPct", defaults.defaultProposalDepositPct).finiteOrZero(),
            defaultProposalTerms = root.optString("defaultProposalTerms", defaults.defaultProposalTerms),
            includeSignatureBlockOnProposal = root.optBoolean("includeSignatureBlockOnProposal", defaults.includeSignatureBlockOnProposal),
            privacyPolicyUrl = root.optString("privacyPolicyUrl", defaults.privacyPolicyUrl),
            headerEmblemChoice = com.example.model.HeaderEmblemChoice.fromKey(root.optString("headerEmblemChoice", defaults.headerEmblemChoice.key)),
            customHeaderEmblemPath = root.optString("customHeaderEmblemPath", defaults.customHeaderEmblemPath),
            brandLogoChoice = com.example.model.BrandLogoChoice.fromKey(root.optString("brandLogoChoice", defaults.brandLogoChoice.key)),
            customBrandLogoPath = root.optString("customBrandLogoPath", defaults.customBrandLogoPath),
            heroBackdropChoice = com.example.model.HeroBackdropChoice.fromKey(root.optString("heroBackdropChoice", defaults.heroBackdropChoice.key)),
            customBackdropPath = root.optString("customBackdropPath", defaults.customBackdropPath),
            mascotChoice = com.example.model.MascotSlotChoice.fromKey(root.optString("mascotChoice", defaults.mascotChoice.key)),
            customMascotPath = root.optString("customMascotPath", defaults.customMascotPath),
            homeMascotPosition = com.example.model.HeroMascotPosition.fromKey(root.optString("homeMascotPosition", defaults.homeMascotPosition.key)),
            showHomeHeroBanner = root.optBoolean("showHomeHeroBanner", defaults.showHomeHeroBanner),
            backdropOpacityPct = root.optInt("backdropOpacityPct", defaults.backdropOpacityPct).coerceIn(5, 45),
            pdfColorTheme = com.example.model.PdfColorTheme.fromKey(root.optString("pdfColorTheme", defaults.pdfColorTheme.key)),
            pdfLogoMode = com.example.model.PdfLogoMode.fromKey(root.optString("pdfLogoMode", defaults.pdfLogoMode.key)),
            customPdfLogoPath = root.optString("customPdfLogoPath", defaults.customPdfLogoPath),
            pdfShowBrandBadge = root.optBoolean("pdfShowBrandBadge", defaults.pdfShowBrandBadge),
            pdfShowCornerPattern = root.optBoolean("pdfShowCornerPattern", defaults.pdfShowCornerPattern),
            pdfShowSplitBackdrop = root.optBoolean("pdfShowSplitBackdrop", defaults.pdfShowSplitBackdrop),
            pdfCompanyTitleOverride = root.optString("pdfCompanyTitleOverride", defaults.pdfCompanyTitleOverride),
            pdfEstimateTitle = root.optString("pdfEstimateTitle", defaults.pdfEstimateTitle),
            pdfProposalTitle = root.optString("pdfProposalTitle", defaults.pdfProposalTitle),
            pdfDocNumberPrefix = root.optString("pdfDocNumberPrefix", defaults.pdfDocNumberPrefix),
            pdfShowCustomerSection = root.optBoolean("pdfShowCustomerSection", defaults.pdfShowCustomerSection),
            pdfCustomerSectionTitle = root.optString("pdfCustomerSectionTitle", defaults.pdfCustomerSectionTitle),
            pdfShowProjectDescription = root.optBoolean("pdfShowProjectDescription", defaults.pdfShowProjectDescription),
            pdfProjectSectionTitle = root.optString("pdfProjectSectionTitle", defaults.pdfProjectSectionTitle),
            pdfShowMaterialsTable = root.optBoolean("pdfShowMaterialsTable", defaults.pdfShowMaterialsTable),
            pdfMaterialsSectionTitle = root.optString("pdfMaterialsSectionTitle", defaults.pdfMaterialsSectionTitle),
            pdfShowNotesSection = root.optBoolean("pdfShowNotesSection", defaults.pdfShowNotesSection),
            pdfCustomEstimateNotes = root.optString("pdfCustomEstimateNotes", defaults.pdfCustomEstimateNotes),
            pdfShowContactSection = root.optBoolean("pdfShowContactSection", defaults.pdfShowContactSection),
            pdfContactSectionTitle = root.optString("pdfContactSectionTitle", defaults.pdfContactSectionTitle),
            includeSignatureBlockOnEstimate = root.optBoolean("includeSignatureBlockOnEstimate", defaults.includeSignatureBlockOnEstimate),
            pdfContractorSignatureLabel = root.optString("pdfContractorSignatureLabel", defaults.pdfContractorSignatureLabel),
            pdfClientSignatureLabel = root.optString("pdfClientSignatureLabel", defaults.pdfClientSignatureLabel)
        )
    }

    fun encodeCustomColorScheme(scheme: com.example.model.CustomColorScheme): JSONObject =
        JSONObject().apply {
            put("id", scheme.id)
            put("name", scheme.name)
            put("isDark", scheme.isDark)
            put("primaryHex", com.example.model.ColorHexCodec.normalizeHex(scheme.primaryHex, "#2F5D8A"))
            put("secondaryHex", com.example.model.ColorHexCodec.normalizeHex(scheme.secondaryHex, "#5E8FB9"))
            put("accentHex", com.example.model.ColorHexCodec.normalizeHex(scheme.accentHex, "#F5832B"))
            put("backgroundHex", com.example.model.ColorHexCodec.normalizeHex(scheme.backgroundHex, if (scheme.isDark) "#1B334D" else "#E6E7EA"))
            put("surfaceHex", com.example.model.ColorHexCodec.normalizeHex(scheme.surfaceHex, if (scheme.isDark) "#233F5E" else "#FFFFFF"))
            put("textPrimaryHex", com.example.model.ColorHexCodec.normalizeHex(scheme.textPrimaryHex, if (scheme.isDark) "#FFFFFF" else "#1D2B3A"))
            put("textMutedHex", com.example.model.ColorHexCodec.normalizeHex(scheme.textMutedHex, if (scheme.isDark) "#CDE3F5" else "#4A4A4A"))
            put("outlineHex", com.example.model.ColorHexCodec.normalizeHex(scheme.outlineHex, "#5E8FB9"))
            put("cardOpacityPct", scheme.cardOpacityPct.coerceIn(55, 100))
            put("navOpacityPct", scheme.navOpacityPct.coerceIn(55, 100))
        }

    fun decodeCustomColorScheme(
        obj: JSONObject,
        fallbackId: String = "scheme_${System.currentTimeMillis()}"
    ): com.example.model.CustomColorScheme {
        val isDark = obj.optBoolean("isDark", false)
        val rawId = obj.optString("id", "").trim()
        val rawName = obj.optString("name", "").trim().ifBlank { "Custom Scheme" }
        return com.example.model.CustomColorScheme(
            id = rawId.ifBlank { fallbackId },
            name = rawName,
            isDark = isDark,
            primaryHex = com.example.model.ColorHexCodec.normalizeHex(obj.optString("primaryHex", ""), "#2F5D8A"),
            secondaryHex = com.example.model.ColorHexCodec.normalizeHex(obj.optString("secondaryHex", ""), "#5E8FB9"),
            accentHex = com.example.model.ColorHexCodec.normalizeHex(obj.optString("accentHex", ""), "#F5832B"),
            backgroundHex = com.example.model.ColorHexCodec.normalizeHex(
                obj.optString("backgroundHex", ""),
                if (isDark) "#1B334D" else "#E6E7EA"
            ),
            surfaceHex = com.example.model.ColorHexCodec.normalizeHex(
                obj.optString("surfaceHex", ""),
                if (isDark) "#233F5E" else "#FFFFFF"
            ),
            textPrimaryHex = com.example.model.ColorHexCodec.normalizeHex(
                obj.optString("textPrimaryHex", ""),
                if (isDark) "#FFFFFF" else "#1D2B3A"
            ),
            textMutedHex = com.example.model.ColorHexCodec.normalizeHex(
                obj.optString("textMutedHex", ""),
                if (isDark) "#CDE3F5" else "#4A4A4A"
            ),
            outlineHex = com.example.model.ColorHexCodec.normalizeHex(obj.optString("outlineHex", ""), "#5E8FB9"),
            cardOpacityPct = obj.optInt("cardOpacityPct", 86).coerceIn(55, 100),
            navOpacityPct = obj.optInt("navOpacityPct", 85).coerceIn(55, 100)
        )
    }

    private fun decodeCustomColorSchemes(array: JSONArray?): List<com.example.model.CustomColorScheme> {
        if (array == null) return emptyList()
        val list = mutableListOf<com.example.model.CustomColorScheme>()
        val seenIds = mutableSetOf<String>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val scheme = decodeCustomColorScheme(obj, fallbackId = "scheme_${i + 1}")
            if (seenIds.add(scheme.id)) {
                list.add(scheme)
            }
        }
        return list
    }

    private fun Double.finiteOrZero(): Double = if (this.isFinite()) this else 0.0
}
