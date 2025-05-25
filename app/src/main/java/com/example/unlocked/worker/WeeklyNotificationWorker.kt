package com.example.unlocked.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.unlocked.UnlockedApplication
import com.example.unlocked.notification.UnlockedNotificationManager
import kotlinx.coroutines.flow.first

class WeeklyNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val application = applicationContext as UnlockedApplication
            val repository = application.repository
            val notificationManager = UnlockedNotificationManager(applicationContext)

            // Get current statistics
            val cities = repository.getAllCities().first()
            val totalCities = cities.size
            val totalCountries = cities.mapNotNull { it.country }.distinct().size
            val lastUnlockedCity = cities.sortedByDescending { it.unlockDate }
                .firstOrNull()?.locality

            // Show notification
            notificationManager.showWeeklyStatsNotification(
                totalCities = totalCities,
                totalCountries = totalCountries,
                lastUnlockedCity = lastUnlockedCity
            )

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}