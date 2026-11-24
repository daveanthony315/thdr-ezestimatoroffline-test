package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EstimatorRepository
import com.example.data.db.EstimatorDatabase
import com.example.domain.EstimatePrintHelper
import com.example.domain.EstimatorEngine
import com.example.domain.PriceBookJsonCodec
import com.example.model.CalcResultRow
import com.example.model.CategoryMeta
import com.example.model.EstimateDraft
import com.example.model.EstimateRoom
import com.example.model.EstimateSide
import com.example.model.MaterialCalcKind
import com.example.model.MeasurementBasis
import com.example.model.PriceBookConstants
import com.example.model.PriceBookItem
import com.example.model.PriceViewMode
import com.example.model.RoomServiceSelection
import com.example.model.SavedProposal
import com.example.model.TakeoffResult
import com.example.model.TimeframeBenchmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class EstimatorUiState(
    val draft: EstimateDraft = EstimateDraft(date = todayIsoDate()),
    val takeoff: TakeoffResult = TakeoffResult(
        materials = emptyMap(),
        serviceTotal = 0.0,
        jobCost = 0.0,
        grandTotal = 0.0,
        grossProfit = 0.0,
        serviceBreakdown = emptyMap()
    ),
    val savedProposals: List<SavedProposal> = emptyList(),
    val categories: List<CategoryMeta> = emptyList(),
    val priceBookItems: List<PriceBookItem> = emptyList(),
    val timeframes: List<TimeframeBenchmark> = emptyList(),
    val headerStatus: String = "Local workspace",
    val saveMessage: String = "",
    val pbSaveStatus: String = "Changes are saved automatically on this device. Use Save Price Book to confirm your catalog.",
    val pbSearchQuery: String = "",
    val pbCategoryFilter: String = PriceBookConstants.FILTER_ALL,
    val pbPriceViewMode: PriceViewMode = PriceViewMode.BENCHMARK,
    val expandedPbCategory: String? = null,
    val tfSearchQuery: String = "",
    val tfCategoryFilter: String = PriceBookConstants.FILTER_ALL,
    val calcResults: List<CalcResultRow> = emptyList(),
    val snackbarMessage: String? = null
)

