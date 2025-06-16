package com.maranatha.foodlergic.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranatha.foodlergic.domain.models.User
import com.maranatha.foodlergic.domain.repository.IAuthRepository
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {
    private val _registrationStatus = MutableLiveData<Resource<Boolean>>()
    val registrationStatus: LiveData<Resource<Boolean>> = _registrationStatus

    fun register(name: String, email: String, password: String) {
        _registrationStatus.value = Resource.Loading()

        viewModelScope.launch {
            val result = authRepository.register(name, email, password)
            _registrationStatus.value = result
        }
    }

    private val _loginStatus = MutableLiveData<Resource<User>>()
    val loginStatus: LiveData<Resource<User>> = _loginStatus

    fun login(email: String, password: String) {
        _loginStatus.value = Resource.Loading() // Show loading state

        Log.d(AuthViewModel::class.simpleName, "email: $email , password: $password")
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            Log.d(AuthViewModel::class.simpleName, "login result: $result")
            _loginStatus.value = result
        }
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}