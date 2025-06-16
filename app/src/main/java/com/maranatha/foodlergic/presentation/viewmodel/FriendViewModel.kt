package com.maranatha.foodlergic.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranatha.foodlergic.domain.models.FriendRequest
import com.maranatha.foodlergic.domain.models.User
import com.maranatha.foodlergic.domain.repository.FriendRepository
import com.maranatha.foodlergic.domain.repository.UserRepository
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val friendRepo: FriendRepository,
    private val userRepo: UserRepository
) : ViewModel() {
    private val _searchedUser = MutableLiveData<Resource<User>>()
    val searchedUser: LiveData<Resource<User>> = _searchedUser

    fun searchUserByFriendCode(code: String) {
        _searchedUser.value = Resource.Loading()
        viewModelScope.launch {
            val user = userRepo.getUserByFriendCode(code)
            _searchedUser.value = user
        }
    }


    private val _requestStatus = MutableLiveData<Resource<Unit>>()
    val requestStatus: LiveData<Resource<Unit>> = _requestStatus

    fun sendFriendRequest(friendCode: String) {
        _requestStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = friendRepo.sendFriendRequest(friendCode)
            _requestStatus.value = result
        }
    }

    private val _receivedRequests = MutableLiveData<Resource<List<FriendRequest>>>()
    val receivedRequests: LiveData<Resource<List<FriendRequest>>> = _receivedRequests

    fun getReceivedFriendRequests() {
        _receivedRequests.value = Resource.Loading()
        viewModelScope.launch {
            val result = friendRepo.getReceivedFriendRequests()
            _receivedRequests.value = result
        }
    }
    private val _actionStatus = MutableLiveData<Resource<Unit>>()
    val actionStatus: LiveData<Resource<Unit>> = _actionStatus

    fun acceptFriendRequest(request: FriendRequest) {
        _actionStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = friendRepo.acceptFriendRequest(request)
            _actionStatus.value = result
        }
    }

    fun denyFriendRequest(request: FriendRequest) {
        _actionStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = friendRepo.rejectFriendRequest(request)
            _actionStatus.value = result
        }
    }

}
