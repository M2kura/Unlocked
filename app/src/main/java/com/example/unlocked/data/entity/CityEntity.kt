package com.example.unlocked.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val placeId: String? = null,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val country: String? = null,
    val administrativeArea: String? = null, // State/Province
    val locality: String? = null, // City name
    val formattedAddress: String? = null,
    val viewportNorthEastLat: Double? = null,
    val viewportNorthEastLng: Double? = null,
    val viewportSouthWestLat: Double? = null,
    val viewportSouthWestLng: Double? = null,
    val area: Double? = null,
    val population: Int? = null,
    val elevation: Double? = null, // in meters
    val unlockDate: Long = System.currentTimeMillis(),
    val notes: String? = null
)