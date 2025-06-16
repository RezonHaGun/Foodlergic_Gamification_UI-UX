package com.maranatha.foodlergic.domain.repository

import com.maranatha.foodlergic.domain.models.FriendRequest
import com.maranatha.foodlergic.domain.models.Predict
import com.maranatha.foodlergic.domain.models.User
import com.maranatha.foodlergic.domain.models.WarningFood
import com.maranatha.foodlergic.utils.Resource

interface UserRepository {
    suspend fun getUserAchievements(): Map<String, Boolean>
    suspend fun getUserRewards(): Map<String, Boolean>
    suspend fun getFriendAchievements(userId: String): Map<String, Boolean>

    suspend fun updateUserAchievements(
        unlocked: Map<String, Boolean>
    )

    suspend fun updateUserRewards(
        unlocked: Map<String, Boolean>
    )

    suspend fun updateScanCount(userId: String, newScanCount: Int)

    suspend fun getScanCount(userId: String): Int
    suspend fun getScanPoint(userId: String): Int
    suspend fun getSafeScanCount(userId: String): Int

    suspend fun getTodayScanCount(userId: String): Int
    suspend fun getUserByFriendCode(code: String): Resource<User>

    suspend fun getCurrentUser(): User
    suspend fun getRecentPredictions(userId: String): List<Predict>
    suspend fun getAllPredictions(userId: String): List<Predict>
    suspend fun checkFoodAllergyRisk(uid: String): List<WarningFood>
    suspend fun clearUser()

}