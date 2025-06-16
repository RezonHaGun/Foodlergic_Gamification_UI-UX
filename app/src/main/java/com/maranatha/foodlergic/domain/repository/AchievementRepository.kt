package com.maranatha.foodlergic.domain.repository

import com.maranatha.foodlergic.domain.models.Achievement

interface AchievementRepository {
    suspend fun getAllAchievements(): List<Achievement>
    // method lainnya...
}
