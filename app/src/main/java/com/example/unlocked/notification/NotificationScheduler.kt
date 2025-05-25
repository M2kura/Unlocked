package com.example.unlocked.notification

import android.content.Context
import androidx.work.*
import com.example.unlocked.worker.WeeklyNotificationWorker
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val WEEKLY_NOTIFICATION_WORK = "weekly_notification_work"

    fun scheduleWeeklyNotifications(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val weeklyWorkRequest = PeriodicWorkRequestBuilder<WeeklyNotificationWorker>(
            7, TimeUnit.DAYS,
            1, TimeUnit.HOURS // Flex interval
        )
            .setInitialDelay(7, TimeUnit.DAYS) // Start after 7 days
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WEEKLY_NOTIFICATION_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyWorkRequest
        )
    }

    fun cancelWeeklyNotifications(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(WEEKLY_NOTIFICATION_WORK)
    }
}