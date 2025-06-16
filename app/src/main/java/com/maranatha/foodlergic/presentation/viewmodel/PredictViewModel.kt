package com.maranatha.foodlergic.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.maranatha.foodlergic.data.dao.PredictionDao
import com.maranatha.foodlergic.data.entity.Prediction
import com.maranatha.foodlergic.domain.models.Predict
import com.maranatha.foodlergic.domain.repository.LevelRepository
import com.maranatha.foodlergic.utils.Resource
import com.maranatha.foodlergic.utils.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PredictViewModel @Inject constructor(
    private val dao: PredictionDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val levelRepository: LevelRepository
) : ViewModel() {

    private val _status = MutableLiveData<Resource<Predict>>()
    val status: LiveData<Resource<Predict>> = _status

    fun predictAndSave(
        context: Context, predictedAllergen: String, hasAllergy: Boolean, foodName: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _status.postValue(Resource.Loading())

            Log.d("PREDICTION", "Starting prediction and saving process...")

            if (isNetworkAvailable(context)) {
                val predict = savePredictionToFirestore(predictedAllergen, hasAllergy, foodName)
                if (predict != null) {
                    Log.d("PREDICTION", "Prediction saved successfully.")
                    _status.postValue(Resource.Success(predict))
                } else {
                    Log.e("PREDICTION", "Failed to save prediction.")
                    _status.postValue(Resource.Error("Gagal menyimpan prediksi ke Firestore"))
                }
            } else {
                // Jika offline, simpan ke database lokal (contoh: Room Database)
                val predict = savePredictionLocally(predictedAllergen, hasAllergy, foodName)
                if (predict != null) {
                    _status.postValue(Resource.Success(predict))
                } else {
                    _status.postValue(Resource.Error("Gagal menyimpan prediksi secara offline"))
                }
            }
        }
    }

    private suspend fun savePredictionToFirestore(
        predictionResult: String, hasAllergy: Boolean, foodName: String
    ): Predict? {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("PREDICTION", "User ID is null.")
            return null
        }

        Log.d("PREDICTION", "User ID: $userId")

        val predictionData = mapOf(
            "timestamp" to com.google.firebase.Timestamp.now(),
            "predicted_allergen" to predictionResult,
            "foodName" to foodName
        )

        try {
            Log.d("PREDICTION", "Saving prediction to Firestore...")
            val predictionRef =
                firestore.collection("users").document(userId).collection("predictions")
                    .add(predictionData).await()

            Log.d("PREDICTION", "Prediction data added successfully: $predictionRef")

            // Update scan count
            Log.d("PREDICTION", "Updating scan count...")
            firestore.collection("users").document(userId)
                .update("scanCount", FieldValue.increment(1)).await()


            // Fetch user details
            Log.d("PREDICTION", "Fetching user details...")
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userName = userDoc.getString("name") ?: "Anonymous"
            val point = userDoc.getLong("point") ?: 1L

            val levels = levelRepository.fetchLevels()

            val userLevel =
                levels.firstOrNull { point in it.minScanCount..it.maxScanCount }?.name
                    ?: "Unknown"
            Log.d("PREDICTION", "User level determined: $userLevel")

            // Update user level in Firestore
            firestore.collection("users").document(userId).update("level", userLevel).await()

            Log.d("PREDICTION", "User level updated in Firestore.")

            // Update leaderboard
            Log.d("PREDICTION", "Updating leaderboard...")
            val leaderboardDocRef = firestore.collection("leaderboard").document(userId)
            leaderboardDocRef.set(
                mapOf(
                    "userId" to userId,
                    "name" to userName,
                    "point" to point,
                    "level" to userLevel,
                    "lastUpdated" to com.google.firebase.Timestamp.now()
                ), SetOptions.merge()
            ).await()


            if (!hasAllergy) {
                firestore.collection("users").document(userId)
                    .update("safeScanCount", FieldValue.increment(1)).await()

                firestore.collection("users").document(userId)
                    .update("point", FieldValue.increment(5)).await()
            } else {
                firestore.collection("users").document(userId)
                    .update("safeScanCount", 0).await()
            }

            Log.d("PREDICTION", "Leaderboard updated successfully.")

            // Return the prediction object
            return Predict(
                predictionResult,
                hasAllergy,
                com.google.firebase.Timestamp.now(),
                foodName
            )

        } catch (e: Exception) {
            Log.e("PREDICTION", "Failed to save prediction: ${e.message}", e)
            return null
        }
    }

    private suspend fun savePredictionLocally(
        predictedAllergen: String, hasAllergy: Boolean, foodName: String
    ): Predict? {
        return try {
            val entity = Prediction(
                allergen = predictedAllergen,
                hasAllergy = hasAllergy,
                foodName = foodName,
                timestamp = System.currentTimeMillis()
            )
            dao.insertPrediction(entity)

            Predict(
                predictedAllergen = predictedAllergen,
                hasAllergy = hasAllergy,
                foodName = foodName,
                timestamp = com.google.firebase.Timestamp.now()
            )
        } catch (e: Exception) {
            Log.e("PREDICTION", "Failed to save prediction locally: ${e.message}", e)
            null
        }
    }

    fun syncLocalPredictions(context: Context) {
        if (!isNetworkAvailable(context)) return

        viewModelScope.launch(Dispatchers.IO) {
            val unsynced = dao.getUnsyncedPredictions()

            unsynced.forEach { prediction ->
                try {
                    val firestorePredict = savePredictionToFirestore(
                        predictionResult = prediction.allergen,
                        hasAllergy = prediction.hasAllergy,
                        foodName = prediction.foodName
                    )
                    if (firestorePredict != null) {
                        dao.updatePrediction(prediction.copy(isSynced = true))
                    }
//                    dao.updatePrediction(prediction.copy(isSynced = true))
                } catch (e: Exception) {
                    Log.e("SYNC", "Gagal sinkronisasi prediksi ID ${prediction.id}: ${e.message}")
                }
            }
        }
    }

    fun clearStatus() {
        _status.value = null
    }
}
