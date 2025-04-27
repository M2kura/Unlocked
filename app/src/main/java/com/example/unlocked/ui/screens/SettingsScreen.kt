package com.example.unlocked.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unlocked.UnlockedApplication
import com.example.unlocked.ui.viewmodel.SettingsViewModel
import com.example.unlocked.ui.viewmodel.SettingsViewModelFactory

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            (LocalContext.current.applicationContext as UnlockedApplication).repository
        )
    )
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val deleteState by viewModel.deleteState.collectAsState()

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null) },
            title = { Text("Delete All Data") },
            text = { Text("Are you sure you want to delete all your unlocked cities? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllData()
                        showConfirmDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    when (deleteState) {
        is SettingsViewModel.DeleteState.Error -> {
            LaunchedEffect(deleteState) {
                // You might want to show a snackbar or error message here
            }
        }
        is SettingsViewModel.DeleteState.Success -> {
            LaunchedEffect(deleteState) {
                // You might want to show a success message here
                viewModel.resetDeleteState()
            }
        }
        else -> {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            TextButton(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                enabled = deleteState !is SettingsViewModel.DeleteState.Deleting
            ) {
                if (deleteState is SettingsViewModel.DeleteState.Deleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Delete all data",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete All Data",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}