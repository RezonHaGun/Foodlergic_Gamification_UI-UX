package com.maranatha.foodlergic.domain.models

data class Achievement(
    val code: String,
    val name: String,
    val description: String,
    val type: String,
    val threshold: Int,
    var isUnlocked: Boolean = false,
    val unlockedImage: String,
    val lockedImage: String
)

