package io.maa96.cats.domain.usecase

import io.maa96.cats.domain.repository.ThemeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SaveThemeUseCase @Inject constructor(private val themeRepository: ThemeRepository) {
    suspend operator fun invoke(isDark: Boolean) {
        themeRepository.saveTheme(isDark)
    }
}

class GetThemeUseCase @Inject constructor(private val themeRepository: ThemeRepository) {
    operator fun invoke(): Flow<Boolean> = themeRepository.getTheme()
}