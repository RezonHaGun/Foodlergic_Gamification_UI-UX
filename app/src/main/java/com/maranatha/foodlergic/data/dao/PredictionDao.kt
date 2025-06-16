package com.maranatha.foodlergic.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.maranatha.foodlergic.data.entity.Prediction

@Dao
interface PredictionDao {
    @Insert
    fun insertPrediction(prediction: Prediction)

    @Query("SELECT * FROM predictions")
    fun getAllPredictions(): List<Prediction>

    @Query("SELECT * FROM predictions WHERE isSynced = 0")
    fun getUnsyncedPredictions(): List<Prediction>

    @Update
    fun updatePrediction(prediction: Prediction)
}
