package com.example.trackr.feature_settings.domain.repository

import com.example.trackr.feature_settings.domain.model.UserType
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getUserType(): Flow<UserType>
    suspend fun saveUserType(userType: UserType)
    suspend fun logout()
}