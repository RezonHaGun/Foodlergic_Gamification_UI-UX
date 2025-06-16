package com.maranatha.foodlergic.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maranatha.foodlergic.presentation.leaderboard.LeaderboardItem
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val _leaderboardItems = MutableLiveData<Resource<List<LeaderboardItem>>>()
    val leaderboardItems: LiveData<Resource<List<LeaderboardItem>>> = _leaderboardItems

    fun fetchLeaderboard() {
        viewModelScope.launch {
            _leaderboardItems.value = Resource.Loading()
            try {
                val documents = firestore.collection("leaderboard")
                    .orderBy(
                        "point",
                        Query.Direction.DESCENDING
                    )
                    .get()
                    .await()

                val leaderboard = mutableListOf<LeaderboardItem>()
                var rank = 1
                for (document in documents) {
                    val userName = document.getString("name") ?: "Anonymous"
                    val point = document.getLong("point") ?: 0L
                    val userId =  document.getString("userId") ?: ""
                    val level = document.getString("level") ?: "Rookie I"
                    leaderboard.add(
                        LeaderboardItem(
                            userId = userId,
                            playerName = userName,
                            score = point.toInt(),
                            rank = rank,
                            level = level
                        )
                    )
                    rank++
                }

                _leaderboardItems.value = Resource.Success(leaderboard)
            } catch (e: Exception) {
                _leaderboardItems.value =
                    Resource.Error("Error fetching leaderboard: ${e.message}")
            }
        }
    }


    fun submitLeaderboardItem(leaderboardItem: LeaderboardItem) {
        viewModelScope.launch {
            try {
                val leaderboardRef = firestore.collection("leaderboard")
                val newItem = leaderboardRef.add(leaderboardItem).await()
                _leaderboardItems.value =
                    Resource.Success(listOf(leaderboardItem))  // Update UI with new data
            } catch (e: Exception) {
                _leaderboardItems.value =
                    Resource.Error("Failed to submit leaderboard item: ${e.message}")
            }
        }
    }

    private val _friendLeaderboard = MutableLiveData<Resource<List<LeaderboardItem>>>()
    val friendLeaderboard: LiveData<Resource<List<LeaderboardItem>>> = _friendLeaderboard

    fun loadFriendLeaderboard() {
        _friendLeaderboard.value = Resource.Loading()
        viewModelScope.launch {
            val result = getFriendsLeaderboard()
            _friendLeaderboard.value = result
        }
    }

    private suspend fun getFriendsLeaderboard(): Resource<List<LeaderboardItem>> {
        val userId = firebaseAuth.currentUser?.uid.orEmpty()
        return try {
            // 1. Ambil daftar teman user
            val friendsSnapshot = firestore.collection("users")
                .document(userId)
                .collection("friends")
                .get()
                .await()

            val friendIds = friendsSnapshot.documents.map { it.id }

            if (friendIds.isEmpty()) {
                return Resource.Success(emptyList())
            }

            val batchSize = 20
            val batches = friendIds.chunked(batchSize)

            val allResults = mutableListOf<LeaderboardItem>()

            for (batch in batches) {
                val querySnapshot = firestore.collection("leaderboard")
                    .whereIn("userId", batch)
                    .get()
                    .await()

                val batchResults = querySnapshot.documents.map { doc ->
                    LeaderboardItem(
                        userId = doc.getString("userId") ?: "",
                        playerName = doc.getString("name") ?: "",
                        score = doc.getLong("point")?.toInt() ?: 0,
                        rank = 0,
                        level = doc.getString("level") ?: "Rookie I"
                    )
                }
                allResults.addAll(batchResults)
            }

            // 3. Urutkan berdasarkan score descending & kasih rank
            val sortedWithRank = allResults
                .sortedByDescending { it.score }
                .take(20)
                .mapIndexed { index, item ->
                    item.copy(rank = index + 1)
                }

            Resource.Success(sortedWithRank)

        } catch (e: Exception) {
            Resource.Error("Failed to fetch leaderboard: ${e.message}")
        }
    }


}
