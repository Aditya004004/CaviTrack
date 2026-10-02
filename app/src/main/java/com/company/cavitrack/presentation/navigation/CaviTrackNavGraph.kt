package com.company.cavitrack.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.presentation.addupdate.manual.ManualUpdateScreen
import com.company.cavitrack.presentation.addupdate.photo.PhotoUpdateScreen
import com.company.cavitrack.presentation.auth.AuthViewModel
import com.company.cavitrack.presentation.auth.LoginScreen
import com.company.cavitrack.presentation.auth.RegisterScreen
import com.company.cavitrack.presentation.history.HistoryScreen
import com.company.cavitrack.presentation.home.HomeScreen
import com.company.cavitrack.presentation.inventory.InventoryScreen
import com.company.cavitrack.presentation.inventory.details.ComponentDetailScreen
import com.company.cavitrack.presentation.inventory.details.CustomerDetailScreen
import com.company.cavitrack.presentation.inventory.details.MoldDetailScreen
import com.company.cavitrack.presentation.settings.SettingsScreen
import com.company.cavitrack.presentation.settings.export.ExportCenterScreen

@Composable
fun CaviTrackNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier
    ) {
        composable<Route.Home> { 
            HomeScreen(
                onNavigateToDetail = { type, id -> 
                    when (type) {
                        EntityType.Component.name -> navController.navigate(Route.ComponentDetail(id)) { launchSingleTop = true }
                        EntityType.Customer.name -> navController.navigate(Route.CustomerDetail(id)) { launchSingleTop = true }
                        EntityType.Mold.name -> navController.navigate(Route.MoldDetail(id)) { launchSingleTop = true }
                        else -> { /* Ignore unknown types like History */ }
                    }
                }
            ) 
        }
        composable<Route.Inventory> { 
            InventoryScreen(
                onComponentClick = { id -> navController.navigate(Route.ComponentDetail(id)) { launchSingleTop = true } },
                onCustomerClick = { id -> navController.navigate(Route.CustomerDetail(id)) { launchSingleTop = true } },
                onMoldClick = { id -> navController.navigate(Route.MoldDetail(id)) { launchSingleTop = true } },
                onAddNewItem = { entityType -> navController.navigate(Route.ManualUpdate(entityType, null)) { launchSingleTop = true } }
            ) 
        }
        composable<Route.History> { 
            HistoryScreen(
                onNavigateToDetail = { type, id -> 
                    when (type) {
                        EntityType.Component.name -> navController.navigate(Route.ComponentDetail(id)) { launchSingleTop = true }
                        EntityType.Customer.name -> navController.navigate(Route.CustomerDetail(id)) { launchSingleTop = true }
                        EntityType.Mold.name -> navController.navigate(Route.MoldDetail(id)) { launchSingleTop = true }
                        else -> { /* Ignore unknown types */ }
                    }
                }
            ) 
        }

        composable<Route.Settings> { 
            SettingsScreen(
                authViewModel = authViewModel,
                onNavigateToExportCenter = { navController.navigate(Route.ExportCenter) { launchSingleTop = true } }
            ) 
        }
        composable<Route.ExportCenter> {
            ExportCenterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<Route.ManualUpdate> { backStackEntry ->
            val route: Route.ManualUpdate = backStackEntry.toRoute()
            ManualUpdateScreen(
                entityType = route.entityType,
                entityId = route.entityId,
                onUpdateComplete = {
                    navController.popBackStack() // pop ManualUpdate
                    if (route.entityId != null) navController.popBackStack() // also pop Detail
                }
            )
        }
        
        composable<Route.PhotoUpdate> { backStackEntry ->
            val route: Route.PhotoUpdate = backStackEntry.toRoute()
            PhotoUpdateScreen(
                entityType = route.entityType, 
                entityId = route.entityId,
                onUpdateComplete = {
                    navController.popBackStack() // pop PhotoUpdate
                    if (route.entityId != null) navController.popBackStack() // also pop Detail
                }
            )
        }
        
        composable<Route.ComponentDetail> { 
            ComponentDetailScreen(
                onNavigateToUpdate = { id -> navController.navigate(Route.ManualUpdate(EntityType.Component, id)) { launchSingleTop = true } },
                onNavigateToPhotoUpdate = { id -> navController.navigate(Route.PhotoUpdate(EntityType.Component, id)) { launchSingleTop = true } },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Route.CustomerDetail> { 
            CustomerDetailScreen(
                onNavigateToUpdate = { id -> navController.navigate(Route.ManualUpdate(EntityType.Customer, id)) { launchSingleTop = true } },
                onNavigateToPhotoUpdate = { id -> navController.navigate(Route.PhotoUpdate(EntityType.Customer, id)) { launchSingleTop = true } },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Route.MoldDetail> { 
            MoldDetailScreen(
                onNavigateToUpdate = { id -> navController.navigate(Route.ManualUpdate(EntityType.Mold, id)) { launchSingleTop = true } },
                onNavigateToPhotoUpdate = { id -> navController.navigate(Route.PhotoUpdate(EntityType.Mold, id)) { launchSingleTop = true } },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun CaviTrackAuthGraph(onAuthSuccess: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.Login) {
        composable<Route.Login> {
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                onNavigateToRegister = { 
                    navController.navigate(Route.Register)
                }
            )
        }
        composable<Route.Register> {
            RegisterScreen(
                onRegisterSuccess = onAuthSuccess,
                onNavigateToLogin = { 
                    if (!navController.popBackStack()) {
                        navController.navigate(Route.Login)
                    }
                }
            )
        }
    }
}
