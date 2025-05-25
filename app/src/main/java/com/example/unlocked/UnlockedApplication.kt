package com.example.unlocked

import android.app.Application
import com.example.unlocked.data.database.AppDatabase
import com.example.unlocked.data.preferences.PreferencesManager
import com.example.unlocked.data.repository.CityRepository
import com.example.unlocked.notification.NotificationScheduler
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UnlockedApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CityRepository(database.cityDao()) }
    val preferencesManager by lazy { PreferencesManager(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }

        // Schedule notifications if enabled
        applicationScope.launch {
            val notificationsEnabled = preferencesManager.weeklyNotificationsEnabled.first()
            if (notificationsEnabled) {
                NotificationScheduler.scheduleWeeklyNotifications(this@UnlockedApplication)
            }
        }
    }
}