package com.company.cavitrack.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.company.cavitrack.R
import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.presentation.addupdate.AddUpdateActionScreen
import com.company.cavitrack.presentation.auth.AuthState
import com.company.cavitrack.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    var showUpdateSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    if (authState is AuthState.Deleting) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Text(stringResource(R.string.msg_deleting_account))
            }
        }
    } else if (authState !is AuthState.Authenticated) {
        // Show auth graph
        CaviTrackAuthGraph(
            onAuthSuccess = { authViewModel.checkAuthStatus() }
        )
    } else {
        val homeLabel = stringResource(R.string.nav_home)
        val inventoryLabel = stringResource(R.string.nav_inventory)
        val historyLabel = stringResource(R.string.nav_history)
        val settingsLabel = stringResource(R.string.nav_settings)
        val topLevelItems = remember(homeLabel, inventoryLabel, historyLabel, settingsLabel) {
            listOf(
                BottomNavItem(homeLabel, Route.Home::class, Route.Home, Icons.Filled.Home),
                BottomNavItem(inventoryLabel, Route.Inventory::class, Route.Inventory, Icons.AutoMirrored.Filled.List),
                BottomNavItem(historyLabel, Route.History::class, Route.History, Icons.Filled.History),
                BottomNavItem(settingsLabel, Route.Settings::class, Route.Settings, Icons.Filled.Settings)
            )
        }
        val currentItem = topLevelItems.find { item -> currentDestination?.hasRoute(item.routeClass) == true }
        val isTopLevel = currentItem != null

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (isTopLevel) {
                    NavigationBar {
                        topLevelItems.forEach { item ->
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
                }
            },
            floatingActionButton = {
                val showFab = currentDestination?.hierarchy?.any { 
                    it.hasRoute(Route.Home::class) || it.hasRoute(Route.Inventory::class) 
                } == true
                
                if (showFab) {
                    FloatingActionButton(
                        onClick = { showUpdateSheet = true },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Filled.Add, "Add Update")
                    }
                }
            }
        ) { innerPadding ->
            CaviTrackNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                authViewModel = authViewModel
            )
            
            if (showUpdateSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showUpdateSheet = false },
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) {
                    AddUpdateActionScreen(
                        entityType = null,
                        onNavigateToManual = { type ->
                            showUpdateSheet = false
                            navController.navigate(Route.ManualUpdate(type ?: EntityType.Component, null))
                        },
                        onNavigateToPhoto = { type ->
                            showUpdateSheet = false
                            navController.navigate(Route.PhotoUpdate(type ?: EntityType.Component, null))
                        }
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String, 
    val routeClass: kotlin.reflect.KClass<out Any>, 
    val routeObj: Any, 
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
