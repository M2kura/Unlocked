package com.example.unlocked.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unlocked.UnlockedApplication
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
            OutlinedTextField(
                value = cityAddress,
                onValueChange = { cityAddress = it },
                label = { Text("City Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = saveState !is AddCityViewModel.SaveState.Saving
            )

            if (saveState is AddCityViewModel.SaveState.Error) {
                Text(
                    text = (saveState as AddCityViewModel.SaveState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (cityAddress.isNotBlank()) {
                        viewModel.saveCity(cityAddress)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = cityAddress.isNotBlank() && saveState !is AddCityViewModel.SaveState.Saving
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