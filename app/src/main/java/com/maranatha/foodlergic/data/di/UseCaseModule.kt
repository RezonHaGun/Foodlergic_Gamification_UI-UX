package com.maranatha.foodlergic.data.di

import android.app.Application
import android.content.Context
import com.maranatha.foodlergic.data.checker.DailyScanAchievementChecker
import com.maranatha.foodlergic.data.checker.SafeScanAchievementChecker
import com.maranatha.foodlergic.data.checker.ScanPointAchievementChecker
import com.maranatha.foodlergic.domain.repository.AchievementRepository
import com.maranatha.foodlergic.domain.repository.UserRepository
import com.maranatha.foodlergic.domain.usecase.AchievementUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideAchievmentUseCase(
        userRepository: UserRepository,
        achievementRepository: AchievementRepository,
        scanPointAchievementChecker: ScanPointAchievementChecker,
        safeScanAchievementChecker: SafeScanAchievementChecker,
        dailyScanAchievementChecker: DailyScanAchievementChecker
    ): AchievementUseCase {
        return AchievementUseCase(userRepository, achievementRepository,scanPointAchievementChecker,safeScanAchievementChecker,dailyScanAchievementChecker)
    }

    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app.applicationContext
}
