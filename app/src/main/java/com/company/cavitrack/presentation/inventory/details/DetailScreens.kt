package com.company.cavitrack.presentation.inventory.details

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.cavitrack.presentation.components.UiState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import coil.compose.AsyncImage

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ComponentDetailScreen(
    entityId: String,
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateToUpdate: (String) -> Unit = {},
    onNavigateToPhotoUpdate: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.componentState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    LaunchedEffect(entityId) { viewModel.loadComponent(entityId) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Component Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Error: ${state.message}") }
            is UiState.Success -> {
                val component = state.data
                Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(scrollState)) {
                    if (!component.photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = component.photoUrl,
                            contentDescription = "Component Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text("Name: ${component.name}", style = MaterialTheme.typography.titleMedium)
                    Text("SKU: ${component.sku}", style = MaterialTheme.typography.bodyLarge)
                    Text("Category: ${component.category}", style = MaterialTheme.typography.bodyLarge)
                    Text("Quantity: ${component.qty} ${component.unit}", style = MaterialTheme.typography.bodyLarge)
                    Text("Min Stock: ${component.minStockThreshold}", style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { onNavigateToPhotoUpdate(component.id) },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Text("Update Photo")
                        }
                        Button(
                            onClick = { onNavigateToUpdate(component.id) },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Text("Adjust Stock")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    entityId: String,
    viewModel: DetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.customerState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    LaunchedEffect(entityId) { viewModel.loadCustomer(entityId) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Error: ${state.message}") }
            is UiState.Success -> {
                val customer = state.data
                Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(scrollState)) {
                    if (!customer.photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = customer.photoUrl,
                            contentDescription = "Customer Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text("Name: ${customer.name}", style = MaterialTheme.typography.titleMedium)
                    Text("Phone: ${customer.phone}", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: ${customer.email}", style = MaterialTheme.typography.bodyLarge)
                    Text("Address: ${customer.address}", style = MaterialTheme.typography.bodyLarge)
                    Text("Notes: ${customer.notes}", style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Note: Edit and delete functionalities are a known v1 limitation and will be added in a future update.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MoldDetailScreen(
    entityId: String,
    viewModel: DetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.moldState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    LaunchedEffect(entityId) { viewModel.loadMold(entityId) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mold Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Error: ${state.message}") }
            is UiState.Success -> {
                val mold = state.data
                Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(scrollState)) {
                    if (!mold.photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = mold.photoUrl,
                            contentDescription = "Mold Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text("Mold Code: ${mold.moldCode}", style = MaterialTheme.typography.titleMedium)
                    Text("Cavity Count: ${mold.cavityCount}", style = MaterialTheme.typography.bodyLarge)
                    Text("Status: ${mold.status.name}", style = MaterialTheme.typography.bodyLarge)
                    Text("Location: ${mold.location}", style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Note: Edit and delete functionalities are a known v1 limitation and will be added in a future update.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
