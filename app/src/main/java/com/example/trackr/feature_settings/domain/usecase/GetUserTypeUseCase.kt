package com.example.trackr.feature_settings.domain.usecase


import com.example.trackr.feature_settings.domain.model.UserType
import com.example.trackr.feature_settings.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserTypeUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<UserType> = repository.getUserType()
}