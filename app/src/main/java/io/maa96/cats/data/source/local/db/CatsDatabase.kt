package io.maa96.cats.data.source.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.maa96.cats.data.source.local.db.dao.BreedDao
import io.maa96.cats.data.source.local.db.entity.CatBreedEntity

@Database(
    entities = [
        CatBreedEntity::class
    ],
    version = CatsDatabase.VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CatsDatabase : RoomDatabase() {
    companion object {
        const val DB_NAME = "snapAssignment.db"
        const val VERSION = 1

        @Volatile
        private var instance: CatsDatabase? = null

        fun getInstance(context: Context): CatsDatabase = instance ?: synchronized(this) {
            instance ?: buildDataBase(context).also {
                instance = it
            }
        }

        private fun buildDataBase(context: Context): CatsDatabase = Room
            .databaseBuilder(context, CatsDatabase::class.java, DB_NAME)
            .build()
    }

    abstract fun breedDao(): BreedDao
}