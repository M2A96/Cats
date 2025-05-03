package io.maa96.cats.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.maa96.cats.data.repository.ThemeRepositoryImpl
import io.maa96.cats.data.source.prefrence.ThemePreferences
import io.maa96.cats.data.source.prefrence.ThemePreferencesImpl
import io.maa96.cats.domain.repository.ThemeRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds
    @Singleton
    abstract fun bindThemePreferences(impl: ThemePreferencesImpl): ThemePreferences

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository
}