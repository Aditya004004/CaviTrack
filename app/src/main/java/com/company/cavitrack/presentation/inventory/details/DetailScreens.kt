package com.company.cavitrack.presentation.inventory.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.cavitrack.presentation.components.UiState
import com.company.cavitrack.presentation.inventory.InventoryViewModel

@Composable
fun ComponentDetailScreen(
    entityId: String,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (uiState) {
        is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${(uiState as UiState.Error).message}") }
        is UiState.Success -> {
            val component = (uiState as UiState.Success).data.components.find { it.id == entityId || it.sku == entityId }
            if (component != null) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Component Details", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!component.photoUrl.isNullOrEmpty()) {
                        coil.compose.AsyncImage(
                            model = component.photoUrl,
                            contentDescription = "Component Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text("ID: ${component.id}")
                    Text("Name: ${component.name}")
                    Text("SKU: ${component.sku}")
                    Text("Category: ${component.category}")
                    Text("Quantity: ${component.qty} ${component.unit}")
                    Text("Min Stock: ${component.minStockThreshold}")
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Component not found") }
            }
        }
    }
}

@Composable
fun CustomerDetailScreen(
    entityId: String,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (uiState) {
        is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${(uiState as UiState.Error).message}") }
        is UiState.Success -> {
            val customer = (uiState as UiState.Success).data.customers.find { it.id == entityId }
            if (customer != null) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Customer Details", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!customer.photoUrl.isNullOrEmpty()) {
                        coil.compose.AsyncImage(
                            model = customer.photoUrl,
                            contentDescription = "Customer Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text("ID: ${customer.id}")
                    Text("Name: ${customer.name}")
                    Text("Phone: ${customer.phone}")
                    Text("Email: ${customer.email}")
                    Text("Address: ${customer.address}")
                    Text("Notes: ${customer.notes}")
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Customer not found") }
            }
        }
    }
}

@Composable
fun MoldDetailScreen(
    entityId: String,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (uiState) {
        is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${(uiState as UiState.Error).message}") }
        is UiState.Success -> {
            val mold = (uiState as UiState.Success).data.molds.find { it.id == entityId || it.moldCode == entityId }
            if (mold != null) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Mold Details", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!mold.photoUrl.isNullOrEmpty()) {
                        coil.compose.AsyncImage(
                            model = mold.photoUrl,
                            contentDescription = "Mold Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text("ID: ${mold.id}")
                    Text("Mold Code: ${mold.moldCode}")
                    Text("Cavity Count: ${mold.cavityCount}")
                    Text("Status: ${mold.status.name}")
                    Text("Location: ${mold.location}")
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Mold not found") }
            }
        }
    }
}
