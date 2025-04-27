package com.example.unlocked.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unlocked.UnlockedApplication
import com.example.unlocked.ui.viewmodel.SettingsViewModel
import com.example.unlocked.ui.viewmodel.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
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

    val scrollState = rememberScrollState()

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
            }
        }
        is SettingsViewModel.DeleteState.Success -> {
            LaunchedEffect(deleteState) {
                viewModel.resetDeleteState()
            }
        }
        else -> {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // App Information Section
            SettingsSection(
                title = "App Information",
                icon = Icons.Default.Info
            ) {
                SettingsInfoItem(
                    title = "Version",
                    description = "1.0.0",
                    icon = Icons.Default.Info
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsInfoItem(
                    title = "Developer",
                    description = "Heorhii Teteria",
                    icon = Icons.Default.Person
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data & Storage Section
            SettingsSection(
                title = "Data & Storage",
                icon = Icons.Default.Storage
            ) {
                SettingsActionItem(
                    title = "Delete All Data",
                    description = "Remove all unlocked cities from your device",
                    icon = Icons.Default.DeleteForever,
                    isDestructive = true,
                    enabled = deleteState !is SettingsViewModel.DeleteState.Deleting,
                    onClick = { showConfirmDialog = true },
                    isLoading = deleteState is SettingsViewModel.DeleteState.Deleting
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Section
            SettingsSection(
                title = "About",
                icon = Icons.Default.Info
            ) {
                SettingsInfoItem(
                    title = "About Unlocked",
                    description = "Track the countries and cities you've visited",
                    icon = Icons.Default.Info
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsInfoItem(
                    title = "Privacy Policy",
                    description = "Learn how we handle your data",
                    icon = Icons.Default.Security
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingsInfoItem(
                    title = "Terms of Service",
                    description = "Read our terms and conditions",
                    icon = Icons.Default.Article
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            content()
        }
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        color = if (isDestructive) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsInfoItem(
    title: String,
    description: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}