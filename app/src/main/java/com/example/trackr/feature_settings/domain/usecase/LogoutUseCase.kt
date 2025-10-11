package com.example.trackr.feature_settings.domain.usecase


import com.example.trackr.feature_settings.domain.repository.AccountRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke() = repository.logout()
}