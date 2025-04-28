package io.maa96.cats.data.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [], version = 1, exportSchema = false)
abstract class CatsDatabase : RoomDatabase() {
    companion object {
        const val DB_NAME = "architecture.db"
        const val VERSION = 2
    }
}