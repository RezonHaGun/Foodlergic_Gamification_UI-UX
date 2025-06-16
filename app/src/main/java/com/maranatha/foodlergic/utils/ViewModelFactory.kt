package com.maranatha.foodlergic.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maranatha.foodlergic.presentation.viewmodel.AllergyViewModel
import com.maranatha.foodlergic.presentation.viewmodel.PredictViewModel
import com.maranatha.foodlergic.data.Preference

//class ViewModelFactory(private val preference: Preference) : ViewModelProvider.Factory {
//
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return when {
//            modelClass.isAssignableFrom(AllergyViewModel::class.java) -> {
//                AllergyViewModel(preference) as T
//            }
//
//            modelClass.isAssignableFrom(PredictViewModel::class.java) -> {
//                PredictViewModel(preference) as T
//            }
//
//            else -> {
//                throw IllegalArgumentException("Class ViewModel not Implement")
//            }
//        }
//    }
//}