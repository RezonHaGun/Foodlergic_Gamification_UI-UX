package com.maranatha.foodlergic.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.maranatha.foodlergic.data.Preference
import com.maranatha.foodlergic.domain.models.User
import com.maranatha.foodlergic.domain.repository.IAuthRepository
import com.maranatha.foodlergic.utils.FriendCodeGenerator
import com.maranatha.foodlergic.utils.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val sessionManager: Preference
) : IAuthRepository {
    override suspend fun login(email: String, password: String): Resource<User> {
        return runCatching {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Login failed")

            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
            val isAllergySubmitted = snapshot.getBoolean("isAllergySubmitted") ?: false
            val name = snapshot.getString("name") ?: "-"
            val friendCode = snapshot.getString("friendCode") ?: "-"

            val user = User(
                id = firebaseUser.uid,
                username = name,
                email = email,
                friendCode = friendCode,
                isAllergySubmitted = isAllergySubmitted
            )
            sessionManager.saveUserSession(user)

            user
        }.fold(
            onSuccess = { user -> Resource.Success(user) },
            onFailure = { error -> Resource.Error("An error occurred: ${error.message}") }
        )
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Resource<Boolean> {
        return runCatching {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Registration failed")

            val allergies = mapOf(
                "ikan" to false,
                "udang" to false,
                "kepiting" to false,
                "kerang" to false
            )

            val uid = firebaseUser.uid

            val userData = mapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "scanCount" to 0,
                "scanStreak" to 0,
                "safeScanCount" to 0,
                "point" to 0,
                "lastScanDate" to com.google.firebase.Timestamp.now(),
                "unlockedAchievements" to emptyMap<String, Boolean>(),
//                "level" to "Rookie",
                "allergies" to allergies,
                "isAllergySubmitted" to false,
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(userData)
                .await()

            val friendCode = generateAndSaveFriendCode(uid)

            val user = User(firebaseUser.uid, name, email, friendCode = friendCode)
            sessionManager.saveUserSession(user)
            true
        }.fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { error -> Resource.Error("An error occurred: ${error.message}") }
        )
    }

    private suspend fun generateAndSaveFriendCode(uid: String): String {
        val friendCodesRef = firestore.collection("friendCodes")

        var code: String
        var doc: DocumentSnapshot

        do {
            code = FriendCodeGenerator.generate()
            doc = friendCodesRef.document(code).get().await()
        } while (doc.exists())

        // Simpan ke friendCodes/{code}
        friendCodesRef.document(code).set(mapOf("uid" to uid)).await()

        // Simpan juga ke users/{uid}
        firestore.collection("users").document(uid).update("friendCode", code).await()

        return code
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun logout() {
        firebaseAuth.signOut()
        sessionManager.clearUserSession()
    }
}