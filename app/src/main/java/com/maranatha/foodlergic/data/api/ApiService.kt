package com.maranatha.foodlergic.data.api

import com.maranatha.foodlergic.data.models.AddAllergyResponse
import com.maranatha.foodlergic.data.models.AllergyRequest
import com.maranatha.foodlergic.data.models.DeleteAlergyRequest
import com.maranatha.foodlergic.data.models.LoginRequest
import com.maranatha.foodlergic.data.models.LoginResponse
import com.maranatha.foodlergic.data.models.RegisterRequest
import com.maranatha.foodlergic.data.models.RegisterResponse
import com.maranatha.foodlergic.data.models.ResponseBody
import com.maranatha.foodlergic.data.models.ShowAllergiesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {
    @POST("api/auth/login")
    suspend fun login(
        @Body requestBody: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body requestBody: RegisterRequest
    ): Response<RegisterResponse>

    // Show allergies
    @GET("api/allergy/show/{userId}/allergies")
    suspend fun showAllergies(
        @Path("userId") userId: String
    ): Response<ShowAllergiesResponse>

    // Add allergy
    @POST("api/allergy/{userId}/allergies")
    suspend fun addAllergy(
        @Path("userId") userId: String,
        @Body requestBody: AllergyRequest
    ): Response<AddAllergyResponse>

    // Update allergy
    @FormUrlEncoded
    @POST("api/scan/allergy/{userId}/allergies")
    suspend fun updateAllergy(
        @Path("userId") userId: String,
        @Field("allergies") allergies: String
    ): Response<ResponseBody>

    // Delete allergy
    @POST("api/allergy/delete")
    suspend fun deleteAllergy(
        @Body requestBody: DeleteAlergyRequest
    ): Response<ShowAllergiesResponse>

}
