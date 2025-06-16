package com.maranatha.foodlergic.data.di

import com.maranatha.foodlergic.data.checker.SafeScanAchievementChecker
import com.maranatha.foodlergic.data.checker.ScanPointAchievementChecker
import com.maranatha.foodlergic.data.repository.FirebaseAchievementRepository
import com.maranatha.foodlergic.data.repository.FirebaseAuthRepository
import com.maranatha.foodlergic.data.repository.FirebaseFriendRepository
import com.maranatha.foodlergic.data.repository.FirebaseLevelRepository
import com.maranatha.foodlergic.data.repository.FirebaseRewardRepository
import com.maranatha.foodlergic.data.repository.FirebaseUserProfileRepository
import com.maranatha.foodlergic.data.repository.FirebaseUserRepository
import com.maranatha.foodlergic.domain.repository.AchievementRepository
import com.maranatha.foodlergic.domain.repository.FriendRepository
import com.maranatha.foodlergic.domain.repository.IAuthRepository
import com.maranatha.foodlergic.domain.repository.IUserProfileRepository
import com.maranatha.foodlergic.domain.repository.LevelRepository
import com.maranatha.foodlergic.domain.repository.RewardRepository
import com.maranatha.foodlergic.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(repository: FirebaseAuthRepository): IAuthRepository

    @Binds
    abstract fun bindUserProfileRepository(repository: FirebaseUserProfileRepository): IUserProfileRepository

    @Binds
    abstract fun bindUserRepository(repository: FirebaseUserRepository): UserRepository

    @Binds
    abstract fun bindAchievementRepository(repository: FirebaseAchievementRepository): AchievementRepository

    @Binds
    abstract fun bindRewardRepository(repository: FirebaseRewardRepository): RewardRepository

    @Binds
    abstract fun bindFriendRepository(repository: FirebaseFriendRepository): FriendRepository

    @Binds
    abstract fun bindLevelRepository(repository: FirebaseLevelRepository): LevelRepository
}