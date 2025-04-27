package com.example.unlocked.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun PlacesAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onPlaceSelected: (PlaceResult) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Search for a city",
    enabled: Boolean = true
) {
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showPredictions by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    val token = remember { AutocompleteSessionToken.newInstance() }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                showPredictions = true

                scope.launch {
                    if (newValue.isNotBlank()) {
                        isLoading = true
                        try {
                            val request = FindAutocompletePredictionsRequest.builder()
                                .setTypeFilter(TypeFilter.CITIES)
                                .setSessionToken(token)
                                .setQuery(newValue)
                                .build()

                            placesClient.findAutocompletePredictions(request)
                                .addOnSuccessListener { response ->
                                    predictions = response.autocompletePredictions
                                    isLoading = false
                                }
                                .addOnFailureListener { exception ->
                                    isLoading = false
                                    predictions = emptyList()
                                    exception.printStackTrace()
                                }
                        } catch (e: Exception) {
                            isLoading = false
                            predictions = emptyList()
                            e.printStackTrace()
                        }
                    } else {
                        predictions = emptyList()
                        isLoading = false
                    }
                }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { showPredictions = false })
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        if (showPredictions && predictions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                LazyColumn {
                    items(predictions) { prediction ->
                        TextButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val placeFields = listOf(
                                            Place.Field.ID,
                                            Place.Field.ADDRESS,
                                            Place.Field.LAT_LNG,
                                            Place.Field.ADDRESS_COMPONENTS,
                                            Place.Field.VIEWPORT,
                                            Place.Field.NAME
                                        )

                                        val request = FetchPlaceRequest.builder(
                                            prediction.placeId,
                                            placeFields
                                        ).build()

                                        placesClient.fetchPlace(request)
                                            .addOnSuccessListener { response ->
                                                val place = response.place
                                                val placeResult = createPlaceResult(place)
                                                onPlaceSelected(placeResult)
                                                onValueChange(place.address ?: prediction.getFullText(null).toString())
                                                showPredictions = false
                                            }
                                            .addOnFailureListener { exception ->
                                                exception.printStackTrace()
                                            }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = prediction.getPrimaryText(null).toString(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = prediction.getSecondaryText(null).toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PlaceResult(
    val placeId: String?,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val country: String?,
    val administrativeArea: String?,
    val locality: String?,
    val formattedAddress: String?,
    val viewport: ViewportBounds?,
    val approximateArea: Double?
)

data class ViewportBounds(
    val northEastLat: Double,
    val northEastLng: Double,
    val southWestLat: Double,
    val southWestLng: Double
)

private fun createPlaceResult(place: Place): PlaceResult {
    var country: String? = null
    var administrativeArea: String? = null
    var locality: String? = null

    place.addressComponents?.asList()?.forEach { component ->
        when {
            component.types.contains("country") -> country = component.name
            component.types.contains("administrative_area_level_1") -> administrativeArea = component.name
            component.types.contains("locality") -> locality = component.name
        }
    }

    val viewportBounds = place.viewport?.let {
        ViewportBounds(
            northEastLat = it.northeast.latitude,
            northEastLng = it.northeast.longitude,
            southWestLat = it.southwest.latitude,
            southWestLng = it.southwest.longitude
        )
    }

    val approximateArea = viewportBounds?.let { bounds ->
        calculateApproximateArea(bounds)
    }

    return PlaceResult(
        placeId = place.id,
        address = place.name ?: place.address ?: "",
        latitude = place.latLng?.latitude,
        longitude = place.latLng?.longitude,
        country = country,
        administrativeArea = administrativeArea,
        locality = locality,
        formattedAddress = place.address,
        viewport = viewportBounds,
        approximateArea = approximateArea
    )
}

private fun calculateApproximateArea(bounds: ViewportBounds): Double {
    val earthRadius = 6371.0 // km

    val lat1 = Math.toRadians(bounds.southWestLat)
    val lon1 = Math.toRadians(bounds.southWestLng)
    val lat2 = Math.toRadians(bounds.northEastLat)
    val lon2 = Math.toRadians(bounds.northEastLng)

    // Calculate distance between the points
    val dLat = lat2 - lat1
    val dLon = lon2 - lon1

    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val diagonal = earthRadius * c

    // Approximation: assuming rectangular area
    val width = earthRadius * cos((lat1 + lat2) / 2) * (lon2 - lon1)
    val height = earthRadius * dLat

    return abs(width * height)
}