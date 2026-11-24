package com.example.data

import android.content.Context
import com.example.data.db.EstimatorDao
import com.example.data.db.PriceBookCategoryEntity
import com.example.data.db.PriceBookItemEntity
import com.example.data.db.SavedEstimateEntity
import com.example.data.db.TimeframeEntity
import com.example.domain.PriceBookJsonCodec
import com.example.model.CategoryMeta
import com.example.model.PriceBookItem
import com.example.model.SavedProposal
import com.example.model.TimeframeBenchmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EstimatorRepository(
    private val context: Context,
    private val dao: EstimatorDao
) {
    val categories: Flow<List<CategoryMeta>> = dao.observeCategories().map { list ->
        list.map { it.toModel() }
    }

    val priceBookItems: Flow<List<PriceBookItem>> = dao.observePriceBookItems().map { list ->
        list.map { it.toModel() }
    }

    val timeframes: Flow<List<TimeframeBenchmark>> = dao.observeTimeframes().map { list ->
        list.map { it.toModel() }
    }

    val savedProposals: Flow<List<SavedProposal>> = dao.observeSavedEstimates().map { list ->
        list.map { it.toModel() }
    }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dao.getPriceBookItemCount() == 0 && dao.getCategoryCount() == 0) {
            val pbJson = context.assets.open("pricebook_seed.json").bufferedReader().use { it.readText() }
            val parsedItems = PriceBookJsonCodec.parsePriceBookItems(pbJson)
            val catNames = parsedItems.map { it.category }.filter { it.isNotBlank() }.distinct()
            dao.insertCategories(catNames.map { PriceBookCategoryEntity(name = it) })
            dao.insertPriceBookItems(parsedItems.map { PriceBookItemEntity.fromModel(it) })
        }
        if (dao.getTimeframeCount() == 0) {
            val tfJson = context.assets.open("timeframes_seed.json").bufferedReader().use { it.readText() }
            val parsedTf = PriceBookJsonCodec.parseTimeframes(tfJson)
            dao.insertTimeframes(parsedTf.map { TimeframeEntity.fromModel(it) })
        }
    }

    suspend fun addCategory(name: String) = withContext(Dispatchers.IO) {
        dao.insertCategory(PriceBookCategoryEntity(name = name))
    }

    suspend fun updateCategoryDescription(name: String, description: String) = withContext(Dispatchers.IO) {
        dao.insertCategory(PriceBookCategoryEntity(name = name, description = description))
    }

    suspend fun renameCategory(oldName: String, newName: String, description: String) = withContext(Dispatchers.IO) {
        dao.renameCategoryTransaction(oldName = oldName, newName = newName, description = description)
    }

    suspend fun deleteCategory(name: String) = withContext(Dispatchers.IO) {
        dao.deleteCategoryTransaction(name)
    }

    suspend fun addPriceBookItem(item: PriceBookItem): Long = withContext(Dispatchers.IO) {
        dao.insertPriceBookItem(PriceBookItemEntity.fromModel(item))
    }

    suspend fun updatePriceBookItem(item: PriceBookItem) = withContext(Dispatchers.IO) {
        dao.updatePriceBookItem(PriceBookItemEntity.fromModel(item))
    }

    suspend fun updatePriceBookItems(items: List<PriceBookItem>) = withContext(Dispatchers.IO) {
        dao.insertPriceBookItems(items.map { PriceBookItemEntity.fromModel(it) })
    }

    suspend fun deletePriceBookItem(id: Long) = withContext(Dispatchers.IO) {
        dao.deletePriceBookItemById(id)
    }

    suspend fun addTimeframe(tf: TimeframeBenchmark): Long = withContext(Dispatchers.IO) {
        dao.insertTimeframe(TimeframeEntity.fromModel(tf))
    }

    suspend fun updateTimeframe(tf: TimeframeBenchmark) = withContext(Dispatchers.IO) {
        dao.updateTimeframe(TimeframeEntity.fromModel(tf))
    }

    suspend fun deleteTimeframe(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTimeframeById(id)
    }

    suspend fun saveEstimate(
        existingId: Long?,
        customer: String,
        project: String,
        date: String,
        total: Double,
        savedAt: String,
        payloadJson: String
    ): Long = withContext(Dispatchers.IO) {
        dao.insertSavedEstimate(
            SavedEstimateEntity(
                id = existingId ?: 0L,
                customer = customer,
                project = project,
                date = date,
                total = total,
                savedAt = savedAt,
                payloadJson = payloadJson
            )
        )
    }

    suspend fun deleteSavedEstimate(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteSavedEstimateById(id)
    }

    suspend fun replaceBackup(
        categories: List<CategoryMeta>,
        items: List<PriceBookItem>,
        timeframes: List<TimeframeBenchmark>
    ) = withContext(Dispatchers.IO) {
        dao.replaceCatalogBackup(
            categories = categories.map { PriceBookCategoryEntity(name = it.name, description = it.description) },
            items = items.map { PriceBookItemEntity.fromModel(it.copy(id = 0)) },
            timeframes = timeframes.map { TimeframeEntity.fromModel(it.copy(id = 0)) }
        )
    }
}
