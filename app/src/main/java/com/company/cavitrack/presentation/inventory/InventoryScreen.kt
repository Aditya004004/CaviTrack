package com.company.cavitrack.presentation.inventory



import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.company.cavitrack.presentation.components.*
import com.company.cavitrack.domain.model.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onComponentClick: (String) -> Unit = {},
    onCustomerClick: (String) -> Unit = {},
    onMoldClick: (String) -> Unit = {}
) {
    val components = viewModel.componentsFlow.collectAsLazyPagingItems()
    val customers = viewModel.customersFlow.collectAsLazyPagingItems()
    val molds = viewModel.moldsFlow.collectAsLazyPagingItems()
    
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Components", "Customers", "Molds")

    val componentsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val customersListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val moldsListState = androidx.compose.foundation.lazy.rememberLazyListState()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val lowStockOnly by viewModel.lowStockOnly.collectAsStateWithLifecycle()
    val selectedMoldStatus by viewModel.selectedMoldStatus.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search inventory...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                IconButton(onClick = { showFilterSheet = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            },
            singleLine = true
        )
        
        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        val listState = when (selectedTabIndex) {
            0 -> componentsListState
            1 -> customersListState
            else -> moldsListState
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp) // Space for FAB
        ) {
            when (selectedTabIndex) {
                0 -> {
                    if (components.loadState.refresh is androidx.paging.LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    items(components.itemCount) { idx ->
                        val component = components[idx]
                        if (component != null) {
                            ComponentItem(component, onClick = { onComponentClick(component.id) })
                        }
                    }
                    if (components.loadState.append is androidx.paging.LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                1 -> {
                    if (customers.loadState.refresh is androidx.paging.LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    items(customers.itemCount) { idx ->
                        val customer = customers[idx]
                        if (customer != null) {
                            CustomerItem(customer, onClick = { onCustomerClick(customer.id) })
                        }
                    }
                    if (customers.loadState.append is androidx.paging.LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                2 -> {
                    if (molds.loadState.refresh is androidx.paging.LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    items(molds.itemCount) { idx ->
                        val mold = molds[idx]
                        if (mold != null) {
                            MoldItem(mold, onClick = { onMoldClick(mold.id) })
                        }
                    }
                    if (molds.loadState.append is androidx.paging.LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showFilterSheet) {
        androidx.activity.compose.BackHandler { showFilterSheet = false }
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text("Filter Options", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                when (selectedTabIndex) {
                    0 -> {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.updateLowStockFilter(!lowStockOnly) }) {
                            Checkbox(checked = lowStockOnly, onCheckedChange = { viewModel.updateLowStockFilter(it) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Low Stock Only")
                        }
                    }
                    2 -> {
                        Text("Mold Status", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        val statuses = listOf(null, MoldStatus.Active, MoldStatus.InMaintenance, MoldStatus.Retired)
                        val labels = listOf("All", "Active", "In Maintenance", "Retired")
                        statuses.forEachIndexed { index, status ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.updateMoldStatusFilter(status) }.padding(vertical = 4.dp)
                            ) {
                                RadioButton(selected = selectedMoldStatus == status, onClick = { viewModel.updateMoldStatusFilter(status) })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(labels[index])
                            }
                        }
                    }
                    else -> {
                        Text("No filters available for Customers.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { showFilterSheet = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Apply Filters")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}



@Composable
fun ComponentItem(component: Component, onClick: () -> Unit = {}) {
    ListCard(onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!component.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = component.photoUrl,
                        contentDescription = "Component Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                
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
fun CustomerItem(customer: Customer, onClick: () -> Unit = {}) {
    ListCard(onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (!customer.photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = customer.photoUrl,
                    contentDescription = "Customer Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            } else {
                Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(text = customer.name, fontWeight = FontWeight.Bold)
                Text(text = customer.email, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun MoldItem(mold: Mold, onClick: () -> Unit = {}) {
    ListCard(onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!mold.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = mold.photoUrl,
                        contentDescription = "Mold Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(text = mold.moldCode, fontWeight = FontWeight.Bold)
                    Text(text = "${mold.cavityCount} cavities", style = MaterialTheme.typography.bodyMedium)
                }
            }
            StatusBadge(
                text = mold.status.name,
                statusType = when(mold.status) {
                    MoldStatus.Active -> StatusType.SUCCESS
                    MoldStatus.InMaintenance -> StatusType.WARNING
                    MoldStatus.Retired -> StatusType.NEUTRAL
                    else -> StatusType.NEUTRAL
                }
            )
        }
    }
}


