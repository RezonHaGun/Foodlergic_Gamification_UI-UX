package com.maranatha.foodlergic.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.maranatha.foodlergic.data.dao.PredictionDao
import com.maranatha.foodlergic.data.entity.Prediction

@Database(entities = [Prediction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao
}
