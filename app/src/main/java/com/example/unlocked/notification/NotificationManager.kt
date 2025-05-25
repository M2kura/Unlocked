package com.example.unlocked.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.unlocked.MainActivity
import com.example.unlocked.R
import kotlin.random.Random

class UnlockedNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "unlocked_weekly_stats"
        const val CHANNEL_NAME = "Weekly Statistics"
        const val CHANNEL_DESCRIPTION = "Weekly reminders about your travel statistics"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showWeeklyStatsNotification(
        totalCities: Int,
        totalCountries: Int,
        lastUnlockedCity: String?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, text) = generateNotificationContent(totalCities, totalCountries, lastUnlockedCity)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }

    private fun generateNotificationContent(
        totalCities: Int,
        totalCountries: Int,
        lastUnlockedCity: String?
    ): Pair<String, String> {
        val titles = listOf(
            "🌍 Your Travel Journey",
            "🗺️ Adventure Update",
            "📍 Places Unlocked",
            "✈️ Travel Stats",
            "🌟 Your Progress"
        )

        val messages = when {
            totalCities == 0 -> listOf(
                "Ready to start your journey? Add your first city and begin exploring the world!",
                "Your adventure awaits! Start tracking the amazing places you visit.",
                "Time to unlock your first destination. Where will your journey begin?"
            )
            totalCities < 5 -> listOf(
                "You've unlocked $totalCities ${if (totalCities == 1) "city" else "cities"} across $totalCountries ${if (totalCountries == 1) "country" else "countries"}! Keep exploring to grow your collection.",
                "Great start! $totalCities ${if (totalCities == 1) "place" else "places"} unlocked so far. What's your next destination?",
                "Your travel map is taking shape with $totalCities ${if (totalCities == 1) "city" else "cities"}! Check out your stats for more insights."
            )
            totalCities < 20 -> listOf(
                "Impressive! You've explored $totalCities cities in $totalCountries ${if (totalCountries == 1) "country" else "countries"}. ${lastUnlockedCity?.let { "Last unlocked: $it." } ?: ""}",
                "You're building quite the travel portfolio! $totalCities cities and counting across $totalCountries ${if (totalCountries == 1) "country" else "countries"}.",
                "Look at you go! $totalCities destinations unlocked. Your travel stats have some interesting surprises!"
            )
            else -> listOf(
                "Wow! $totalCities cities across $totalCountries countries! You're becoming a true world explorer. ${lastUnlockedCity?.let { "Latest addition: $it." } ?: ""}",
                "World traveler alert! 🌎 $totalCities cities unlocked across $totalCountries countries. Your stats are incredible!",
                "Amazing journey! You've unlocked $totalCities cities in $totalCountries different countries. Check your detailed stats!"
            )
        }

        return Pair(
            titles.random(),
            messages.random()
        )
    }
}