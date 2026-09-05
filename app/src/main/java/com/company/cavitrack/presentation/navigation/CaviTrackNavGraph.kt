package com.company.cavitrack.presentation.navigation


import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.company.cavitrack.presentation.addupdate.AddUpdateActionScreen
import com.company.cavitrack.presentation.addupdate.manual.ManualUpdateScreen
import com.company.cavitrack.presentation.addupdate.photo.PhotoUpdateScreen
import com.company.cavitrack.presentation.history.HistoryScreen
import com.company.cavitrack.presentation.home.HomeScreen
import com.company.cavitrack.presentation.inventory.InventoryScreen
import com.company.cavitrack.presentation.settings.SettingsScreen
import com.company.cavitrack.presentation.auth.LoginScreen
import com.company.cavitrack.presentation.auth.RegisterScreen
import com.company.cavitrack.presentation.auth.AuthViewModel

@Composable
fun CaviTrackNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier
    ) {
        composable<Route.Home> { 
            HomeScreen(
                onNavigateToDetail = { type, id -> 
                    when(type) {
                        com.company.cavitrack.domain.model.EntityType.Component.name -> navController.navigate(Route.ComponentDetail(id)) { launchSingleTop = true }
                        com.company.cavitrack.domain.model.EntityType.Customer.name -> navController.navigate(Route.CustomerDetail(id)) { launchSingleTop = true }
                        com.company.cavitrack.domain.model.EntityType.Mold.name -> navController.navigate(Route.MoldDetail(id)) { launchSingleTop = true }
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
                    when(type) {
                        com.company.cavitrack.domain.model.EntityType.Component.name -> navController.navigate(Route.ComponentDetail(id)) { launchSingleTop = true }
                        com.company.cavitrack.domain.model.EntityType.Customer.name -> navController.navigate(Route.CustomerDetail(id)) { launchSingleTop = true }
                        com.company.cavitrack.domain.model.EntityType.Mold.name -> navController.navigate(Route.MoldDetail(id)) { launchSingleTop = true }
                        else -> { /* Ignore unknown types */ }
                    }
                }
            ) 
        }
        composable<Route.Settings> { SettingsScreen(authViewModel = authViewModel) }
        

        
        composable<Route.ManualUpdate> { backStackEntry ->
            val route: Route.ManualUpdate = backStackEntry.toRoute()
            ManualUpdateScreen(
                entityType = route.entityType,
                entityId = route.entityId,
                onUpdateComplete = { navController.popBackStack() }
            )
        }
        
        composable<Route.PhotoUpdate> { backStackEntry ->
            val route: Route.PhotoUpdate = backStackEntry.toRoute()
            PhotoUpdateScreen(
                entityType = route.entityType, 
                entityId = route.entityId,
                onUpdateComplete = { navController.popBackStack() }
            )
        }
        
        composable<Route.ComponentDetail> { 
            com.company.cavitrack.presentation.inventory.details.ComponentDetailScreen(
                onNavigateToUpdate = { id -> navController.navigate(Route.ManualUpdate(com.company.cavitrack.domain.model.EntityType.Component, id)) { launchSingleTop = true } },
                onNavigateToPhotoUpdate = { id -> navController.navigate(Route.PhotoUpdate(com.company.cavitrack.domain.model.EntityType.Component, id)) { launchSingleTop = true } },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Route.CustomerDetail> { 
            com.company.cavitrack.presentation.inventory.details.CustomerDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<Route.MoldDetail> { 
            com.company.cavitrack.presentation.inventory.details.MoldDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun CaviTrackAuthGraph(authViewModel: AuthViewModel, onAuthSuccess: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Route.Login) {
        composable<Route.Login> {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = onAuthSuccess,
                onNavigateToRegister = { 
                    navController.navigate(Route.Register) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }
        composable<Route.Register> {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = onAuthSuccess,
                onNavigateToLogin = { 
                    navController.navigate(Route.Login) {
                        popUpTo(Route.Register) { inclusive = true }
                    }
                }
            )
        }
    }
}


