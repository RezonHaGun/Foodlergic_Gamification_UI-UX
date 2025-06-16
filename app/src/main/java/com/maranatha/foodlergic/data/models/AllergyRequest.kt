package com.maranatha.foodlergic.data.models

data class AllergyRequest(
    val userId: String,
    val allergies : List<String>
)
