package com.example.unlocked.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "Map",
        icon = Icons.Default.Map,
        route = Screen.Map.route
    ),
    BottomNavItem(
        label = "List",
        icon = Icons.Default.ListAlt,
        route = Screen.List.route
    ),
    BottomNavItem(
        label = "Stats",
        icon = Icons.Default.BarChart,
        route = Screen.Stats.route
    ),
    BottomNavItem(
        label = "Profile",
        icon = Icons.Default.Person,
        route = Screen.Profile.route
    )
)