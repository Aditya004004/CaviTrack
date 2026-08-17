package com.company.cavitrack.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.company.cavitrack.presentation.addupdate.AddUpdateActionScreen
import com.company.cavitrack.presentation.addupdate.manual.ManualUpdateScreen
import com.company.cavitrack.presentation.addupdate.photo.PhotoUpdateScreen
import com.company.cavitrack.presentation.history.HistoryScreen
import com.company.cavitrack.presentation.home.HomeScreen
import com.company.cavitrack.presentation.inventory.InventoryScreen
import com.company.cavitrack.presentation.settings.SettingsScreen

@Composable
fun CaviTrackNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier
    ) {
        composable<Route.Home> { HomeScreen() }
        composable<Route.Inventory> { InventoryScreen() }
        composable<Route.History> { HistoryScreen() }
        composable<Route.Settings> { SettingsScreen() }
        
        composable<Route.AddUpdateAction> { backStackEntry ->
            val route: Route.AddUpdateAction = backStackEntry.toRoute()
            AddUpdateActionScreen(entityType = route.entityType)
        }
        
        composable<Route.ManualUpdate> { backStackEntry ->
            val route: Route.ManualUpdate = backStackEntry.toRoute()
            ManualUpdateScreen(entityType = route.entityType, entityId = route.entityId)
        }
        
        composable<Route.PhotoUpdate> { backStackEntry ->
            val route: Route.PhotoUpdate = backStackEntry.toRoute()
            PhotoUpdateScreen(entityType = route.entityType, entityId = route.entityId)
        }
        
        // Detail screens can be added here
        composable<Route.ComponentDetail> { }
        composable<Route.CustomerDetail> { }
        composable<Route.MoldDetail> { }
    }
}
