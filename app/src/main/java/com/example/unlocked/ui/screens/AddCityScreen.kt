package com.example.unlocked.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unlocked.UnlockedApplication
import com.example.unlocked.ui.components.PlaceResult
import com.example.unlocked.ui.components.PlacesAutocompleteTextField
import com.example.unlocked.ui.viewmodel.AddCityViewModel
import com.example.unlocked.ui.viewmodel.AddCityViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCityScreen(
    onBackPressed: () -> Unit,
    viewModel: AddCityViewModel = viewModel(
        factory = AddCityViewModelFactory(
            (LocalContext.current.applicationContext as UnlockedApplication).repository
        )
    )
) {
    var cityAddress by remember { mutableStateOf("") }
    var selectedPlace by remember { mutableStateOf<PlaceResult?>(null) }
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        when (saveState) {
            is AddCityViewModel.SaveState.Success -> {
                onBackPressed()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Place") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlacesAutocompleteTextField(
                value = cityAddress,
                onValueChange = { cityAddress = it },
                onPlaceSelected = { placeResult ->
                    selectedPlace = placeResult
                    cityAddress = placeResult.address
                    // Reset error state when a new place is selected
                    if (saveState is AddCityViewModel.SaveState.Error) {
                        viewModel.resetState()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = saveState !is AddCityViewModel.SaveState.Saving
            )

            // Show selected place details
            selectedPlace?.let { place ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Selected Place Details",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        place.locality?.let {
                            Text("City: $it")
                        }
                        place.administrativeArea?.let {
                            Text("State/Province: $it")
                        }
                        place.country?.let {
                            Text("Country: $it")
                        }

                        Text(
                            text = "Additional data will be fetched from Wikidata",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Show error message if there's an error
            if (saveState is AddCityViewModel.SaveState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = (saveState as AddCityViewModel.SaveState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedPlace?.let { place ->
                        viewModel.saveCity(place)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedPlace != null && saveState !is AddCityViewModel.SaveState.Saving
            ) {
                if (saveState is AddCityViewModel.SaveState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Unlock")
                }
            }
        }
    }
}