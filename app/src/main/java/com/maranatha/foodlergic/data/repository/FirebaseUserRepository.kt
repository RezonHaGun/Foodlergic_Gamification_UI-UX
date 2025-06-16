package com.maranatha.foodlergic.data.repository

import android.icu.util.Calendar
import android.icu.util.TimeZone
import com.google.firebase.Timestamp
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maranatha.foodlergic.data.Preference
import com.maranatha.foodlergic.domain.models.Predict
import com.maranatha.foodlergic.domain.models.User
import com.maranatha.foodlergic.domain.models.WarningFood
import com.maranatha.foodlergic.domain.repository.UserRepository
import com.maranatha.foodlergic.utils.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val sharedPreference: Preference
) : UserRepository {
    override suspend fun getUserAchievements(): Map<String, Boolean> {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val doc = firestore.collection("users").document(userId).get().await()
        return doc.get("unlockedAchievements") as? Map<String, Boolean> ?: mapOf()
    }

    override suspend fun getUserRewards(): Map<String, Boolean> {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val doc = firestore.collection("users").document(userId).get().await()
        return doc.get("unlockedRewards") as? Map<String, Boolean> ?: mapOf()
    }

    override suspend fun getFriendAchievements(userId: String): Map<String, Boolean> {
        val doc = firestore.collection("users").document(userId).get().await()
        return doc.get("unlockedAchievements") as? Map<String, Boolean> ?: mapOf()
    }

    override suspend fun updateUserAchievements(
        unlocked: Map<String, Boolean>
    ) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val data = mapOf(
            "unlockedAchievements" to unlocked
        )
        firestore.collection("users").document(userId).update(data).await()
    }

    override suspend fun updateUserRewards(
        unlocked: Map<String, Boolean>
    ) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val data = mapOf(
            "unlockedRewards" to unlocked
        )
        firestore.collection("users").document(userId).update(data).await()
    }

    override suspend fun updateScanCount(userId: String, newScanCount: Int) {
        val userRef = firestore.collection("users").document(userId)
        userRef.update("scanCount", newScanCount).await()
    }

    override suspend fun getScanCount(userId: String): Int {
        val userRef = firestore.collection("users").document(userId)
        val snapshot = userRef.get().await()
        return snapshot.getLong("scanCount")?.toInt() ?: 0
    }

    override suspend fun getScanPoint(userId: String): Int {
        val userRef = firestore.collection("users").document(userId)
        val snapshot = userRef.get().await()
        return snapshot.getLong("point")?.toInt() ?: 0
    }

    override suspend fun getSafeScanCount(userId: String): Int {
        val userRef = firestore.collection("users").document(userId)
        val snapshot = userRef.get().await()
        return snapshot.getLong("safeScanCount")?.toInt() ?: 0
    }
    override suspend fun getTodayScanCount(userId: String): Int {
        val startOfDay = getStartOfTodayTimestamp()
        val predictions = firestore.collection("users")
            .document(userId)
            .collection("predictions")
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .get()
            .await()

        return predictions.size()
    }
    private fun getStartOfTodayTimestamp(): Timestamp {
        val now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        return Timestamp(now.time)
    }

    override suspend fun getCurrentUser(): User {
        val userId = auth.currentUser?.uid.orEmpty()
        val userRef = firestore.collection("users").document(userId)
        val snapshot = userRef.get().await()
        val username = snapshot.getString("name") ?: ""
        val email = snapshot.getString("email") ?: ""
        val level = snapshot.getString("level") ?: ""
        val scanCount = snapshot.getLong("scanCount") ?: 0L
        val friendCode = snapshot.getString("friendCode") ?: "-"
        val point = snapshot.getLong("point") ?: 0L

        return User(
            id = userId,
            username = username,
            email = email,
            level = level,
            friendCode = friendCode,
            scanCount = scanCount.toInt(),
            point = point.toInt()
        )
    }

    override suspend fun getRecentPredictions(userId: String): List<Predict> {
        return try {

            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val allergies = userDoc.get("allergies") as? Map<String, Boolean> ?: emptyMap()

            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("predictions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(4)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val predictedAllergen = doc.getString("predicted_allergen")?.lowercase()
                val timestamp = doc.getTimestamp("timestamp")
                val foodName = doc.getString("foodName")

                if (predictedAllergen != null  && timestamp != null && foodName != null) {
                    val hasAllergy = allergies[predictedAllergen] == true
                    Predict(predictedAllergen, hasAllergy, timestamp, foodName)
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAllPredictions(userId: String): List<Predict> {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val allergies = userDoc.get("allergies") as? Map<String, Boolean> ?: emptyMap()

            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("predictions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val predictedAllergen = doc.getString("predicted_allergen")?.lowercase()
                val timestamp = doc.getTimestamp("timestamp")
                val foodName = doc.getString("foodName")

                if (predictedAllergen != null && timestamp != null && foodName != null) {
                    val hasAllergy = allergies[predictedAllergen] == true
                    Predict(predictedAllergen, hasAllergy, timestamp, foodName)
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getUserByFriendCode(friendCode: String): Resource<User> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Resource.Error("User not authenticated")
        return try {
            val friendCodeDoc = firestore.collection("friendCodes")
                .document(friendCode)
                .get()
                .await()

            if (!friendCodeDoc.exists()) {
                return Resource.Error("Friend code not found")
            }

            val uid = friendCodeDoc.getString("uid")
                ?: return Resource.Error("Invalid friend code data")

            if (uid == currentUserId) {
                return Resource.Error("Cannot add yourself as a friend")
            }

            val userDoc = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            if (!userDoc.exists()) {
                return Resource.Error("User not found")
            }

            val username = userDoc.getString("name") ?: ""
            val email = userDoc.getString("email") ?: ""
            val level = userDoc.getString("level") ?: ""
            val scanCount = userDoc.getLong("scanCount") ?: 0L
            val point = userDoc.getLong("point") ?: 0L

            val user = User(
                id = uid,
                username = username,
                email = email,
                level = level,
                friendCode = friendCode,
                scanCount = scanCount.toInt(),
                point = point.toInt()
            )
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error("Failed to get user: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun checkFoodAllergyRisk(uid: String): List<WarningFood> {
        val userDoc = firestore.collection("users").document(uid).get().await()
        val allergies = userDoc.get("allergies") as? Map<String, Boolean> ?: emptyMap()

        val predictionDocs = firestore.collection("users")
            .document(uid)
            .collection("predictions")
            .get()
            .await()

        val foodAllergyCount = mutableMapOf<String, Pair<Int, String>>()

        for (doc in predictionDocs) {
            val predictedAllergen = doc.getString("predicted_allergen")?.lowercase() ?: continue
            val foodName = doc.getString("foodName") ?: continue

            if (allergies[predictedAllergen] == true) {
                val (currentCount, _) = foodAllergyCount[foodName] ?: (0 to predictedAllergen)
                foodAllergyCount[foodName] = (currentCount + 1) to predictedAllergen
            }
        }

        return foodAllergyCount.filterValues { it.first >= 10 }.map { (foodName, pair) ->
            WarningFood(
                foodName = foodName,
                scanCount = pair.first,
                allergen = pair.second.replaceFirstChar { it.uppercaseChar() }
            )
        }
    }

    override suspend fun clearUser() {
        try {
            // Log out from Firebase
            auth.signOut()

            // Example: clearUserDataFromLocalStorage()
            sharedPreference.clearUserSession()
        } catch (e: Exception) {
            // Handle any error that may occur during logout
            throw Exception("Error while clearing user: ${e.message}")
        }
    }
}