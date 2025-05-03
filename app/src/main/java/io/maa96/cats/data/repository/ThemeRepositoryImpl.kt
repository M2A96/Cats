package io.maa96.cats.data.repository

import io.maa96.cats.data.source.prefrence.ThemePreferences
import io.maa96.cats.domain.repository.ThemeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ThemeRepositoryImpl @Inject constructor(private val themePreferences: ThemePreferences) : ThemeRepository {

    override suspend fun saveTheme(isDark: Boolean) {
        themePreferences.saveTheme(isDark)
    }

    override fun getTheme(): Flow<Boolean> = themePreferences.getTheme()
}