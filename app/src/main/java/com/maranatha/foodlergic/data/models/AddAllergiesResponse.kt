package com.maranatha.foodlergic.data.models

data class AddAllergyResponse(
    val message: String,
    val userId: String,
    val allergy: AllergyItem
)

data class AllergyItem(
    val id: String,
    val name: String
)
