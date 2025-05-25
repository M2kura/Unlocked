package com.example.unlocked

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.unlocked.navigation.Screen
import com.example.unlocked.navigation.bottomNavItems
import com.example.unlocked.notification.NotificationScheduler
import com.example.unlocked.ui.components.NotificationPermissionDialog
import com.example.unlocked.utils.NotificationUtils
import com.example.unlocked.ui.screens.*
import com.example.unlocked.ui.theme.UnlockedTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnlockedTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val application = context.applicationContext as UnlockedApplication

    var showPermissionDialog by remember { mutableStateOf(false) }
    var hasCheckedPermission by remember { mutableStateOf(false) }

    // Check notification permission on app start
    LaunchedEffect(Unit) {
        if (!hasCheckedPermission) {
            hasCheckedPermission = true
            val hasPermission = NotificationUtils.hasNotificationPermission(context)
            val notificationsEnabled = application.preferencesManager.weeklyNotificationsEnabled.first()

            if (!hasPermission && notificationsEnabled) {
                showPermissionDialog = true
            }
        }
    }

    if (showPermissionDialog) {
        NotificationPermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onPermissionResult = { granted ->
                showPermissionDialog = false
                if (granted) {
                    // Permission granted, notifications will work
                    NotificationScheduler.scheduleWeeklyNotifications(context)
                } else {
                    // Permission denied, disable notifications in preferences
                    GlobalScope.launch {
                        application.preferencesManager.setWeeklyNotificationsEnabled(false)
                    }
                }
            }
        )
    }

    val showBottomBar = when (currentRoute) {
        Screen.AddCity.route -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        },
        contentWindowInsets = if (showBottomBar) {
            WindowInsets.navigationBars
        } else {
            WindowInsets(0)
        }
    ) { innerPadding ->
        NavigationGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.List.route,
        modifier = modifier
    ) {
        composable(Screen.List.route) {
            ListScreen(
                onAddClick = {
                    navController.navigate(Screen.AddCity.route)
                }
            )
        }
        composable(Screen.Map.route) {
            MapScreen()
        }
        composable(Screen.Stats.route) {
            StatsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.AddCity.route) {
            AddCityScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}