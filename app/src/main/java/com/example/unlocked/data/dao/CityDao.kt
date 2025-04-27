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

    @Query("DELETE FROM cities")
    suspend fun deleteAllCities()

    @Query("SELECT COUNT(*) FROM cities")
    fun getCityCount(): Flow<Int>

    @Query("SELECT * FROM cities WHERE locality = :locality AND country = :country LIMIT 1")
    suspend fun getCityByLocalityAndCountry(locality: String, country: String): CityEntity?

    @Query("SELECT * FROM cities WHERE placeId = :placeId LIMIT 1")
    suspend fun getCityByPlaceId(placeId: String): CityEntity?
}