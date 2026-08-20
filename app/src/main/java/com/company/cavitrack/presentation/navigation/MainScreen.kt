package com.company.cavitrack.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.company.cavitrack.presentation.auth.AuthState
import com.company.cavitrack.presentation.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    if (authState !is AuthState.Authenticated) {
        // Show auth graph
        CaviTrackAuthGraph(
            authViewModel = authViewModel,
            onAuthSuccess = { authViewModel.checkAuthStatus() }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("CaviTrack") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    val items = listOf(
                        BottomNavItem("Home", Route.Home::class, Route.Home, Icons.Filled.Home),
                        BottomNavItem("Inventory", Route.Inventory::class, Route.Inventory, Icons.AutoMirrored.Filled.List),
                        BottomNavItem("History", Route.History::class, Route.History, Icons.Filled.History),
                        BottomNavItem("Settings", Route.Settings::class, Route.Settings, Icons.Filled.Settings)
                    )
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.hasRoute(item.routeClass) 
                        } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.routeObj) {
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
            },
            floatingActionButton = {
                val showFab = currentDestination?.hierarchy?.any { 
                    it.hasRoute(Route.Home::class) || it.hasRoute(Route.Inventory::class) 
                } == true
                
                if (showFab) {
                    FloatingActionButton(
                        onClick = { navController.navigate(Route.AddUpdateAction(null)) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Filled.Add, "Add Update")
                    }
                }
            }
        ) { innerPadding ->
            CaviTrackNavGraph(
                navController = navController,
                authViewModel = authViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

data class BottomNavItem(
    val label: String, 
    val routeClass: kotlin.reflect.KClass<out Any>, 
    val routeObj: Any, 
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)


