package com.maranatha.foodlergic.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.maranatha.foodlergic.domain.models.FriendRequest
import com.maranatha.foodlergic.domain.repository.FriendRepository
import com.maranatha.foodlergic.utils.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFriendRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : FriendRepository {
    override suspend fun sendFriendRequest(friendCode: String): Resource<Unit> {
        val currentUser = firebaseAuth.currentUser ?: return Resource.Error("User not logged in")
        val currentUid = currentUser.uid

        return try {
            val friendDoc = firestore.collection("friendCodes").document(friendCode).get().await()
            val friendUid =
                friendDoc.getString("uid") ?: return Resource.Error("Friend code not found")

            if (friendUid == currentUid) {
                return Resource.Error("You cannot add yourself")
            }

            val now = FieldValue.serverTimestamp()

            val currentUserDoc = firestore.collection("users").document(currentUid).get().await()
            val fromName = currentUserDoc.getString("name") ?: "Unknown"
            val fromFriendCode = currentUserDoc.getString("friendCode") ?: "Unknown"

            val sentRef = firestore.collection("users").document(currentUid)
                .collection("sentRequests").document(friendUid)

            val receivedRef = firestore.collection("users").document(friendUid)
                .collection("receivedRequests").document(currentUid)

            val requestData = mapOf(
                "from" to currentUid,
                "fromName" to fromName,
                "fromFriendCode" to fromFriendCode,
                "to" to friendUid,
                "status" to "pending",
                "timestamp" to now
            )

            firestore.runBatch { batch ->
                batch.set(sentRef, requestData)
                batch.set(receivedRef, requestData)
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message.orEmpty())
        }
    }

    override suspend fun getReceivedFriendRequests(): Resource<List<FriendRequest>> {
        val currentUser = firebaseAuth.currentUser ?: return Resource.Error("User not logged in")
        val currentUid = currentUser.uid

        try {
            Log.d("FriendRepo", "Fetching received friend requests for userId: $currentUid")

            val snapshot = firestore.collection("users")
                .document(currentUid)
                .collection("receivedRequests")
                .whereEqualTo("status", "pending")
                .get()
                .await()

            Log.d("FriendRepo", "Received ${snapshot.size()} requests")

            val requests = snapshot.documents.map { doc ->
                val from = doc.getString("from") ?: ""
                val fromName = doc.getString("fromName") ?: ""
                val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                val fromFriendCode = doc.getString("fromFriendCode") ?: ""

                Log.d("FriendRepo", "Request from: $from | Name: $fromName | Timestamp: $timestamp")

                FriendRequest(
                    id = doc.id,
                    fromUserId = from,
                    fromUserName = fromName,
                    fromFriendCode = fromFriendCode,
                    toUserId = currentUid,
                    status = doc.getString("status") ?: "pending",
                    createdAt = timestamp
                )
            }

            return Resource.Success(requests)
        } catch (e: Exception) {
            Log.e("FriendRepo", "Error fetching received requests: ${e.message}", e)
            return Resource.Error("Failed to get friend request: ${e.message}")
        }
    }


    override suspend fun acceptFriendRequest(request: FriendRequest): Resource<Unit> {
        val currentUser = firebaseAuth.currentUser ?: return Resource.Error("User not logged in")
        val currentUid = currentUser.uid

        if (currentUid != request.toUserId) {
            return Resource.Error("Unauthorized to accept this request")
        }

        return try {
            val now = FieldValue.serverTimestamp()

            val currentUserDoc = firestore.collection("users").document(currentUid).get().await()
            val currentUserName = currentUserDoc.getString("name") ?: "Unknown"

            val sentRef = firestore.collection("users")
                .document(request.fromUserId)
                .collection("sentRequests")
                .document(currentUid)

            val receivedRef = firestore.collection("users")
                .document(currentUid)
                .collection("receivedRequests")
                .document(request.fromUserId)

            firestore.runBatch { batch ->
                batch.update(sentRef, mapOf("status" to "accepted", "timestamp" to now))
                batch.update(receivedRef, mapOf("status" to "accepted", "timestamp" to now))

                // Optional: tambah teman di koleksi 'friends'
                val friendData = mapOf(
                    "friendId" to request.fromUserId,
                    "friendName" to request.fromUserName,
                    "createdAt" to now
                )
                val currentUserFriendRef = firestore.collection("users").document(currentUid).collection("friends").document(request.fromUserId)
                val otherUserFriendRef = firestore.collection("users").document(request.fromUserId).collection("friends").document(currentUid)

                batch.set(currentUserFriendRef, friendData)
                batch.set(otherUserFriendRef, mapOf(
                    "friendId" to currentUid,
                    "friendName" to currentUserName,
                    "createdAt" to now
                ))
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to accept friend request: ${e.message}")
        }
    }

    override suspend fun rejectFriendRequest(request: FriendRequest): Resource<Unit> {
        val currentUser = firebaseAuth.currentUser ?: return Resource.Error("User not logged in")
        val currentUid = currentUser.uid

        if (currentUid != request.toUserId) {
            return Resource.Error("Unauthorized to deny this request")
        }

        return try {
            val sentRef = firestore.collection("users")
                .document(request.fromUserId)
                .collection("sentRequests")
                .document(currentUid)

            val receivedRef = firestore.collection("users")
                .document(currentUid)
                .collection("receivedRequests")
                .document(request.fromUserId)

            firestore.runBatch { batch ->
                batch.delete(sentRef)
                batch.delete(receivedRef)
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to deny friend request: ${e.message}")
        }
    }

}