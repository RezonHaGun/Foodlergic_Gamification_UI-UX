package com.maranatha.foodlergic.domain.repository

import android.net.Uri
import com.maranatha.foodlergic.domain.models.User

interface IUserProfileRepository {
    fun getUserId(): String?
    fun getUserData(): User
    fun saveUserData(user: User)
    fun clearUser()
    fun uploadProfileImage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    )
}