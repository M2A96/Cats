package io.maa96.cats.data.source.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null

        return value.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
    }
}
