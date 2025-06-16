package com.maranatha.foodlergic.domain.repository

import com.maranatha.foodlergic.domain.models.Book

interface RewardRepository {
    suspend fun getAllReward(): List<Book>
}