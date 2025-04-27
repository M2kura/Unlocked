package com.example.unlocked.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unlocked.UnlockedApplication
import com.example.unlocked.data.entity.CityEntity
import com.example.unlocked.ui.utils.CountryFlagUtils
import com.example.unlocked.ui.viewmodel.StatsViewModel
import com.example.unlocked.ui.viewmodel.StatsViewModelFactory
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(
        factory = StatsViewModelFactory(
            (LocalContext.current.applicationContext as UnlockedApplication).repository
        )
    )
) {
    val cities by viewModel.cities.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistics",
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
        if (cities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No statistics available yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start unlocking cities to see your stats",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                OverviewStats(cities)

                TopCountriesSection(cities)

                GeographicStats(cities)

                PopulationStats(cities)

                TimelineStats(cities)

                FunFacts(cities)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun OverviewStats(cities: List<CityEntity>) {
    val totalCities = cities.size
    val totalCountries = cities.mapNotNull { it.country }.distinct().size
    val totalPopulation = cities.sumOf { it.population?.toDouble() ?: 0.0 }
    val totalArea = cities.sumOf { it.area ?: 0.0 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.LocationCity,
                    value = totalCities.toString(),
                    label = "Cities"
                )
                StatItem(
                    icon = Icons.Default.Flag,
                    value = totalCountries.toString(),
                    label = "Countries"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.People,
                    value = formatLargeNumber(totalPopulation),
                    label = "Population"
                )
                StatItem(
                    icon = Icons.Default.Map,
                    value = "${formatLargeNumber(totalArea)} km²",
                    label = "Area"
                )
            }
        }
    }
}

@Composable
fun TopCountriesSection(cities: List<CityEntity>) {
    val countryCounts = cities
        .groupBy { it.country }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    if (countryCounts.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Top Countries",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            countryCounts.forEachIndexed { index, (country, count) ->
                if (index > 0) Spacer(modifier = Modifier.height(12.dp))

                CountryRankItem(
                    rank = index + 1,
                    country = country ?: "Unknown",
                    count = count,
                    totalCities = cities.size
                )
            }
        }
    }
}

@Composable
fun CountryRankItem(
    rank: Int,
    country: String,
    count: Int,
    totalCities: Int
) {
    val percentage = (count.toFloat() / totalCities) * 100

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CountryFlagUtils.getCountryEmoji(country),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = country,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${percentage.roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GeographicStats(cities: List<CityEntity>) {
    val citiesWithCoords = cities.filter { it.latitude != null && it.longitude != null }

    if (citiesWithCoords.isEmpty()) return

    val northernmost = citiesWithCoords.maxByOrNull { it.latitude!! }
    val southernmost = citiesWithCoords.minByOrNull { it.latitude!! }
    val easternmost = citiesWithCoords.maxByOrNull { it.longitude!! }
    val westernmost = citiesWithCoords.minByOrNull { it.longitude!! }
    val highestElevation = cities.filter { it.elevation != null }.maxByOrNull { it.elevation!! }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Geographic Extremes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            northernmost?.let {
                ExtremeLocationItem(
                    icon = Icons.Default.North,
                    label = "Northernmost",
                    city = it
                )
            }

            southernmost?.let {
                if (it != northernmost) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExtremeLocationItem(
                        icon = Icons.Default.South,
                        label = "Southernmost",
                        city = it
                    )
                }
            }

            easternmost?.let {
                Spacer(modifier = Modifier.height(12.dp))
                ExtremeLocationItem(
                    icon = Icons.Default.East,
                    label = "Easternmost",
                    city = it
                )
            }

            westernmost?.let {
                if (it != easternmost) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExtremeLocationItem(
                        icon = Icons.Default.West,
                        label = "Westernmost",
                        city = it
                    )
                }
            }

            highestElevation?.let {
                Spacer(modifier = Modifier.height(12.dp))
                ExtremeLocationItem(
                    icon = Icons.Default.Terrain,
                    label = "Highest Elevation",
                    city = it,
                    extraInfo = "${it.elevation?.roundToInt()} m"
                )
            }
        }
    }
}

