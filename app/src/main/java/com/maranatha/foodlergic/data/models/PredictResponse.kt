package com.maranatha.foodlergic.data.models

import com.google.gson.annotations.SerializedName

data class PredictResponse(

    @field:SerializedName("data")
    val data: PredictData,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: String
)

data class PredictData(

    @field:SerializedName("result")
    val result: String,

    @field:SerializedName("createdAt")
    val createdAt: String,

    @field:SerializedName("suggestion")
    val suggestion: String,

    @field:SerializedName("id")
    val id: String
)