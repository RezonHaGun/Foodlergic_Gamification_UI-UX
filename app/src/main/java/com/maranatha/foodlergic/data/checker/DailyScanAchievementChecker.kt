package com.maranatha.foodlergic.data.checker

import android.util.Log
import com.maranatha.foodlergic.domain.checker.AchievementChecker
import com.maranatha.foodlergic.domain.models.Achievement
import com.maranatha.foodlergic.domain.repository.UserRepository
import javax.inject.Inject

class DailyScanAchievementChecker @Inject constructor(
    private val userRepository: UserRepository
) : AchievementChecker {
    override suspend fun canUnlock(achievement: Achievement, userId: String): Boolean {
        val scanCount = userRepository.getTodayScanCount(userId)
        Log.d("rezon-dbg", "today scan count: $scanCount")
        return scanCount >= achievement.threshold
    }
}
