package com.maranatha.foodlergic.data.models

data class DeleteAlergyRequest (
    val userId: String,
    val allergyIds : List<String>
)