package com.maranatha.foodlergic.domain.models

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import com.maranatha.foodlergic.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val image: String,
    val name: String,
    val code: String,
    val urlBook: String,
    val threshold: Int,
    val summary: String
) : Parcelable
