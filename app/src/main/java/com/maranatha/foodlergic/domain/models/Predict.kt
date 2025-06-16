package com.maranatha.foodlergic.domain.models

data class Predict(
    val predictedAllergen: String,
    val hasAllergy: Boolean,
    val timestamp: com.google.firebase.Timestamp,
    val foodName: String
)

