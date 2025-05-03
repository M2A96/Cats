package io.maa96.cats.domain.repository

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    suspend fun saveTheme(isDark: Boolean)
    fun getTheme(): Flow<Boolean>
}