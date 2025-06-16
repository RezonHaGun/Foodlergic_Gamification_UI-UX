package com.maranatha.foodlergic.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.maranatha.foodlergic.domain.models.Achievement
import com.maranatha.foodlergic.domain.usecase.AchievementUseCase
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PredictResultViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val achievementUseCase: AchievementUseCase
) : ViewModel() {

    private val _checkAchievementResult = MutableLiveData<Resource<List<Achievement>>>()
    val checkAchievementResult: LiveData<Resource<List<Achievement>>> = _checkAchievementResult

    fun checkAchievements() {
        val userId = auth.currentUser?.uid ?: return
        _checkAchievementResult.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val unlocked = achievementUseCase.checkAchievements(userId)

                _checkAchievementResult.value = Resource.Success(unlocked)
            } catch (e: Exception) {
                _checkAchievementResult.value = Resource.Error(e.message ?: "Unexpected error")
            }
        }
    }
}
