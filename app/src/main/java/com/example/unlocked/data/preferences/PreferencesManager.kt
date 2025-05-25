package com.example.unlocked.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey // Add this import
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val WEEKLY_NOTIFICATIONS_KEY = booleanPreferencesKey("weekly_notifications")
        val MARKER_COLOR_KEY = stringPreferencesKey("marker_color") // Now properly imported
    }

    val weeklyNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[WEEKLY_NOTIFICATIONS_KEY] ?: true // Default to enabled
        }

    val markerColor: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[MARKER_COLOR_KEY] ?: "#FF0000" // Default to red
        }

    suspend fun setWeeklyNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WEEKLY_NOTIFICATIONS_KEY] = enabled
        }
    }

    suspend fun setMarkerColor(colorHex: String) {
        context.dataStore.edit { preferences ->
            preferences[MARKER_COLOR_KEY] = colorHex
        }
    }
}