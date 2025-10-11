package com.example.trackr.feature_settings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.trackr.feature_settings.data.preferences.SettingsPreferences
import com.example.trackr.feature_settings.data.preferences.UserPreferences
import com.example.trackr.feature_settings.data.repository.SettingsRepositoryImpl
import com.example.trackr.feature_settings.data.repository.AccountRepositoryImpl
import com.example.trackr.feature_settings.domain.repository.SettingsRepository
import com.example.trackr.feature_settings.domain.repository.AccountRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    // --- Theme Settings Dependencies ---
    @Provides
    @Singleton
    fun provideSettingsPreferences(dataStore: DataStore<Preferences>): SettingsPreferences {
        return SettingsPreferences(dataStore)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsPreferences: SettingsPreferences
    ): SettingsRepository {
        return SettingsRepositoryImpl(settingsPreferences)
    }

    // --- Account Dependencies  ---
    @Provides
    @Singleton
    fun provideUserPreferences(dataStore: DataStore<Preferences>): UserPreferences {
        return UserPreferences(dataStore)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(
        userPreferences: UserPreferences
    ): AccountRepository {
        return AccountRepositoryImpl(userPreferences)
    }
}