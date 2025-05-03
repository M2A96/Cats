package io.maa96.cats.data.source.prefrence

import kotlinx.coroutines.flow.Flow

interface ThemePreferences {
    suspend fun saveTheme(isDark: Boolean)
    fun getTheme(): Flow<Boolean>
}