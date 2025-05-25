package com.example.unlocked.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val WEEKLY_NOTIFICATIONS_KEY = booleanPreferencesKey("weekly_notifications")
    }

    val weeklyNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[WEEKLY_NOTIFICATIONS_KEY] ?: true // Default to enabled
        }

    suspend fun setWeeklyNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WEEKLY_NOTIFICATIONS_KEY] = enabled
        }
    }
}