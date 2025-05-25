package com.example.unlocked.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unlocked.data.preferences.PreferencesManager
import com.example.unlocked.data.repository.CityRepository
import com.example.unlocked.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: CityRepository,
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : ViewModel() {

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    val weeklyNotificationsEnabled = preferencesManager.weeklyNotificationsEnabled

    fun deleteAllData() {
        viewModelScope.launch {
            try {
                _deleteState.value = DeleteState.Deleting
                repository.deleteAllCities()
                _deleteState.value = DeleteState.Success
            } catch (e: Exception) {
                _deleteState.value = DeleteState.Error(e.message ?: "Failed to delete data")
            }
        }
    }

    fun setWeeklyNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setWeeklyNotificationsEnabled(enabled)
            if (enabled) {
                NotificationScheduler.scheduleWeeklyNotifications(context)
            } else {
                NotificationScheduler.cancelWeeklyNotifications(context)
            }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteState.Idle
    }

    sealed class DeleteState {
        object Idle : DeleteState()
        object Deleting : DeleteState()
        object Success : DeleteState()
        data class Error(val message: String) : DeleteState()
    }
}

class SettingsViewModelFactory(
    private val repository: CityRepository,
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, preferencesManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}