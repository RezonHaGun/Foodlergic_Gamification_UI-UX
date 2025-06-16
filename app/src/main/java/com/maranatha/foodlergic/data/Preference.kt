package com.maranatha.foodlergic.data

import android.content.Context
import android.content.SharedPreferences
import com.maranatha.foodlergic.domain.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Preference @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    // Define keys for SharedPreferences
    companion object {
        private const val PREFERENCE_NAME = "UserPreferences"
        private const val KEY_USERNAME = "username"
        private const val KEY_ALLERGIES = "allergies"
        private const val KEY_USER_ID = "userId"
        private const val KEY_EMAIL = "email"
        private const val KEY_LOGGED_IN = "loggedIn"
        private const val KEY_PROFILE_IMAGE_URI = "profileImageUri"
        private const val KEY_IS_ALLERGIES_SUBMITTED = "is_allergies_submitted"
    }

    // Save user session
    fun saveUserSession(user: User) {
        sharedPreferences.edit().putString(KEY_USER_ID, user.id).apply()
        sharedPreferences.edit().putString(KEY_USERNAME, user.username).apply()
        sharedPreferences.edit().putString(KEY_EMAIL, user.email).apply()
        sharedPreferences.edit().putBoolean(KEY_LOGGED_IN, true).apply()
        sharedPreferences.edit().putBoolean(KEY_IS_ALLERGIES_SUBMITTED, true).apply()
    }


    // Save username
    fun saveUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    // Retrieve username
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    // Save email
    fun saveEmail(email: String) {
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply()
    }

    // Retrieve email
    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }


    // Save allergies
    fun saveAllergies(allergies: List<String>) {
        sharedPreferences.edit().putStringSet(KEY_ALLERGIES, allergies.toSet()).apply()
    }

    // Retrieve allergies
    fun getAllergies(): List<String> {
        return sharedPreferences.getStringSet(KEY_ALLERGIES, emptySet())?.toList() ?: emptyList()
    }

    // Save user ID
    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply()
    }

    // Retrieve user ID
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    fun isAnonymous(): Boolean {
        return !getIsLoggedIn()
    }

    // Retrieve Logged In User State
    fun getIsLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false)
    }

    fun saveProfileImageUri(uri: String) {
        sharedPreferences.edit().putString(KEY_PROFILE_IMAGE_URI, uri).apply()
    }

    fun getProfileImageUri(): String? {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE_URI, null)
    }


    // Clear all saved preferences
    fun clearUserSession() {
        sharedPreferences.edit().clear().apply()
    }
}
