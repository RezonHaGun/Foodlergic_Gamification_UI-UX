package com.maranatha.foodlergic.domain.usecase

import com.maranatha.foodlergic.domain.models.Book
import com.maranatha.foodlergic.domain.repository.RewardRepository
import com.maranatha.foodlergic.domain.repository.UserRepository
import javax.inject.Inject

class RewardUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val rewardRepository: RewardRepository,
) {
    suspend fun checkRewards(userId: String): List<Book> {
        return try {
            val unlockedMap = userRepository.getUserRewards()
            val allReward = rewardRepository.getAllReward()
            val point = userRepository.getScanPoint(userId)

            val newlyUnlocked = allReward.filter { book ->
                point >= book.threshold && unlockedMap[book.code] != true
            }

            if (newlyUnlocked.isNotEmpty()) {
                val updatedMap = unlockedMap.toMutableMap()
                newlyUnlocked.forEach { updatedMap[it.code] = true }
                userRepository.updateUserRewards(updatedMap)
            }

            newlyUnlocked
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

