package com.example.unlocked.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unlocked.data.entity.CityEntity
import com.example.unlocked.data.repository.CityRepository
import com.example.unlocked.data.service.WikidataService
import com.example.unlocked.ui.components.PlaceResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCityViewModel(private val repository: CityRepository) : ViewModel() {
    companion object {
        private const val TAG = "AddCityViewModel"
    }

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun saveCity(placeResult: PlaceResult) {
        viewModelScope.launch {
            try {
                _saveState.value = SaveState.Saving

                Log.d(TAG, "Saving city: ${placeResult.locality} in ${placeResult.country}")

                // Fetch accurate data from Wikidata
                val wikidataResult = placeResult.locality?.let { cityName ->
                    Log.d(TAG, "Fetching Wikidata for: $cityName")
                    WikidataService.getCityData(cityName, placeResult.country)
                }

                Log.d(TAG, "Wikidata result: $wikidataResult")

                val city = CityEntity(
                    placeId = placeResult.placeId,
                    address = placeResult.address,
                    latitude = placeResult.latitude,
                    longitude = placeResult.longitude,
                    country = placeResult.country,
                    administrativeArea = placeResult.administrativeArea,
                    locality = placeResult.locality,
                    formattedAddress = placeResult.formattedAddress,
                    viewportNorthEastLat = placeResult.viewport?.northEastLat,
                    viewportNorthEastLng = placeResult.viewport?.northEastLng,
                    viewportSouthWestLat = placeResult.viewport?.southWestLat,
                    viewportSouthWestLng = placeResult.viewport?.southWestLng,
                    area = wikidataResult?.area,
                    population = wikidataResult?.population,
                    elevation = wikidataResult?.elevationAboveSeaLevel,
                    unlockDate = System.currentTimeMillis()
                )

                Log.d(TAG, "Saving city with area: ${city.area}, population: ${city.population}, elevation: ${city.elevation}")

                repository.insertCity(city)
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                Log.e(TAG, "Error saving city", e)
                _saveState.value = SaveState.Error(e.message ?: "Failed to save city")
            }
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Saving : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}

class AddCityViewModelFactory(private val repository: CityRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddCityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddCityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}