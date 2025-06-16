package com.maranatha.foodlergic.data.checker

import com.maranatha.foodlergic.domain.checker.AchievementChecker
import com.maranatha.foodlergic.domain.models.Achievement
import com.maranatha.foodlergic.domain.repository.UserRepository
import javax.inject.Inject

class ScanPointAchievementChecker @Inject constructor(
    private val userRepository: UserRepository
) : AchievementChecker {
    override suspend fun canUnlock(achievement: Achievement, userId: String): Boolean {
        val point = userRepository.getScanPoint(userId)
        return point >= achievement.threshold
    }
}