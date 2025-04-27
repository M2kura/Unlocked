package com.example.unlocked.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unlocked.data.entity.CityEntity
import com.example.unlocked.data.repository.CityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModel(private val repository: CityRepository) : ViewModel() {

    val cities: StateFlow<List<CityEntity>> = repository.getAllCities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCountry = MutableStateFlow<String?>(null)
    val selectedCountry: StateFlow<String?> = _selectedCountry.asStateFlow()

    // Get available countries from the cities
    val availableCountries: StateFlow<List<String>> = cities
        .map { cityList ->
            cityList.mapNotNull { it.country }.distinct()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredCities: StateFlow<List<CityEntity>> = combine(
        cities,
        searchQuery,
        selectedCountry
    ) { cityList, query, country ->
        cityList.filter { city ->
            val matchesSearch = if (query.isEmpty()) {
                true
            } else {
                val searchLower = query.lowercase()
                city.locality?.lowercase()?.contains(searchLower) == true ||
                        city.country?.lowercase()?.contains(searchLower) == true ||
                        city.administrativeArea?.lowercase()?.contains(searchLower) == true ||
                        city.address.lowercase().contains(searchLower)
            }

            val matchesCountry = if (country == null) {
                true
            } else {
                city.country == country
            }

            matchesSearch && matchesCountry
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCountry(country: String?) {
        _selectedCountry.value = country
    }

    fun deleteCity(city: CityEntity) {
        viewModelScope.launch {
            repository.deleteCity(city)
        }
    }

    fun deleteCities(cities: List<CityEntity>) {
        viewModelScope.launch {
            cities.forEach { city ->
                repository.deleteCity(city)
            }
        }
    }
}

class ListViewModelFactory(private val repository: CityRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}