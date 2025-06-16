package com.maranatha.foodlergic.data.models

data class Allergy(
    val name: String,
    val image: Int = 0,
    var isSelected: Boolean = false,
)