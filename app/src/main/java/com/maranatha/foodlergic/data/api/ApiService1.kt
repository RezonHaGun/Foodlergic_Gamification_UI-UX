package com.maranatha.foodlergic.data.api

import com.maranatha.foodlergic.data.models.PredictResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ApiService1 {
    // Delete allergy

    @Multipart
    @POST("predict")
    suspend fun predict(
        @Part("userId") userId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<PredictResponse>
}