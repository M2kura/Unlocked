package com.example.unlocked.navigation

sealed class Screen(val route: String) {
    object Map : Screen("map")
    object List : Screen("list")
    object Stats : Screen("stats")
    object Profile : Screen("profile")
}