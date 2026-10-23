package com.rojas.disco.core.database.di

import android.content.Context
import com.rojas.disco.core.database.local.database.AppDatabase
import com.rojas.disco.core.database.local.database.UniversidadDao
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
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUniversidadDao(
        database: AppDatabase
    ): UniversidadDao {
        return database.universidadDao()
    }
}
