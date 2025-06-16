package com.maranatha.foodlergic.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.annotation.Nonnull

@Entity(tableName = "predictions")
data class Prediction(
    @PrimaryKey(autoGenerate = true) @Nonnull val id: Long = 0L,
    @ColumnInfo(name = "predictedAllergen") val allergen: String,
    @ColumnInfo(name = "hasAllergy") val hasAllergy: Boolean,
    @ColumnInfo(name = "foodName") val foodName : String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "isSynced") val isSynced: Boolean = false
)

