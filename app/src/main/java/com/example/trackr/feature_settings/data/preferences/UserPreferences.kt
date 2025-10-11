package com.example.trackr.feature_settings.data.preferences


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.trackr.feature_settings.domain.model.UserType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val USER_TYPE = stringPreferencesKey("user_type")
    }

    val userTypeFlow: Flow<UserType> = dataStore.data
        .map { prefs ->
            // Read the string from DataStore and map it to the correct enum type
            when (prefs[USER_TYPE]) {
                "Admin" -> UserType.Admin
                "Manager" -> UserType.Manager
                else -> UserType.User // Default to User
            }
        }

    suspend fun setUserType(type: UserType) {
        dataStore.edit { it[USER_TYPE] = type.name }
    }
}