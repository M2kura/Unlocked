package com.example.unlocked.data.repository

import com.example.unlocked.data.dao.CityDao
import com.example.unlocked.data.entity.CityEntity
import kotlinx.coroutines.flow.Flow

class CityRepository(private val cityDao: CityDao) {

    fun getAllCities(): Flow<List<CityEntity>> = cityDao.getAllCities()

    suspend fun getCityById(id: Long): CityEntity? = cityDao.getCityById(id)

    suspend fun insertCity(city: CityEntity): Long = cityDao.insertCity(city)

    suspend fun updateCity(city: CityEntity) = cityDao.updateCity(city)

    suspend fun deleteCity(city: CityEntity) = cityDao.deleteCity(city)

    suspend fun deleteAllCities() = cityDao.deleteAllCities()

    fun getCityCount(): Flow<Int> = cityDao.getCityCount()

    suspend fun isCityExists(locality: String?, country: String?, placeId: String?): Boolean {
        placeId?.let { pid ->
            cityDao.getCityByPlaceId(pid)?.let { return true }
        }

        if (locality != null && country != null) {
            cityDao.getCityByLocalityAndCountry(locality, country)?.let { return true }
        }

        return false
    }
}