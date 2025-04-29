package io.maa96.cats.data.source.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.maa96.cats.data.source.local.db.entity.CatBreedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BreedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreeds(breeds: List<CatBreedEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreed(breed: CatBreedEntity)

    @Query("Select * From breed WHERE `id` == :breedId")
    fun getBreedById(breedId: String): Flow<CatBreedEntity>

    @Query("SELECT * FROM breed WHERE name LIKE '%' || :search || '%'")
    fun searchByName(search: String): Flow<List<CatBreedEntity>>

    @Query("SELECT * FROM breed")
    fun getBreeds(): Flow<List<CatBreedEntity>>

    @Query("DELETE from breed")
    suspend fun deleteAllBreeds()

    @Query("DELETE FROM breed WHERE id == :breedId")
    suspend fun deleteBreedById(breedId: String)
}