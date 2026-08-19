package com.company.cavitrack.presentation.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.company.cavitrack.presentation.components.*
import com.company.cavitrack.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Components", "Customers", "Molds")

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (val state = uiState) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.loadData() })
            is UiState.Success -> {
                val data = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> {
                            if (data.components.isEmpty()) item { EmptyState("No components found") }
                            items(data.components, key = { it.id }) { component ->
                                ComponentItem(component)
                            }
                        }
                        1 -> {
                            if (data.customers.isEmpty()) item { EmptyState("No customers found") }
                            items(data.customers, key = { it.id }) { customer ->
                                CustomerItem(customer)
                            }
                        }
                        2 -> {
                            if (data.molds.isEmpty()) item { EmptyState("No molds found") }
                            items(data.molds, key = { it.id }) { mold ->
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
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (component.photoUrl != null) {
                    AsyncImage(
                        model = component.photoUrl,
                        contentDescription = "Component Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column {
                    Text(text = component.name, fontWeight = FontWeight.Bold)
                    Text(text = "SKU: ${component.sku}", style = MaterialTheme.typography.bodyMedium)
                }
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
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (customer.photoUrl != null) {
                AsyncImage(
                    model = customer.photoUrl,
                    contentDescription = "Customer Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column {
                Text(text = customer.name, fontWeight = FontWeight.Bold)
                Text(text = customer.email, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun MoldItem(mold: Mold) {
    ListCard(onClick = { /* TODO */ }) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (mold.photoUrl != null) {
                    AsyncImage(
                        model = mold.photoUrl,
                        contentDescription = "Mold Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column {
                    Text(text = mold.moldCode, fontWeight = FontWeight.Bold)
                    Text(text = "${mold.cavityCount} cavities", style = MaterialTheme.typography.bodyMedium)
                }
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
