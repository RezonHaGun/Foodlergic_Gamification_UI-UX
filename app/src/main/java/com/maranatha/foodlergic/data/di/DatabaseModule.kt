package com.maranatha.foodlergic.data.di

import android.content.Context
import androidx.room.Room
import com.maranatha.foodlergic.data.AppDatabase
import com.maranatha.foodlergic.data.dao.PredictionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "prediction_db"
        ).build()
    }

    @Provides
    fun providePredictionDao(db: AppDatabase): PredictionDao {
        return db.predictionDao()
    }

}