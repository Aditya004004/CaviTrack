package com.company.cavitrack.presentation.navigation

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
    val inventoryViewModel: com.company.cavitrack.presentation.inventory.InventoryViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier
    ) {
        composable<Route.Home> { 
            HomeScreen(
                onNavigateToDetail = { type, id -> 
                    when(type) {
                        "Component" -> navController.navigate(Route.ComponentDetail(id))
                        "Customer" -> navController.navigate(Route.CustomerDetail(id))
                        "Mold" -> navController.navigate(Route.MoldDetail(id))
                    }
                }
            ) 
        }
        composable<Route.Inventory> { 
            InventoryScreen(
                viewModel = inventoryViewModel,
                onComponentClick = { id -> navController.navigate(Route.ComponentDetail(id)) },
                onCustomerClick = { id -> navController.navigate(Route.CustomerDetail(id)) },
                onMoldClick = { id -> navController.navigate(Route.MoldDetail(id)) }
            ) 
        }
        composable<Route.History> { 
            HistoryScreen(
                onNavigateToDetail = { type, id -> 
                    when(type) {
                        "Component" -> navController.navigate(Route.ComponentDetail(id))
                        "Customer" -> navController.navigate(Route.CustomerDetail(id))
                        "Mold" -> navController.navigate(Route.MoldDetail(id))
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
                onUpdateComplete = { navController.popBackStack(Route.Home, inclusive = false) }
            )
        }
        
        composable<Route.PhotoUpdate> { backStackEntry ->
            val route: Route.PhotoUpdate = backStackEntry.toRoute()
            PhotoUpdateScreen(
                entityType = route.entityType, 
                entityId = route.entityId,
                onUpdateComplete = { navController.popBackStack(Route.Home, inclusive = false) }
            )
        }
        
        composable<Route.ComponentDetail> { backStackEntry -> 
            val route: Route.ComponentDetail = backStackEntry.toRoute()
            com.company.cavitrack.presentation.inventory.details.ComponentDetailScreen(
                entityId = route.id,
                viewModel = inventoryViewModel,
                onNavigateToUpdate = { id -> navController.navigate(Route.ManualUpdate("Component", id)) }
            )
        }
        composable<Route.CustomerDetail> { backStackEntry -> 
            val route: Route.CustomerDetail = backStackEntry.toRoute()
            com.company.cavitrack.presentation.inventory.details.CustomerDetailScreen(entityId = route.id, viewModel = inventoryViewModel)
        }
        composable<Route.MoldDetail> { backStackEntry -> 
            val route: Route.MoldDetail = backStackEntry.toRoute()
            com.company.cavitrack.presentation.inventory.details.MoldDetailScreen(entityId = route.id, viewModel = inventoryViewModel)
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
                onNavigateToRegister = { navController.navigate(Route.Register) }
            )
        }
        composable<Route.Register> {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = onAuthSuccess,
                onNavigateToLogin = { navController.navigate(Route.Login) }
            )
        }
    }
}
