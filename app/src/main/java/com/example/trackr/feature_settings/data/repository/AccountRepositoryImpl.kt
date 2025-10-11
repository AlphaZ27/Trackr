package com.example.trackr.feature_settings.data.repository


import com.example.trackr.feature_settings.data.preferences.UserPreferences
import com.example.trackr.feature_settings.domain.model.UserType
import com.example.trackr.feature_settings.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : AccountRepository {

    override fun getUserType(): Flow<UserType> = userPreferences.userTypeFlow

    override suspend fun saveUserType(userType: UserType) {
        userPreferences.setUserType(userType)
    }

    override suspend fun logout() {
        // Here you would add real logic, like signing out from Firebase,
        // clearing tokens, and resetting the stored user type.
        // For this example, we'll just reset the type.
        userPreferences.setUserType(UserType.User)
    }
}