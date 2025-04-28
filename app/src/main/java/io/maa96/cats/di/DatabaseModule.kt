package io.maa96.cats.di

import android.content.Context
import androidx.room.Room
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.maa96.cats.data.source.local.db.CatsDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideRoomDatabase(context: Context): CatsDatabase {
        return Room
            .databaseBuilder(context, CatsDatabase::class.java, CatsDatabase.DB_NAME)
            .fallbackToDestructiveMigration(false)
            .build()
    }
}