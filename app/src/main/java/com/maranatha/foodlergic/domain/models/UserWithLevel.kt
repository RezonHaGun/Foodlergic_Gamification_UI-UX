package com.maranatha.foodlergic.domain.models

data class UserWithLevel(
    val user: User,
    val allergies: List<String>,
    val levelInfo: LevelThreshold,
    val friendCount: Int
)
