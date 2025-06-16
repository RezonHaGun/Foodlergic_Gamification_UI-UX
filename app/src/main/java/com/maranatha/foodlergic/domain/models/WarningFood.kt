package com.maranatha.foodlergic.domain.models

data class WarningFood(
    val foodName: String,
    val scanCount: Int,
    val allergen: String
)
