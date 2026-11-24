package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EstimatorDao {

    @Query("SELECT * FROM price_book_categories ORDER BY name ASC")
    fun observeCategories(): Flow<List<PriceBookCategoryEntity>>

    @Query("SELECT COUNT(*) FROM price_book_categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: PriceBookCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<PriceBookCategoryEntity>)

    @Query("DELETE FROM price_book_categories WHERE name = :name")
    suspend fun deleteCategoryByName(name: String)

    @Query("DELETE FROM price_book_categories")
    suspend fun clearCategories()

    @Query("SELECT * FROM price_book_items ORDER BY id ASC")
    fun observePriceBookItems(): Flow<List<PriceBookItemEntity>>

    @Query("SELECT COUNT(*) FROM price_book_items")
    suspend fun getPriceBookItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceBookItem(item: PriceBookItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceBookItems(items: List<PriceBookItemEntity>)

    @Update
    suspend fun updatePriceBookItem(item: PriceBookItemEntity)

    @Query("UPDATE price_book_items SET category = :newName WHERE category = :oldName")
    suspend fun renameCategoryInItems(oldName: String, newName: String)

    @Query("DELETE FROM price_book_items WHERE id = :id")
    suspend fun deletePriceBookItemById(id: Long)

    @Query("DELETE FROM price_book_items WHERE category = :categoryName")
    suspend fun deleteItemsByCategory(categoryName: String)

    @Query("DELETE FROM price_book_items")
    suspend fun clearPriceBookItems()

    @Query("SELECT * FROM timeframe_benchmarks ORDER BY id ASC")
    fun observeTimeframes(): Flow<List<TimeframeEntity>>

    @Query("SELECT COUNT(*) FROM timeframe_benchmarks")
    suspend fun getTimeframeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeframe(timeframe: TimeframeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeframes(timeframes: List<TimeframeEntity>)

    @Update
    suspend fun updateTimeframe(timeframe: TimeframeEntity)

    @Query("DELETE FROM timeframe_benchmarks WHERE id = :id")
    suspend fun deleteTimeframeById(id: Long)

    @Query("DELETE FROM timeframe_benchmarks")
    suspend fun clearTimeframes()

    @Query("SELECT * FROM saved_estimates ORDER BY id DESC")
    fun observeSavedEstimates(): Flow<List<SavedEstimateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedEstimate(estimate: SavedEstimateEntity): Long

    @Query("DELETE FROM saved_estimates WHERE id = :id")
    suspend fun deleteSavedEstimateById(id: Long)

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    fun observeCustomers(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observeAppSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppSettings(settings: AppSettingsEntity)

    @Transaction
    suspend fun renameCategoryTransaction(oldName: String, newName: String, description: String) {
        deleteCategoryByName(oldName)
        insertCategory(PriceBookCategoryEntity(name = newName, description = description))
        renameCategoryInItems(oldName = oldName, newName = newName)
    }

    @Transaction
    suspend fun deleteCategoryTransaction(categoryName: String) {
        deleteItemsByCategory(categoryName)
        deleteCategoryByName(categoryName)
    }

    @Transaction
    suspend fun replaceCatalogBackup(
        categories: List<PriceBookCategoryEntity>,
        items: List<PriceBookItemEntity>,
        timeframes: List<TimeframeEntity>
    ) {
        clearCategories()
        clearPriceBookItems()
        clearTimeframes()
        insertCategories(categories)
        insertPriceBookItems(items)
        insertTimeframes(timeframes)
    }
}
