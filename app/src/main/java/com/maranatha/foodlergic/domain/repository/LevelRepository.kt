package com.maranatha.foodlergic.domain.repository

import com.maranatha.foodlergic.domain.models.LevelThreshold

interface LevelRepository {
    suspend fun fetchLevels(): List<LevelThreshold>
}