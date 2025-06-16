package com.maranatha.foodlergic.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.maranatha.foodlergic.domain.models.Achievement
import com.maranatha.foodlergic.domain.models.Book
import com.maranatha.foodlergic.domain.repository.RewardRepository
import com.maranatha.foodlergic.domain.usecase.AchievementUseCase
import com.maranatha.foodlergic.domain.usecase.RewardUseCase
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val rewardRepository: RewardRepository,
    private val rewardUseCase: RewardUseCase
) : ViewModel() {
    private val _allBooks = MutableLiveData<Resource<List<Book>>>()
    val allBooks: LiveData<Resource<List<Book>>> = _allBooks

    fun getListBook() {
        viewModelScope.launch {
            try {
                val books = rewardRepository.getAllReward()
                _allBooks.value = Resource.Success(books)
            } catch (e: Exception) {
                // Tangani error jika gagal mendapatkan data
                _allBooks.value = Resource.Error("Failed to get all books")
            }
        }
    }

    private val _checkRewardsResult = MutableLiveData<Resource<List<Book>>>()
    val checkRewardsResult: LiveData<Resource<List<Book>>> = _checkRewardsResult

    fun checkRewards() {
        val userId = auth.currentUser?.uid ?: return
        _checkRewardsResult.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val unlocked = rewardUseCase.checkRewards(userId)

                _checkRewardsResult.value = Resource.Success(unlocked)
            } catch (e: Exception) {
                _checkRewardsResult.value = Resource.Error(e.message ?: "Unexpected error")
            }
        }
    }
}