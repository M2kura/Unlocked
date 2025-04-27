package com.example.unlocked.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unlocked.UnlockedApplication
import com.example.unlocked.data.entity.CityEntity
import com.example.unlocked.ui.utils.CountryFlagUtils
import com.example.unlocked.ui.viewmodel.ListViewModel
import com.example.unlocked.ui.viewmodel.ListViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredCities by viewModel.filteredCities.collectAsState()
    val selectedCountries by viewModel.selectedCountries.collectAsState()
    val availableCountries by viewModel.availableCountries.collectAsState()
    val selectedCities = remember { mutableStateListOf<CityEntity>() }
    var isSelectionMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf<CityEntity?>(null) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Delete Cities") },
            text = {
                Text(
                    if (selectedCities.size == 1) {
                        "Are you sure you want to delete ${selectedCities[0].locality ?: selectedCities[0].address}?"
                    } else {
                        "Are you sure you want to delete ${selectedCities.size} cities?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCities(selectedCities.toList())
                        showDeleteDialog = false
                        selectedCities.clear()
                        isSelectionMode = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // City details dialog
    if (showDetailsDialog && selectedCity != null) {
        Dialog(onDismissRequest = { showDetailsDialog = false }) {
            CityDetailsCard(
                city = selectedCity!!,
                onDismiss = { showDetailsDialog = false }
            )
        }
    }

    // Country filter dialog
    if (showFilterDialog) {
        CountryFilterDialog(
            availableCountries = availableCountries,
            selectedCountries = selectedCountries,
            onDismiss = { showFilterDialog = false },
            onCountryToggle = { country ->
                viewModel.toggleCountryFilter(country)
            },
            onSelectAll = {
                viewModel.clearCountryFilters()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isSelectionMode) "${selectedCities.size} selected"
                        else "Unlocked Cities",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedCities.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel selection")
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = selectedCities.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                        }
                    } else {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (selectedCountries.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    LocalContentColor.current
                                }
                            )
                        }
                        IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                            Icon(
                                imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (isSearchVisible) "Close search" else "Search"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(visible = isSearchVisible && !isSelectionMode) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Active filters display
            if (selectedCountries.isNotEmpty() && !isSelectionMode) {
                ActiveFiltersRow(
                    selectedCountries = selectedCountries,
                    onRemoveCountry = { viewModel.toggleCountryFilter(it) }
                )
            }

            if (filteredCities.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (cities.isEmpty()) {
                            "No cities unlocked yet"
                        } else if (searchQuery.isNotEmpty() || selectedCountries.isNotEmpty()) {
                            "No cities match your filters"
                        } else {
                            "No cities found"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredCities,
                        key = { it.id }
                    ) { city ->
                        val isSelected = selectedCities.contains(city)

                        CityListItem(
                            city = city,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) {
                                        selectedCities.remove(city)
                                        if (selectedCities.isEmpty()) {
                                            isSelectionMode = false
                                        }
                                    } else {
                                        selectedCities.add(city)
                                    }
                                } else {
                                    selectedCity = city
                                    showDetailsDialog = true
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    isSelectionMode = true
                                    selectedCities.add(city)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountryFilterDialog(
    availableCountries: List<String>,
    selectedCountries: Set<String>,
    onDismiss: () -> Unit,
    onCountryToggle: (String) -> Unit,
    onSelectAll: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                // Dialog title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter by Country",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider()

                // "All" option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectAll() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedCountries.isEmpty(),
                        onClick = { onSelectAll() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("All countries")
                }

                Divider()

                // Country list
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(availableCountries.sorted()) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCountryToggle(country) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedCountries.contains(country),
                                onCheckedChange = null, // Handled by row click
                                enabled = selectedCountries.isNotEmpty()
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(CountryFlagUtils.getCountryEmoji(country))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(country)
                        }
                    }
                }

                Divider()

                // Apply button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveFiltersRow(
    selectedCountries: Set<String>,
    onRemoveCountry: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(selectedCountries.toList()) { country ->
                AssistChip(
                    onClick = { onRemoveCountry(country) },
                    label = { Text("${CountryFlagUtils.getCountryEmoji(country)} $country") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove filter",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CityListItem(
    city: CityEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val scale = animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null, // Handled by card click
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Text(
                text = CountryFlagUtils.getCountryEmoji(city.country),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.locality ?: city.address,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = listOfNotNull(city.administrativeArea, city.country).joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (!isSelectionMode) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search cities, countries...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CityDetailsCard(
    city: CityEntity,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = city.locality ?: city.address,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                icon = Icons.Default.Flag,
                label = "Country",
                value = "${CountryFlagUtils.getCountryEmoji(city.country)} ${city.country ?: "Unknown"}"
            )

            city.administrativeArea?.let { state ->
                InfoRow(
                    icon = Icons.Default.LocationCity,
                    label = "State/Province",
                    value = state
                )
            }

            city.formattedAddress?.let { address ->
                InfoRow(
                    icon = Icons.Default.Place,
                    label = "Full Address",
                    value = address
                )
            }

            city.area?.let { area ->
                InfoRow(
                    icon = Icons.Default.Landscape,
                    label = "Area",
                    value = String.format("%.2f km²", area)
                )
            }

            city.population?.let { population ->
                InfoRow(
                    icon = Icons.Default.People,
                    label = "Population",
                    value = formatPopulation(population)
                )
            }

            city.elevation?.let { elevation ->
                InfoRow(
                    icon = Icons.Default.Terrain,
                    label = "Elevation",
                    value = String.format("%.0f m", elevation)
                )
            }

            InfoRow(
                icon = Icons.Default.CalendarToday,
                label = "Unlocked Date",
                value = formatDate(city.unlockDate)
            )

            if (city.latitude != null && city.longitude != null) {
                InfoRow(
                    icon = Icons.Default.MyLocation,
                    label = "Coordinates",
                    value = String.format("%.4f, %.4f", city.latitude, city.longitude)
                )
            }
        }
    }
}

fun formatPopulation(population: Int): String {
    return when {
        population >= 1_000_000 -> String.format("%.1fM", population / 1_000_000.0)
        population >= 1_000 -> String.format("%.1fK", population / 1_000.0)
        else -> population.toString()
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}