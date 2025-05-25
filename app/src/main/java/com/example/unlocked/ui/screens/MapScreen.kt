package com.example.unlocked.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unlocked.UnlockedApplication
import com.example.unlocked.data.entity.CityEntity
import com.example.unlocked.ui.utils.CountryFlagUtils
import com.example.unlocked.ui.viewmodel.MapViewModel
import com.example.unlocked.ui.viewmodel.MapViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(
            (LocalContext.current.applicationContext as UnlockedApplication).repository
        )
    )
) {
    val cities by viewModel.cities.collectAsState()
    val context = LocalContext.current

    // Default camera position
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            mapToolbarEnabled = true
        )
    }

    // Get the marker color from preferences
    val preferencesManager = remember { (context.applicationContext as UnlockedApplication).preferencesManager }
    val pinColorHex by preferencesManager.markerColor.collectAsState(initial = "#FF0000") // Default red

    // Convert hex to HSV hue value for BitmapDescriptorFactory
    val pinColorHue = remember(pinColorHex) {
        try {
            val color = android.graphics.Color.parseColor(pinColorHex)
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(color, hsv)
            hsv[0] // Hue value for BitmapDescriptorFactory
        } catch (e: Exception) {
            BitmapDescriptorFactory.HUE_RED // Default red
        }
    }

    var selectedCity by remember { mutableStateOf<CityEntity?>(null) }
    var showCityDetails by remember { mutableStateOf(false) }

    // Calculate map bounds to include all cities
    val boundsBuilder = remember(cities) {
        if (cities.isNotEmpty()) {
            val builder = LatLngBounds.builder()
            var hasValidCoordinates = false

            cities.forEach { city ->
                if (city.latitude != null && city.longitude != null) {
                    builder.include(LatLng(city.latitude, city.longitude))
                    hasValidCoordinates = true
                }
            }

            if (hasValidCoordinates) builder else null
        } else null
    }

    // Fit map to show all markers when cities change
    LaunchedEffect(boundsBuilder) {
        boundsBuilder?.let { builder ->
            try {
                val bounds = builder.build()
                val padding = 100 // px
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                cameraPositionState.animate(cameraUpdate)
            } catch (e: Exception) {
                // Fallback if bounds building fails
                cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            cities.forEach { city ->
                if (city.latitude != null && city.longitude != null) {
                    val position = LatLng(city.latitude, city.longitude)
                    Marker(
                        state = MarkerState(position = position),
                        title = city.locality ?: city.address,
                        snippet = "${city.administrativeArea ?: ""}, ${city.country ?: ""}",
                        icon = BitmapDescriptorFactory.defaultMarker(pinColorHue),
                        onClick = {
                            selectedCity = city
                            showCityDetails = true
                            true
                        }
                    )
                }
            }
        }

        // Display city count
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            )
        ) {
            Text(
                text = "Cities: ${cities.size}",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Show city details in a modal when a marker is clicked
        if (showCityDetails && selectedCity != null) {
            CityDetailsMapDialog(
                city = selectedCity!!,
                onDismiss = { showCityDetails = false }
            )
        }
    }
}

@Composable
fun CityDetailsMapDialog(
    city: CityEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CountryFlagUtils.getCountryEmoji(city.country),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = city.locality ?: city.address,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column {
                city.administrativeArea?.let {
                    Text(
                        text = "Region: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                city.country?.let {
                    Text(
                        text = "Country: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                city.population?.let {
                    Text(
                        text = "Population: ${formatMapPopulation(it)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                city.area?.let {
                    Text(
                        text = "Area: ${String.format("%.2f km²", it)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                city.elevation?.let {
                    Text(
                        text = "Elevation: ${String.format("%.0f m", it)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "Unlocked on: ${formatMapDate(city.unlockDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Helper function to format population (renamed to avoid conflicts)
private fun formatMapPopulation(population: Int): String {
    return when {
        population >= 1_000_000 -> String.format("%.1fM", population / 1_000_000.0)
        population >= 1_000 -> String.format("%.1fK", population / 1_000.0)
        else -> population.toString()
    }
}

// Helper function to format date (renamed to avoid conflicts)
private fun formatMapDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}