package com.maranatha.foodlergic.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.maranatha.foodlergic.domain.models.Book
import com.maranatha.foodlergic.domain.repository.RewardRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRewardRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : RewardRepository {

    override suspend fun getAllReward(): List<Book> {
        return try {
            val result = firestore.collection("Books").get().await()

            val books = mutableListOf<Book>()

            for (document in result) {
                val code = document.getString("code") ?: ""
                val image = document.getString("image") ?: ""
                val name = document.getString("name") ?: ""
                val summary = document.getString("summary") ?: ""
                val threshold = document.getLong("threshold")?.toInt() ?: 0
                val urlBook = document.getString("urlBook") ?: ""


                books.add(
                    Book(
                        name = name,
                        image = image,
                        code = code,
                        urlBook = urlBook,
                        summary = summary,
                        threshold = threshold,
                    )
                )
            }
            books
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

    }
}