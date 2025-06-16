package com.maranatha.foodlergic.domain.repository

import com.maranatha.foodlergic.domain.models.User
import com.maranatha.foodlergic.utils.Resource

interface IAuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun register(name: String, email: String, password: String): Resource<Boolean>
    fun isUserLoggedIn(): Boolean
    fun logout()
}