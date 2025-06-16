package com.maranatha.foodlergic.data.di

import com.maranatha.foodlergic.data.checker.DailyScanAchievementChecker
import com.maranatha.foodlergic.data.checker.SafeScanAchievementChecker
import com.maranatha.foodlergic.data.checker.ScanPointAchievementChecker
import com.maranatha.foodlergic.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AchievementCheckerModule {

    @Provides
    fun provideScanCountAchievementChecker(
        userRepository: UserRepository
    ): ScanPointAchievementChecker {
        return ScanPointAchievementChecker(userRepository)
    }

    @Provides
    fun provideSafeScanAchievementChecker(
        userRepository: UserRepository
    ): SafeScanAchievementChecker {
        return SafeScanAchievementChecker(userRepository)
    }

    @Provides
    fun provideDailyScanAchievementChecker(
        userRepository: UserRepository
    ): DailyScanAchievementChecker {
        return DailyScanAchievementChecker(userRepository)
    }
}