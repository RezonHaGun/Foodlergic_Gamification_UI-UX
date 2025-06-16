package com.maranatha.foodlergic.domain.models

data class FriendRequest(
    val id: String,
    val fromUserId: String,
    val fromUserName: String,
    val fromFriendCode: String,
    val toUserId: String,
    val status: String,
    val createdAt: Long
)
