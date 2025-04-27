package com.example.unlocked

import android.app.Application
import com.example.unlocked.data.database.AppDatabase
import com.example.unlocked.data.repository.CityRepository

class UnlockedApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CityRepository(database.cityDao()) }
}