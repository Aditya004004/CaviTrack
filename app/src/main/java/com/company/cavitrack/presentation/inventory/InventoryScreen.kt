package com.company.cavitrack.presentation.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.cavitrack.presentation.components.*
import com.company.cavitrack.domain.model.*

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Components", "Customers", "Molds")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when {
            uiState.isLoading -> LoadingState()
            uiState.error != null -> ErrorState(message = uiState.error!!, onRetry = { viewModel.loadData() })
            uiState.data != null -> {
                val data = uiState.data!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> {
                            if (data.components.isEmpty()) item { EmptyState("No components found") }
                            items(data.components) { component ->
                                ComponentItem(component)
                            }
                        }
                        1 -> {
                            if (data.customers.isEmpty()) item { EmptyState("No customers found") }
                            items(data.customers) { customer ->
                                CustomerItem(customer)
                            }
                        }
                        2 -> {
                            if (data.molds.isEmpty()) item { EmptyState("No molds found") }
                            items(data.molds) { mold ->
                                MoldItem(mold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentItem(component: Component) {
    ListCard(onClick = { /* TODO */ }) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = component.name, fontWeight = FontWeight.Bold)
                Text(text = "SKU: ${component.sku}", style = MaterialTheme.typography.bodyMedium)
            }
            val isLowStock = component.qty < component.minStockThreshold
            StatusBadge(
                text = "${component.qty} ${component.unit}",
                statusType = if (isLowStock) StatusType.WARNING else StatusType.SUCCESS
            )
        }
    }
}

@Composable
fun CustomerItem(customer: Customer) {
    ListCard(onClick = { /* TODO */ }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = customer.name, fontWeight = FontWeight.Bold)
            Text(text = customer.email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MoldItem(mold: Mold) {
    ListCard(onClick = { /* TODO */ }) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = mold.moldCode, fontWeight = FontWeight.Bold)
                Text(text = "${mold.cavityCount} cavities", style = MaterialTheme.typography.bodyMedium)
            }
            StatusBadge(
                text = mold.status,
                statusType = when(mold.status) {
                    "Active" -> StatusType.SUCCESS
                    "In Maintenance" -> StatusType.WARNING
                    else -> StatusType.NEUTRAL
                }
            )
        }
    }
}