private fun todayIsoDate(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

private fun nowIsoTimestamp(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

class EstimatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EstimatorRepository(
        context = application,
        dao = EstimatorDatabase.getInstance(application).estimatorDao()
    )

    private val _draft = MutableStateFlow(EstimateDraft(date = todayIsoDate()))
    private val _headerStatus = MutableStateFlow("Local workspace")
    private val _saveMessage = MutableStateFlow("")
    private val _pbSaveStatus = MutableStateFlow(
        "Changes are saved automatically on this device. Use Save Price Book to confirm your catalog."
    )
    private val _pbSearchQuery = MutableStateFlow("")
    private val _pbCategoryFilter = MutableStateFlow(PriceBookConstants.FILTER_ALL)
    private val _pbPriceViewMode = MutableStateFlow(PriceViewMode.BENCHMARK)
    private val _expandedPbCategory = MutableStateFlow<String?>(null)
    private val _tfSearchQuery = MutableStateFlow("")
    private val _tfCategoryFilter = MutableStateFlow(PriceBookConstants.FILTER_ALL)
    private val _calcResults = MutableStateFlow<Map<String, CalcResultRow>>(linkedMapOf())
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private data class CatalogState(
        val categories: List<CategoryMeta>,
        val items: List<PriceBookItem>,
        val timeframes: List<TimeframeBenchmark>,
        val proposals: List<SavedProposal>
    )

    private data class FilterState(
        val pbSearch: String,
        val pbCategory: String,
        val pbPriceMode: PriceViewMode,
        val expandedPbCategory: String?,
        val tfSearch: String,
        val tfCategory: String
    )

    private val catalogFlow = combine(
        repository.categories,
        repository.priceBookItems,
        repository.timeframes,
        repository.savedProposals
    ) { cats, items, tfs, proposals ->
        CatalogState(cats, items, tfs, proposals)
    }

    private val filterFlow = combine(
        _pbSearchQuery,
        _pbCategoryFilter,
        _pbPriceViewMode,
        _expandedPbCategory,
        _tfSearchQuery,
        _tfCategoryFilter
    ) { arr ->
        FilterState(
            pbSearch = arr[0] as String,
            pbCategory = arr[1] as String,
            pbPriceMode = arr[2] as PriceViewMode,
            expandedPbCategory = arr[3] as String?,
            tfSearch = arr[4] as String,
            tfCategory = arr[5] as String
        )
    }

    val uiState: StateFlow<EstimatorUiState> = combine(
        _draft,
        catalogFlow,
        filterFlow,
        _headerStatus,
        _saveMessage,
        _pbSaveStatus,
        _calcResults,
        _snackbarMessage
    ) { arr ->
        val draft = arr[0] as EstimateDraft
        val catalog = arr[1] as CatalogState
        val filters = arr[2] as FilterState
        val headerStatus = arr[3] as String
        val saveMsg = arr[4] as String
        val pbStatus = arr[5] as String
        @Suppress("UNCHECKED_CAST")
        val calcMap = arr[6] as Map<String, CalcResultRow>
        val snackbar = arr[7] as String?

        val discount = draft.discountText.toDoubleOrNull() ?: 0.0
        val tax = draft.taxText.toDoubleOrNull() ?: 0.0
        val takeoff = EstimatorEngine.computeTakeoff(
            rooms = draft.rooms,
            priceBook = catalog.items,
            manualMaterials = draft.manualMaterials,
            discount = discount,
            taxPercent = tax
        )

        EstimatorUiState(
            draft = draft,
            takeoff = takeoff,
            savedProposals = catalog.proposals,
            categories = catalog.categories.sortedWith(naturalCategoryComparator()),
            priceBookItems = catalog.items,
            timeframes = catalog.timeframes,
            headerStatus = headerStatus,
            saveMessage = saveMsg,
            pbSaveStatus = pbStatus,
            pbSearchQuery = filters.pbSearch,
            pbCategoryFilter = filters.pbCategory,
            pbPriceViewMode = filters.pbPriceMode,
            expandedPbCategory = filters.expandedPbCategory,
            tfSearchQuery = filters.tfSearch,
            tfCategoryFilter = filters.tfCategory,
            calcResults = calcMap.values.toList(),
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EstimatorUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    // --- Estimate Metadata & Lifecycle ---

    fun createNewEstimate() {
        _draft.value = EstimateDraft(
            id = null,
            customer = "",
            project = "",
            date = todayIsoDate(),
            estimateSide = EstimateSide.INTERIOR,
            detailsVisible = true,
            rooms = emptyList(),
            manualMaterials = emptyMap(),
            discountText = "0",
            taxText = "0"
        )
        _saveMessage.value = ""
        _headerStatus.value = "Local workspace"
    }

    fun toggleDetailsVisible(visible: Boolean) {
        _draft.update { it.copy(detailsVisible = visible) }
    }

    fun updateCustomer(value: String) {
        _draft.update { it.copy(customer = value) }
    }

    fun updateProject(value: String) {
        _draft.update { it.copy(project = value) }
    }

    fun updateDate(value: String) {
        _draft.update { it.copy(date = value) }
    }

    fun updateDiscount(value: String) {
        _draft.update { it.copy(discountText = value) }
    }

    fun updateTax(value: String) {
        _draft.update { it.copy(taxText = value) }
    }

    fun setEstimateSide(side: EstimateSide) {
        _draft.update { it.copy(estimateSide = side) }
    }

    fun saveCurrentEstimate() {
        val state = uiState.value
        val draft = state.draft
        val total = state.takeoff.grandTotal
        val json = PriceBookJsonCodec.encodeEstimateDraft(draft, total)
        viewModelScope.launch {
            val savedId = repository.saveEstimate(
                existingId = draft.id,
                customer = draft.customer,
                project = draft.project,
                date = draft.date,
                total = total,
                savedAt = nowIsoTimestamp(),
                payloadJson = json
            )
            _draft.update { it.copy(id = savedId) }
            _saveMessage.value = "Estimate saved locally on this device."
            _headerStatus.value = "Estimate saved locally"
            _snackbarMessage.value = "Estimate saved locally"
        }
    }

    fun loadSavedProposal(proposal: SavedProposal) {
        val decoded = PriceBookJsonCodec.decodeEstimateDraft(proposal.id, proposal.payloadJson)
        val syncedRooms = decoded.rooms.map { EstimatorEngine.syncRoomServiceSelections(it, uiState.value.priceBookItems) }
        _draft.value = decoded.copy(rooms = syncedRooms)
        _saveMessage.value = "Local proposal loaded for editing."
        _headerStatus.value = "Proposal loaded"
    }

    fun deleteSavedProposal(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedEstimate(id)
            if (_draft.value.id == id) {
                _draft.update { it.copy(id = null) }
            }
            _snackbarMessage.value = "Saved proposal deleted"
        }
    }

    fun printCurrentEstimate(context: Context) {
        val state = uiState.value
        EstimatePrintHelper.printEstimate(
            context = context,
            draft = state.draft,
            takeoff = state.takeoff,
            priceBook = state.priceBookItems
        )
    }

    // --- Rooms & Services ---

    fun addRoomType(type: String) {
        val existing = _draft.value.rooms
        val count = existing.count { it.name.trim().lowercase(Locale.US).startsWith(type.lowercase(Locale.US)) }
        val name = if (type == "Bedroom" && count > 0) "Bedroom ${count + 1}" else type
        val newRoom = EstimateRoom(
            id = UUID.randomUUID().toString(),
            name = name,
            side = EstimateSide.INTERIOR,
            expanded = true
        )
        _draft.update { it.copy(rooms = it.rooms + newRoom) }
    }

    fun addExteriorArea(type: String) {
        val newRoom = EstimateRoom(
            id = UUID.randomUUID().toString(),
            name = type,
            side = EstimateSide.EXTERIOR,
            expanded = true
        )
        _draft.update { it.copy(rooms = it.rooms + newRoom) }
    }

    fun toggleRoomExpanded(roomId: String) {
        updateRoom(roomId) { it.copy(expanded = !it.expanded) }
    }

    fun removeRoom(roomId: String) {
        _draft.update { d -> d.copy(rooms = d.rooms.filterNot { it.id == roomId }) }
    }

    fun updateRoomName(roomId: String, name: String) {
        updateRoom(roomId) { it.copy(name = name) }
    }

    fun updateRoomDimensions(roomId: String, l: Double? = null, w: Double? = null, h: Double? = null) {
        val pb = uiState.value.priceBookItems
        updateRoom(roomId) { room ->
            val updated = room.copy(
                lengthFt = maxOf(0.0, l ?: room.lengthFt),
                widthFt = maxOf(0.0, w ?: room.widthFt),
                heightFt = maxOf(0.0, h ?: room.heightFt)
            )
            EstimatorEngine.syncRoomServiceSelections(updated, pb)
        }
    }

    fun updateRoomFenceLength(roomId: String, fenceLf: Double) {
        val pb = uiState.value.priceBookItems
        val clean = maxOf(0.0, fenceLf)
        updateRoom(roomId) { room ->
            val updatedSelections = room.serviceSelections.toMutableMap()
            val existingFencing = updatedSelections["Fencing"]
            if (existingFencing != null) {
                updatedSelections["Fencing"] = existingFencing.copy(quantity = clean, quantityManual = false)
            }
            EstimatorEngine.syncRoomServiceSelections(
                room.copy(fenceLF = clean, serviceSelections = updatedSelections),
                pb
            )
        }
    }

    fun updateRoomOpenings(roomId: String, doors: Int? = null, windows: Int? = null) {
        val pb = uiState.value.priceBookItems
        updateRoom(roomId) { room ->
            val updated = room.copy(
                doors = maxOf(0, doors ?: room.doors),
                windows = maxOf(0, windows ?: room.windows)
            )
            EstimatorEngine.syncRoomServiceSelections(updated, pb)
        }
    }

    fun updateRoomCondition(roomId: String, condition: String) {
        updateRoom(roomId) { it.copy(condition = condition) }
    }

    fun updateRoomNotes(roomId: String, notes: String) {
        updateRoom(roomId) { it.copy(roomNotes = notes) }
    }

    fun toggleRoomService(roomId: String, service: String) {
        val pb = uiState.value.priceBookItems
        updateRoom(roomId) { room ->
            val nextServices = if (room.services.contains(service)) {
                room.services - service
            } else {
                room.services + service
            }
            EstimatorEngine.syncRoomServiceSelections(room.copy(services = nextServices), pb)
        }
    }

    fun selectServicePbItem(roomId: String, service: String, pbItemName: String) {
        val pb = uiState.value.priceBookItems
        val chosen = EstimatorEngine.pbItemsForService(service, pb).firstOrNull { it.item == pbItemName } ?: return
        updateRoom(roomId) { room ->
            val d = EstimatorEngine.computeDimensions(room)
            val basis = chosen.effectiveBasis()
            val qty = EstimatorEngine.defaultQty(chosen.unit, d, room, service, basis)
            val rate = chosen.defaultEstimateRate()
            val cur = room.serviceSelections[service] ?: RoomServiceSelection(serviceName = service)
            val updated = cur.copy(
                selectedPbItemName = chosen.item,
                rate = rate,
                rateInitialized = true,
                quantity = qty,
                quantityManual = false
            )
            room.copy(serviceSelections = room.serviceSelections + (service to updated))
        }
    }

    fun updateServiceQty(roomId: String, service: String, qty: Double) {
        updateRoom(roomId) { room ->
            val cur = room.serviceSelections[service] ?: RoomServiceSelection(serviceName = service)
            val updated = cur.copy(quantity = maxOf(0.0, qty), quantityManual = true)
            room.copy(serviceSelections = room.serviceSelections + (service to updated))
        }
    }

    fun resetServiceQtyToMeasurement(roomId: String, service: String) {
        val pb = uiState.value.priceBookItems
        updateRoom(roomId) { room ->
            val cur = room.serviceSelections[service] ?: RoomServiceSelection(serviceName = service)
            val updated = room.copy(
                serviceSelections = room.serviceSelections + (service to cur.copy(quantityManual = false))
            )
            EstimatorEngine.syncRoomServiceSelections(updated, pb)
        }
    }

    fun updateServiceRate(roomId: String, service: String, rate: Double) {
        updateRoom(roomId) { room ->
            val cur = room.serviceSelections[service] ?: RoomServiceSelection(serviceName = service)
            val updated = cur.copy(rate = maxOf(0.0, rate), rateInitialized = true)
            room.copy(serviceSelections = room.serviceSelections + (service to updated))
        }
    }

    fun updateServiceScopeNotes(roomId: String, service: String, notes: String) {
        updateRoom(roomId) { room ->
            val cur = room.serviceSelections[service] ?: RoomServiceSelection(serviceName = service)
            val updated = cur.copy(scopeNotes = notes)
            room.copy(serviceSelections = room.serviceSelections + (service to updated))
        }
    }

    fun toggleServiceSubcategory(roomId: String, service: String, sub: String) {
        updateRoom(roomId) { room ->
            val cur = room.serviceSelections[service] ?: RoomServiceSelection(serviceName = service)
            val next = if (cur.selectedSubcategories.contains(sub)) {
                cur.selectedSubcategories - sub
            } else {
                cur.selectedSubcategories + sub
            }
            room.copy(serviceSelections = room.serviceSelections + (service to cur.copy(selectedSubcategories = next)))
        }
    }

    fun setServiceQuestionAnswer(roomId: String, key: String, service: String, option: String, isMulti: Boolean) {
        updateRoom(roomId) { room ->
            val cur = room.serviceSelections[service] ?: RoomServiceSelection(serviceName = service)
            val currentList = cur.questionAnswers[key].orEmpty()
            val nextList = if (isMulti) {
                if (currentList.contains(option)) currentList - option else currentList + option
            } else {
                if (option.isBlank()) emptyList() else listOf(option)
            }
            val nextAnswers = cur.questionAnswers + (key to nextList)
            room.copy(serviceSelections = room.serviceSelections + (service to cur.copy(questionAnswers = nextAnswers)))
        }
    }

    private fun updateRoom(roomId: String, transform: (EstimateRoom) -> EstimateRoom) {
        _draft.update { draft ->
            draft.copy(rooms = draft.rooms.map { if (it.id == roomId) transform(it) else it })
        }
    }

    // --- Materials & V7 Calculators ---

    fun addManualMaterial(name: String, qty: Double) {
        val cleanName = name.trim()
        if (cleanName.isEmpty() || qty <= 0.0) return
        _draft.update { d ->
            val next = d.manualMaterials.toMutableMap()
            next[cleanName] = qty
            d.copy(manualMaterials = next)
        }
    }

    fun removeManualMaterial(name: String) {
        _draft.update { d ->
            d.copy(manualMaterials = d.manualMaterials - name)
        }
    }

    fun runMaterialCalculator(
        kind: MaterialCalcKind,
        p1: Double = 0.0,
        p2: Double = 0.0,
        p3: Double = 0.0,
        p4: Double = 0.0
    ) {
        val result = EstimatorEngine.calculateMaterialTool(kind, p1, p2, p3, p4)
        result.onSuccess { rows ->
            _calcResults.update { existing ->
                val mutable = existing.toMutableMap()
                rows.forEach { mutable[it.key] = it }
                mutable
            }
        }.onFailure { err ->
            _snackbarMessage.value = err.message ?: "Check input values"
        }
    }

    fun clearCalcResults() {
        _calcResults.value = emptyMap()
    }

    fun addCalcResultsToProjectMaterials() {
        val rows = _calcResults.value.values.toList()
        if (rows.isEmpty()) {
            _snackbarMessage.value = "Calculate materials first."
            return
        }
        _draft.update { d ->
            val next = d.manualMaterials.toMutableMap()
            rows.forEach { row ->
                if (row.unit != "%") {
                    val label = "${row.name} (${row.unit})"
                    next[label] = (next[label] ?: 0.0) + row.qty
                }
            }
            d.copy(manualMaterials = next)
        }
        _snackbarMessage.value = "Calculator quantities added to project materials."
    }

    // --- Price Book Filtering & Editing ---

    fun setPbSearchQuery(query: String) {
        _pbSearchQuery.value = query
    }

    fun setPbCategoryFilter(filter: String) {
        _pbCategoryFilter.value = filter
    }

    fun setPbPriceViewMode(mode: PriceViewMode) {
        _pbPriceViewMode.value = mode
    }

    fun togglePbCategoryExpanded(categoryName: String) {
        _expandedPbCategory.update { current ->
            if (current == categoryName) null else categoryName
        }
    }

    fun addPbCategory(rawName: String) {
        val name = rawName.trim()
        if (name.isEmpty()) return
        if (uiState.value.categories.any { it.name.equals(name, ignoreCase = true) }) {
            _snackbarMessage.value = "That service category already exists."
            return
        }
        viewModelScope.launch {
            repository.addCategory(name)
            _expandedPbCategory.value = name
            _snackbarMessage.value = "Added service category \"$name\""
        }
    }

    fun renamePbCategory(oldName: String, rawNewName: String) {
        val newName = rawNewName.trim()
        if (newName.isEmpty() || newName == oldName) return
        if (uiState.value.categories.any { it.name.equals(newName, ignoreCase = true) }) {
            _snackbarMessage.value = "A service with that name already exists."
            return
        }
        val desc = uiState.value.categories.firstOrNull { it.name == oldName }?.description.orEmpty()
        viewModelScope.launch {
            repository.renameCategory(oldName, newName, desc)
            if (_expandedPbCategory.value == oldName) _expandedPbCategory.value = newName
            _snackbarMessage.value = "Renamed \"$oldName\" to \"$newName\""
        }
    }

    fun updatePbCategoryDescription(name: String, description: String) {
        viewModelScope.launch {
            repository.updateCategoryDescription(name, description)
        }
    }

    fun deletePbCategory(name: String) {
        viewModelScope.launch {
            repository.deleteCategory(name)
            if (_expandedPbCategory.value == name) _expandedPbCategory.value = null
            _snackbarMessage.value = "Removed service \"$name\""
        }
    }

    fun addPbItem(category: String, rawItemName: String) {
        val itemName = rawItemName.trim()
        if (itemName.isEmpty()) return
        if (uiState.value.priceBookItems.any {
                it.category == category && it.item.equals(itemName, ignoreCase = true)
            }
        ) {
            _snackbarMessage.value = "That subcategory already exists in this service."
            return
        }
        val newItem = PriceBookItem(
            category = category,
            item = itemName,
            marketSection = EstimatorEngine.inferMarketSection(itemName, category)
        )
        viewModelScope.launch {
            repository.addPriceBookItem(newItem)
            _expandedPbCategory.value = category
            _snackbarMessage.value = "Added \"$itemName\""
        }
    }

    fun updatePbItem(updated: PriceBookItem) {
        val normalizedBasis = if (MeasurementBasis.optionsForUnit(updated.unit)
                .any { it.key == updated.measurementBasis }
        ) updated.measurementBasis else "auto"
        val clean = updated.copy(measurementBasis = normalizedBasis)
        viewModelScope.launch {
            repository.updatePriceBookItem(clean)
            _draft.update { draft ->
                draft.copy(
                    rooms = draft.rooms.map { EstimatorEngine.syncRoomServiceSelections(it, uiState.value.priceBookItems.map { item -> if (item.id == clean.id) clean else item }) }
                )
            }
        }
    }

    fun deletePbItem(id: Long) {
        viewModelScope.launch {
            repository.deletePriceBookItem(id)
            _snackbarMessage.value = "Subcategory removed"
        }
    }

    fun applyCategoryBulkSettings(
        categoryName: String,
        description: String,
        unit: String,
        marketSection: String,
        hourlyRate: Double,
        laborRate: Double,
        benchmarkPrice: Double,
        lowPrice: Double,
        highPrice: Double,
        adjustmentStandard: Double,
        cleanupIncluded: String,
        materialIncluded: Boolean
    ) {
        val targets = uiState.value.priceBookItems.filter { it.category == categoryName }
        if (targets.isEmpty()) {
            _snackbarMessage.value = "There are no subcategories in this service yet."
            return
        }
        val validOptions = MeasurementBasis.optionsForUnit(unit).map { it.key }
        val updatedItems = targets.map { item ->
            item.copy(
                unit = unit,
                marketSection = marketSection,
                hourlyRate = hourlyRate,
                laborRate = laborRate,
                benchmarkUnitPrice = benchmarkPrice,
                lowUnitPrice = lowPrice,
                highUnitPrice = highPrice,
                adjustmentStandard = adjustmentStandard,
                cleanupIncluded = cleanupIncluded,
                materialIncluded = materialIncluded,
                measurementBasis = if (validOptions.contains(item.measurementBasis)) item.measurementBasis else "auto"
            )
        }
        viewModelScope.launch {
            repository.updateCategoryDescription(categoryName, description)
            repository.updatePriceBookItems(updatedItems)
            _pbSaveStatus.value = "Category settings applied to ${targets.size} subcategories in $categoryName."
            _snackbarMessage.value = "Updated ${targets.size} subcategories"
        }
    }

    fun savePriceBookNow() {
        val state = uiState.value
        _pbSaveStatus.value = "Saved on device · ${state.priceBookItems.size} line items · ${state.categories.size} services."
        _draft.update { draft ->
            draft.copy(rooms = draft.rooms.map { EstimatorEngine.syncRoomServiceSelections(it, state.priceBookItems) })
        }
        _snackbarMessage.value = "Price Book confirmed and synced with active estimate"
    }

    fun exportPriceBookToUri(uri: Uri) {
        val state = uiState.value
        val json = PriceBookJsonCodec.encodeBackup(
            categories = state.categories,
            items = state.priceBookItems,
            timeframes = state.timeframes
        )
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    } ?: throw IllegalStateException("Could not open export file.")
                }
                _snackbarMessage.value = "Price Book backup exported"
            } catch (e: Exception) {
                _snackbarMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importPriceBookFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val payload = withContext(Dispatchers.IO) {
                    val text = getApplication<Application>().contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: throw IllegalStateException("Could not read selected file.")
                    PriceBookJsonCodec.decodeBackup(text)
                }
                repository.replaceBackup(payload.categories, payload.items, payload.timeframes)
                _pbSaveStatus.value = "Imported ${payload.items.size} items and ${payload.timeframes.size} timeframes."
                _snackbarMessage.value = "Price Book backup imported"
            } catch (e: Exception) {
                _snackbarMessage.value = "Import failed: ${e.message}"
            }
        }
    }

    // --- Timeframe Benchmarks ---

    fun setTfSearchQuery(query: String) {
        _tfSearchQuery.value = query
    }

    fun setTfCategoryFilter(cat: String) {
        _tfCategoryFilter.value = cat
    }

    fun addTimeframe(projectType: String) {
        val clean = projectType.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.addTimeframe(
                TimeframeBenchmark(
                    projectType = clean,
                    category = "Custom",
                    typicalScope = "",
                    crewCalendar = "",
                    lowHours = 0.0,
                    highHours = 0.0,
                    notes = ""
                )
            )
            _snackbarMessage.value = "Added timeframe \"$clean\""
        }
    }

    fun updateTimeframe(tf: TimeframeBenchmark) {
        viewModelScope.launch {
            repository.updateTimeframe(tf)
        }
    }

    fun deleteTimeframe(id: Long) {
        viewModelScope.launch {
            repository.deleteTimeframe(id)
            _snackbarMessage.value = "Timeframe removed"
        }
    }

    private fun naturalCategoryComparator(): Comparator<CategoryMeta> = Comparator { a, b ->
        compareNatural(a.name, b.name)
    }

    private fun compareNatural(a: String, b: String): Int {
        val re = Regex("(\\d+|\\D+)")
        val partsA = re.findAll(a).map { it.value }.toList()
        val partsB = re.findAll(b).map { it.value }.toList()
        val len = minOf(partsA.size, partsB.size)
        for (i in 0 until len) {
            val pa = partsA[i]
            val pb = partsB[i]
            val na = pa.toLongOrNull()
            val nb = pb.toLongOrNull()
            val cmp = if (na != null && nb != null) {
                na.compareTo(nb)
            } else {
                pa.compareTo(pb, ignoreCase = true)
            }
            if (cmp != 0) return cmp
        }
        return partsA.size.compareTo(partsB.size)
    }
}