@Composable
fun ExtremeLocationItem(
    icon: ImageVector,
    label: String,
    city: CityEntity,
    extraInfo: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = city.locality ?: city.address,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = city.country ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        extraInfo?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PopulationStats(cities: List<CityEntity>) {
    val citiesWithPopulation = cities.filter { it.population != null && it.population > 0 }

    if (citiesWithPopulation.isEmpty()) return

    val mostPopulated = citiesWithPopulation.maxByOrNull { it.population!! }
    val leastPopulated = citiesWithPopulation.minByOrNull { it.population!! }
    val averagePopulation = citiesWithPopulation.map { it.population!!.toDouble() }.average()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Population Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            mostPopulated?.let {
                PopulationItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Most Populated",
                    city = it,
                    population = it.population!!
                )
            }

            leastPopulated?.let {
                if (it != mostPopulated) {
                    Spacer(modifier = Modifier.height(12.dp))
                    PopulationItem(
                        icon = Icons.Default.TrendingDown,
                        label = "Least Populated",
                        city = it,
                        population = it.population!!
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Average Population",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatLargeNumber(averagePopulation),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PopulationItem(
    icon: ImageVector,
    label: String,
    city: CityEntity,
    population: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = city.locality ?: city.address,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = formatLargeNumber(population.toDouble()),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimelineStats(cities: List<CityEntity>) {
    val sortedByDate = cities.sortedBy { it.unlockDate }
    val firstUnlocked = sortedByDate.firstOrNull()
    val lastUnlocked = sortedByDate.lastOrNull()

    // Group by month
    val monthlyStats = cities.groupBy {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = it.unlockDate
        "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH) + 1}"
    }.mapValues { it.value.size }

    val mostActiveMonth = monthlyStats.maxByOrNull { it.value }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Timeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            firstUnlocked?.let {
                TimelineItem(
                    icon = Icons.Default.Rocket,
                    label = "First City Unlocked",
                    city = it,
                    date = formatDateForStats(it.unlockDate)
                )
            }

            lastUnlocked?.let {
                if (it != firstUnlocked) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TimelineItem(
                        icon = Icons.Default.NewReleases,
                        label = "Most Recent Unlock",
                        city = it,
                        date = formatDateForStats(it.unlockDate)
                    )
                }
            }

            mostActiveMonth?.let { (month, count) ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Most Active Month",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatMonth(month),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$count cities unlocked",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    icon: ImageVector,
    label: String,
    city: CityEntity,
    date: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = city.locality ?: city.address,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FunFacts(cities: List<CityEntity>) {
    val facts = mutableListOf<Pair<String, String>>()

    // Longest city name
    cities.maxByOrNull { it.locality?.length ?: 0 }?.let { city ->
        city.locality?.let { name ->
            facts.add("Longest City Name" to "$name (${name.length} characters)")
        }
    }

    // Country with most area
    cities.groupBy { it.country }
        .mapValues { entry -> entry.value.sumOf { it.area ?: 0.0 } }
        .maxByOrNull { it.value }
        ?.let { (country, area) ->
            country?.let {
                facts.add("Country with Most Area" to "$country (${formatLargeNumber(area)} km²)")
            }
        }

    // Average time between unlocks
    if (cities.size > 1) {
        val sortedByDate = cities.sortedBy { it.unlockDate }
        val timeDiffs = sortedByDate.zipWithNext { a, b -> b.unlockDate - a.unlockDate }
        val avgTimeDiff = timeDiffs.average()
        val avgDays = (avgTimeDiff / (1000 * 60 * 60 * 24)).roundToInt()
        facts.add("Average Time Between Unlocks" to "$avgDays days")
    }

    if (facts.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Fun Facts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            facts.forEachIndexed { index, (label, value) ->
                if (index > 0) Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

private fun formatLargeNumber(number: Double): String {
    return when {
        number >= 1_000_000_000 -> String.format("%.1fB", number / 1_000_000_000)
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000)
        number >= 1_000 -> String.format("%.1fK", number / 1_000)
        else -> number.roundToInt().toString()
    }
}

private fun formatDateForStats(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

private fun formatMonth(monthYear: String): String {
    val parts = monthYear.split("-")
    if (parts.size != 2) return monthYear

    val year = parts[0]
    val month = parts[1].toIntOrNull() ?: return monthYear

    val monthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    return "${monthNames.getOrNull(month - 1) ?: month} $year"
}