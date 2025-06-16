package com.maranatha.foodlergic.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.maranatha.foodlergic.domain.models.Achievement
import com.maranatha.foodlergic.domain.models.LevelThreshold
import com.maranatha.foodlergic.domain.models.Predict
import com.maranatha.foodlergic.domain.models.User
import com.maranatha.foodlergic.domain.models.UserWithLevel
import com.maranatha.foodlergic.domain.models.WarningFood
import com.maranatha.foodlergic.domain.repository.AchievementRepository
import com.maranatha.foodlergic.domain.repository.LevelRepository
import com.maranatha.foodlergic.domain.repository.UserRepository
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepo: UserRepository,
    private val achievementRepository: AchievementRepository,
    private val levelRepository: LevelRepository
) : ViewModel() {

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData


    // Fungsi untuk mengambil data pengguna
    fun getUserData() {
        viewModelScope.launch {
            try {
                val user = userRepo.getCurrentUser()
                _userData.value = user
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Failed to get user data", e)
                _userData.value = User(
                    id = "",
                    email = "",
                    username = "guest",
                    friendCode = ""
                )
            }
        }
    }
    private val _warningFoods = MutableLiveData<List<WarningFood>>()
    val warningFoods: LiveData<List<WarningFood>> = _warningFoods

    fun loadFoodWarnings() {
        viewModelScope.launch {
            try {
                val user = userRepo.getCurrentUser()
                val warningsList = userRepo.checkFoodAllergyRisk(user.id)

                _warningFoods.value = warningsList
            } catch (e: Exception) {
                Log.e("PredictionViewModel", "Error checking food allergy risk", e)
                _warningFoods.value = emptyList()
            }
        }
    }

    private val _userDataWithLevel = MutableLiveData<Resource<UserWithLevel>>()
    val userDataWithLevel: LiveData<Resource<UserWithLevel>> = _userDataWithLevel

    fun getUserWithLevel() {
        viewModelScope.launch {
            _userDataWithLevel.value = Resource.Loading()
            try {
                val user = userRepo.getCurrentUser()
                val levels = levelRepository.fetchLevels()

                val levelInfo = levels.find { level ->
                    user.point >= level.minScanCount && user.point < level.maxScanCount
                } ?: levels.first()

                val friendSnapshot = firestore.collection("users")
                    .document(user.id)
                    .collection("friends")
                    .get()
                    .await()

                val userDoc = firestore.collection("users")
                    .document(user.id)
                    .get()
                    .await()

                val rawAllergies = userDoc.get("allergies") as? Map<String, Boolean> ?: emptyMap()

                val allergies = rawAllergies
                    .filterValues { it }
                    .keys
                    .map { it.replaceFirstChar { c -> c.uppercase() } }

                val friendCount = friendSnapshot.size()
                val userWithLevel = UserWithLevel(user, allergies, levelInfo, friendCount)

                _userDataWithLevel.value = Resource.Success(userWithLevel)
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Failed to get user data", e)
                _userDataWithLevel.value = Resource.Error("Failed to load user data: ${e.message}")
            }
        }
    }
    private val _userAchievements = MutableLiveData<Resource<List<Achievement>>>()
    val userAchievements: LiveData<Resource<List<Achievement>>> = _userAchievements

    fun getAchievement() {
        viewModelScope.launch {
            try {
                val achievements = achievementRepository.getAllAchievements()
                Log.d("AchievementLog", "All achievements: $achievements")

                val unlockedMap =
                    userRepo.getUserAchievements() as? Map<String, Boolean> ?: mapOf()
                Log.d("AchievementLog", "Unlocked map: $unlockedMap")

                val combinedAchievements = achievements.map {
                    it.copy(isUnlocked = unlockedMap[it.code] == true)
                }

                Log.d("AchievementLog", "Mapped achievements: $")

                _userAchievements.value = Resource.Success(combinedAchievements)
            } catch (e: Exception) {
                // Tangani error jika gagal mendapatkan data
                _userAchievements.value = Resource.Error("Failed to get user achievements")
            }
        }
    }

    private val _newlyUnlockedWelcome = MutableLiveData<Achievement?>()
    val newlyUnlockedWelcome: LiveData<Achievement?> = _newlyUnlockedWelcome

    fun checkWelcomeAchievement() {
        viewModelScope.launch {
            try {
                val unlockedMap = userRepo.getUserAchievements()
                val allAchievements = achievementRepository.getAllAchievements()
                val welcomeAchievement = allAchievements.first { it.type == "welcome" }
                val alreadyUnlocked = unlockedMap[welcomeAchievement.code] == true

                if (!alreadyUnlocked) {
                    userRepo.updateUserAchievements(unlockedMap + (welcomeAchievement.code to true))
                    _newlyUnlockedWelcome.value = welcomeAchievement
                } else {
                    _newlyUnlockedWelcome.value = null
                }
            } catch (e: Exception) {
                _newlyUnlockedWelcome.value = null
            }
        }
    }

    private val _combineUserAchievements = MutableLiveData<Resource<List<Achievement>>>()
    val combineUserAchievements: LiveData<Resource<List<Achievement>>> = _combineUserAchievements

    fun fetchAndCombineAchievements() {
        viewModelScope.launch {
            _combineUserAchievements.value = Resource.Loading()

            try {
                val unlockedMap = userRepo.getUserAchievements()
                val achievements = achievementRepository.getAllAchievements()

                // Tandai mana yang unlocked
                val combinedAchievements = achievements.map {
                    it.copy(isUnlocked = unlockedMap[it.code] == true)
                }

                _combineUserAchievements.value = Resource.Success(combinedAchievements)

            } catch (e: Exception) {
                _combineUserAchievements.value =
                    Resource.Error("Failed to get user achievements: ${e.message}")
            }
        }
    }

    private val _combineFriendAchievements = MutableLiveData<Resource<List<Achievement>>>()
    val combineFriendAchievements: LiveData<Resource<List<Achievement>>> = _combineFriendAchievements

    fun fetchAndCombineFriendAchievements(userId: String) {
        viewModelScope.launch {
            _combineFriendAchievements.value = Resource.Loading()

            try {
                val unlockedMap = userRepo.getFriendAchievements(userId)
                val achievements = achievementRepository.getAllAchievements()

                Log.d("rezon-dbg", "unlocked map: $unlockedMap")

                Log.d("rezon-dbg", "achievements: $achievements")
                // Tandai mana yang unlocked
                val combinedAchievements = achievements.map {
                    it.copy(isUnlocked = unlockedMap[it.code] == true)
                }

                _combineFriendAchievements.value = Resource.Success(combinedAchievements)

            } catch (e: Exception) {
                _combineFriendAchievements.value =
                    Resource.Error("Failed to get user achievements: ${e.message}")
            }
        }
    }


    private val _setAchievement = MutableLiveData<Resource<Boolean>>()
    val setAchievement: LiveData<Resource<Boolean>> = _setAchievement

    // Fungsi untuk memperbarui status pencapaian user
    fun setAchievement(achievement: Achievement) {
        viewModelScope.launch {
            try {
                val data = mapOf(
                    achievement.code to true
                )
                userRepo.updateUserAchievements(data)

                _setAchievement.value = Resource.Success(true)
            } catch (e: Exception) {
                _setAchievement.value = Resource.Error("Failed to set user achievements")
            }
        }
    }

    private val _recentPredictions = MutableLiveData<List<Predict>>()
    val recentPredictions: LiveData<List<Predict>> = _recentPredictions

    // Fungsi untuk mengambil prediksi terbaru user
    fun fetchRecentPredictions() {
        viewModelScope.launch {
            try {
                val user = userRepo.getCurrentUser()
                val predictions = userRepo.getRecentPredictions(user.id)
                _recentPredictions.value = predictions
            } catch (e: Exception) {
                _recentPredictions.value = emptyList()
            }
        }
    }

    private val _allPredictions = MutableLiveData<List<Predict>>()
    val allPredictions: LiveData<List<Predict>> = _allPredictions

    // Fungsi untuk mengambil semua prediksi user
    fun fetchAllPredictions() {
        viewModelScope.launch {
            try {
                val user = userRepo.getCurrentUser()
                val predictions = userRepo.getAllPredictions(user.id)
                _allPredictions.value = predictions
            } catch (e: Exception) {
                _allPredictions.value = emptyList()
            }
        }
    }

    private val _clearUserStatus = MutableLiveData<Resource<Boolean>>()
    val clearUserStatus: LiveData<Resource<Boolean>> = _clearUserStatus

    // Fungsi untuk menghapus sesi user
    fun clearSession() {
        viewModelScope.launch {
            _clearUserStatus.value = Resource.Loading()
            try {
                userRepo.clearUser()

                // Jika sukses, update status LiveData
                _clearUserStatus.value = Resource.Success(true)
            } catch (e: Exception) {
                // Jika gagal, update status dengan error
                _clearUserStatus.value = Resource.Error("Error while clearing user: ${e.message}")
            }
        }
    }
}
