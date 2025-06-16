package com.maranatha.foodlergic.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.maranatha.foodlergic.domain.models.Achievement
import com.maranatha.foodlergic.domain.repository.AchievementRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAchievementRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : AchievementRepository{

    override suspend fun getAllAchievements(): List<Achievement> {
        return try {
            val result = firestore.collection("Achievement").get().await()

            val achievements = mutableListOf<Achievement>()

            for (document in result) {
                val name = document.getString("name") ?: ""
                val description = document.getString("description") ?: ""
                val type = document.getString("type") ?: ""
                val threshold = if (document.get("threshold") != null) {
                    document.getLong("threshold")?.toInt() ?: 0
                } else {
                    0
                }
                val unlockedImage = document.getString("unlocked_image") ?: ""
                val lockedImage = document.getString("locked_image") ?: ""

                val code = document.id


                Log.d("AchievementDebug", "Fetched: $name, code: $code")

                achievements.add(
                    Achievement(
                        name = name,
                        description = description,
                        type = type,
                        threshold = threshold,
                        unlockedImage = unlockedImage,
                        lockedImage = lockedImage,
                        code = code
                    )
                )
            }

            achievements
        } catch (e: Exception) {
            Log.e("AchievementDebug", "Failed to parse document ${e.toString()}")
            emptyList()
        }

    }
}