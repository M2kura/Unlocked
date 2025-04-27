package com.example.unlocked.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val country: String? = null,
    val unlockDate: Long = System.currentTimeMillis(),
    val notes: String? = null
)