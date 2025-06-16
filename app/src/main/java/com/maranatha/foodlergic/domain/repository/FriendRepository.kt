package com.maranatha.foodlergic.domain.repository

import com.maranatha.foodlergic.domain.models.FriendRequest
import com.maranatha.foodlergic.utils.Resource

interface FriendRepository {
    suspend fun sendFriendRequest(friendCode: String): Resource<Unit>
    suspend fun getReceivedFriendRequests(): Resource<List<FriendRequest>>
    suspend fun acceptFriendRequest(request: FriendRequest):Resource<Unit>
    suspend fun rejectFriendRequest(request: FriendRequest):Resource<Unit>
}