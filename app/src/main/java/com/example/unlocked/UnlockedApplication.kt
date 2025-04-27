package com.example.unlocked

import android.app.Application
import com.example.unlocked.data.database.AppDatabase
import com.example.unlocked.data.repository.CityRepository
import com.google.android.libraries.places.api.Places

class UnlockedApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CityRepository(database.cityDao()) }

    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, com.example.unlocked.BuildConfig.MAPS_API_KEY)
        }
    }
}