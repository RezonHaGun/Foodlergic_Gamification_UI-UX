package com.maranatha.foodlergic.domain.usecase

import com.maranatha.foodlergic.data.checker.DailyScanAchievementChecker
import com.maranatha.foodlergic.data.checker.SafeScanAchievementChecker
import com.maranatha.foodlergic.data.checker.ScanPointAchievementChecker
import com.maranatha.foodlergic.domain.checker.AchievementChecker
import com.maranatha.foodlergic.domain.models.Achievement
import com.maranatha.foodlergic.domain.repository.AchievementRepository
import com.maranatha.foodlergic.domain.repository.UserRepository
import javax.inject.Inject

class AchievementUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository,
    private val scanPointChecker: ScanPointAchievementChecker,
    private val safeScanChecker: SafeScanAchievementChecker,
    private val dailyScanChecker: DailyScanAchievementChecker
) {

    private val checkers: Map<String, AchievementChecker> by lazy {
        mapOf(
            "scan" to scanPointChecker,
            "safeScan" to safeScanChecker,
            "scanDay" to dailyScanChecker
        )
    }
    suspend fun checkAchievements(userId: String): List<Achievement> {
        return try {
            val unlockedMap = userRepository.getUserAchievements()
            val allAchievements = achievementRepository.getAllAchievements()

            val newlyUnlocked = allAchievements.filter { achievement ->
                val checker = checkers[achievement.type]
                checker?.canUnlock(achievement, userId) == true &&
                        unlockedMap[achievement.code] != true
            }

            if (newlyUnlocked.isNotEmpty()) {
                val updatedMap = unlockedMap.toMutableMap()
                newlyUnlocked.forEach { updatedMap[it.code] = true }
                userRepository.updateUserAchievements(updatedMap)
            }

            newlyUnlocked
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

