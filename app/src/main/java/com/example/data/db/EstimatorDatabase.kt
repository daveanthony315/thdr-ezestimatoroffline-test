package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PriceBookCategoryEntity::class,
        PriceBookItemEntity::class,
        TimeframeEntity::class,
        SavedEstimateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EstimatorDatabase : RoomDatabase() {
    abstract fun estimatorDao(): EstimatorDao

    companion object {
        @Volatile
        private var INSTANCE: EstimatorDatabase? = null

        fun getInstance(context: Context): EstimatorDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EstimatorDatabase::class.java,
                    "thdr_estimator_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
