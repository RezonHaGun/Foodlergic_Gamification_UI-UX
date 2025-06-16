package com.maranatha.foodlergic.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.maranatha.foodlergic.domain.models.User
import com.maranatha.foodlergic.domain.repository.IUserProfileRepository
import javax.inject.Inject

class FirebaseUserProfileRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
//    private val firestore: FirebaseStorage,
//    private val imageRepository: IImageRepository
) : IUserProfileRepository {
    override fun getUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun getUserData(): User {
        TODO("Not yet implemented")
    }

    override fun saveUserData(user: User) {
        TODO("Not yet implemented")
    }

    override fun clearUser() {
        TODO("Not yet implemented")
    }

//    fun getUserProfile(onResult: (UserProfile?) -> Unit, onFailure: (Exception) -> Unit) {
//        val userId =
//            firebaseAuth.currentUser?.uid ?: return onFailure(Exception("User not logged in"))
//
//        firestore.collection("users").document(userId).get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val username = document.getString("username") ?: "Unknown User"
//                    val imageUrl = document.getString("imageUrl")
//                    val userProfile = UserProfile(userId, username, imageUrl)
//                    onResult(userProfile)
//                } else {
//                    onFailure(Exception("User data not found"))
//                }
//            }
//            .addOnFailureListener { exception ->
//                onFailure(exception)
//            }
//    }


    override fun uploadProfileImage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId() ?: return
        val filePath = "profile_images/$userId.jpg"
//        imageRepository.uploadImage(filePath, imageUri, onSuccess, onFailure)
    }

}