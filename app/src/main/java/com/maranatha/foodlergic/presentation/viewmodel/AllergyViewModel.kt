package com.maranatha.foodlergic.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.data.models.Allergy
import com.maranatha.foodlergic.utils.AllergyLabelUtils
import com.maranatha.foodlergic.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllergyViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _userAllergies = MutableLiveData<Resource<List<Allergy>>>()
    val userAllergies: LiveData<Resource<List<Allergy>>> = _userAllergies

    fun getUserAllergies() {
        _userAllergies.value = Resource.Loading()

        firestore.collection("users")
            .document(firebaseAuth.currentUser?.uid.orEmpty())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _userAllergies.value =
                        Resource.Error("Error fetching allergies: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val allergiesMap =
                        snapshot.get("allergies") as? Map<String, Boolean> ?: emptyMap()
                    val mappedAllergies = allergiesMap.map { (name, isSelected) ->
                        val image = when (name.lowercase()) {
                            "fish" -> R.drawable.fish_image
                            "shell" -> R.drawable.shellfish_image
                            "crab" -> R.drawable.crab_image
                            "shrimp" -> R.drawable.shrimp_image
                            else -> R.drawable.fish_image
                        }

                        val displayName = AllergyLabelUtils.getEnglishLabel(name)

                        Allergy(name = displayName, image = image, isSelected = isSelected)
                    }

                    _userAllergies.value = Resource.Success(mappedAllergies)
                } else {
                    _userAllergies.value = Resource.Success(emptyList())
                }
            }
    }

    fun setLocalAllergies(localAllergyNames: List<String>) {
        val mappedAllergies = localAllergyNames.map { name ->
            val image = when (name.lowercase()) {
                "fish" -> R.drawable.fish_image
                "shell" -> R.drawable.shellfish_image
                "crab" -> R.drawable.crab_image
                "shrimp" -> R.drawable.shrimp_image
                else -> R.drawable.fish_image
            }

            val displayName = AllergyLabelUtils.getEnglishLabel(name)

            Allergy(name = displayName, image = image, isSelected = true)
        }

        _userAllergies.value = Resource.Success(mappedAllergies)
    }


    fun isAllergic(label: String): Boolean {
        // Get the list of allergies that are selected
        val selectedAllergies = _userAllergies.value?.data?.filter { it.isSelected }
            ?.map { it.name.lowercase() }
            .orEmpty()

        // Check if the given label is in the list of selected allergies
        return label.lowercase() in selectedAllergies
    }


    private val _submitResult = MutableLiveData<Resource<String>>()
    val submitResult: LiveData<Resource<String>> = _submitResult

    fun submitAllergies(allergyNames: List<String>) {
        viewModelScope.launch {
            _submitResult.value = Resource.Loading()
            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId == null) {
                    _submitResult.value = Resource.Error("User tidak ditemukan")
                    return@launch
                }

                val allAllergyOptions = listOf("fish", "shrimp", "crab", "shell")
                val allergyMap = allAllergyOptions.associateWith { it in allergyNames }

                val userAllergyData = mapOf(
                    "allergies" to allergyMap,
                    "isAllergySubmitted" to true
                )

                firestore.collection("users")
                    .document(userId)
                    .set(userAllergyData, SetOptions.merge())
                    .addOnSuccessListener {
                        _submitResult.value = Resource.Success("Allergies berhasil disimpan")
                    }
                    .addOnFailureListener { e ->
                        _submitResult.value = Resource.Error("Gagal menyimpan: ${e.message}")
                    }

            } catch (e: Exception) {
                _submitResult.value = Resource.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }
}