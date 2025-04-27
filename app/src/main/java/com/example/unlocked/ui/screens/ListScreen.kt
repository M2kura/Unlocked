package com.example.unlocked.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unlocked.UnlockedApplication
import com.example.unlocked.ui.viewmodel.ListViewModel
import com.example.unlocked.ui.viewmodel.ListViewModelFactory

@Composable
fun ListScreen(
    onAddClick: () -> Unit,
    viewModel: ListViewModel = viewModel(
        factory = ListViewModelFactory(
            (LocalContext.current.applicationContext as UnlockedApplication).repository
        )
    )
) {
    val cities by viewModel.cities.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (cities.isEmpty()) {
            Text(
                "No cities unlocked yet",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(cities) { city ->
                    Text(
                        text = city.address,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add"
            )
        }
    }
}