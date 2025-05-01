package io.maa96.cats.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.maa96.cats.data.repository.CatBreedsRepositoryImpl
import io.maa96.cats.domain.repository.CatBreedsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCatRepository(
        catBreedsRepositoryImpl: CatBreedsRepositoryImpl
    ): CatBreedsRepository
}