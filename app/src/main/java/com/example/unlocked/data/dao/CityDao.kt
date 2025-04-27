package com.example.unlocked.data.dao

import androidx.room.*
import com.example.unlocked.data.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Query("SELECT * FROM cities ORDER BY unlockDate DESC")
    fun getAllCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE id = :id")
    suspend fun getCityById(id: Long): CityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity): Long

    @Update
    suspend fun updateCity(city: CityEntity)

    @Delete
    suspend fun deleteCity(city: CityEntity)

    @Query("SELECT COUNT(*) FROM cities")
    fun getCityCount(): Flow<Int>
}