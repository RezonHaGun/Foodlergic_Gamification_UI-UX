package com.maranatha.foodlergic.data.models

data class LoginResponse(
    val message: String,
    val user: UserResponse
)