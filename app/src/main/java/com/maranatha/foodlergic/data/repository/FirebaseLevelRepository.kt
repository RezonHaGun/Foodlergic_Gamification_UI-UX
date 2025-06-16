package com.maranatha.foodlergic.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.maranatha.foodlergic.domain.models.LevelThreshold
import com.maranatha.foodlergic.domain.repository.LevelRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseLevelRepository @Inject constructor(private val firestore: FirebaseFirestore) : LevelRepository {

    override suspend fun fetchLevels(): List<LevelThreshold> {
        val snapshot = firestore.collection("levels")
            .orderBy("minScanCount")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            LevelThreshold(
                name = doc.getString("name") ?: "",
                minScanCount = doc.getLong("minScanCount")?.toInt() ?: 0,
                maxScanCount = doc.getLong("maxScanCount")?.toInt() ?: Int.MAX_VALUE
            )
        }
    }
}
