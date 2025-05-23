package io.maa96.cats.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.maa96.cats.data.source.local.db.CatsDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    @Singleton
    fun provideRoomDatabase(context: Context): CatsDatabase = CatsDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideBreedDao(db: CatsDatabase) = db.breedDao()
}