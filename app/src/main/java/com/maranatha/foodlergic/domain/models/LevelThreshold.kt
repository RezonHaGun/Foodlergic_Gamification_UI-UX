package com.maranatha.foodlergic.domain.models

data class LevelThreshold(
    val name: String,
    val minScanCount: Int,
    val maxScanCount: Int
)

