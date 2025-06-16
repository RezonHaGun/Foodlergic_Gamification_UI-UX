package com.maranatha.foodlergic.domain.checker

import com.maranatha.foodlergic.domain.models.Achievement

interface AchievementChecker {
    suspend fun canUnlock(achievement: Achievement, userId: String): Boolean
}
