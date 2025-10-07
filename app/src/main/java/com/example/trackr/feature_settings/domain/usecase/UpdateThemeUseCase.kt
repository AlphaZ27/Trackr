package com.example.trackr.feature_settings.domain.usecase

import com.example.trackr.feature_settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateThemeUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(mode: String) = repository.setThemeMode(mode)
}