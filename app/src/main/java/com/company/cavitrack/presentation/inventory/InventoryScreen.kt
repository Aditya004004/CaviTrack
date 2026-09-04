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
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.company.cavitrack.presentation.components.*
import com.company.cavitrack.domain.model.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onComponentClick: (String) -> Unit = {},
    onCustomerClick: (String) -> Unit = {},
    onMoldClick: (String) -> Unit = {},
    onAddNewItem: ((EntityType) -> Unit)? = null
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

    val hasActiveFilter = (selectedTabIndex == 0 && lowStockOnly) ||
            (selectedTabIndex == 2 && selectedMoldStatus != null)

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (hasActiveFilter) {
                                    Badge()
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    ComponentListContent(
                        components = components,
                        listState = componentsListState,
                        searchQuery = searchQuery,
                        lowStockOnly = lowStockOnly,
                        onComponentClick = onComponentClick,
                        onAddNewItem = onAddNewItem,
                        onClearFilters = {
                            viewModel.updateSearchQuery("")
                            viewModel.updateLowStockFilter(false)
                        }
                    )
                }
                1 -> {
                    CustomerListContent(
                        customers = customers,
                        listState = customersListState,
                        searchQuery = searchQuery,
                        onCustomerClick = onCustomerClick,
                        onAddNewItem = onAddNewItem,
                        onClearSearch = {
                            viewModel.updateSearchQuery("")
                        }
                    )
                }
                2 -> {
                    MoldListContent(
                        molds = molds,
                        listState = moldsListState,
                        searchQuery = searchQuery,
                        selectedMoldStatus = selectedMoldStatus,
                        onMoldClick = onMoldClick,
                        onAddNewItem = onAddNewItem,
                        onClearFilters = {
                            viewModel.updateSearchQuery("")
                            viewModel.updateMoldStatusFilter(null)
                        }
                    )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter Options", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (hasActiveFilter) {
                        TextButton(onClick = {
                            if (selectedTabIndex == 0) viewModel.updateLowStockFilter(false)
                            if (selectedTabIndex == 2) viewModel.updateMoldStatusFilter(null)
                        }) {
                            Text("Reset")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                when (selectedTabIndex) {
                    0 -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateLowStockFilter(!lowStockOnly) }
                                .padding(vertical = 4.dp)
                        ) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateMoldStatusFilter(status) }
                                    .padding(vertical = 4.dp)
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
private fun ComponentListContent(
    components: androidx.paging.compose.LazyPagingItems<Component>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    searchQuery: String,
    lowStockOnly: Boolean,
    onComponentClick: (String) -> Unit,
    onAddNewItem: ((EntityType) -> Unit)?,
    onClearFilters: () -> Unit
) {
    val isFiltered = searchQuery.isNotBlank() || lowStockOnly
    when {
        components.loadState.refresh is androidx.paging.LoadState.Loading -> {
            LoadingState(modifier = Modifier.padding(bottom = 88.dp))
        }
        components.loadState.refresh is androidx.paging.LoadState.Error -> {
            val error = (components.loadState.refresh as androidx.paging.LoadState.Error).error
            ErrorState(
                message = error.localizedMessage ?: "Failed to load components",
                onRetry = { components.retry() },
                modifier = Modifier.padding(bottom = 88.dp)
            )
        }
        components.loadState.refresh is androidx.paging.LoadState.NotLoading && components.itemCount == 0 -> {
            EmptyState(
                modifier = Modifier.padding(bottom = 88.dp),
                icon = if (isFiltered) Icons.Outlined.SearchOff else Icons.Outlined.Category,
                title = if (isFiltered) "No components found" else "No components yet",
                description = if (isFiltered) {
                    if (searchQuery.isNotBlank() && lowStockOnly) {
                        "No low-stock components match \"$searchQuery\"."
                    } else if (searchQuery.isNotBlank()) {
                        "No components match \"$searchQuery\". Try checking the SKU or name."
                    } else {
                        "No components are currently below the minimum stock threshold."
                    }
                } else {
                    "Your inventory is empty. Add components to track quantities, SKUs, and reorder thresholds."
                },
                actionLabel = if (isFiltered) "Clear filters" else "Add Component",
                actionIcon = if (isFiltered) Icons.Default.Clear else Icons.Default.Add,
                onActionClick = {
                    if (isFiltered) {
                        onClearFilters()
                    } else {
                        onAddNewItem?.invoke(EntityType.Component)
                    }
                }
            )
        }
        else -> {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(count = components.itemCount, key = components.itemKey { it.id }) { idx ->
                    val component = components[idx]
                    if (component != null) {
                        ComponentItem(component, onClick = { onComponentClick(component.id) })
                    }
                }
                if (components.loadState.append is androidx.paging.LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                if (components.loadState.append is androidx.paging.LoadState.Error) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { components.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerListContent(
    customers: androidx.paging.compose.LazyPagingItems<Customer>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    searchQuery: String,
    onCustomerClick: (String) -> Unit,
    onAddNewItem: ((EntityType) -> Unit)?,
    onClearSearch: () -> Unit
) {
    val isSearching = searchQuery.isNotBlank()
    when {
        customers.loadState.refresh is androidx.paging.LoadState.Loading -> {
            LoadingState(modifier = Modifier.padding(bottom = 88.dp))
        }
        customers.loadState.refresh is androidx.paging.LoadState.Error -> {
            val error = (customers.loadState.refresh as androidx.paging.LoadState.Error).error
            ErrorState(
                message = error.localizedMessage ?: "Failed to load customers",
                onRetry = { customers.retry() },
                modifier = Modifier.padding(bottom = 88.dp)
            )
        }
        customers.loadState.refresh is androidx.paging.LoadState.NotLoading && customers.itemCount == 0 -> {
            EmptyState(
                modifier = Modifier.padding(bottom = 88.dp),
                icon = if (isSearching) Icons.Outlined.PersonSearch else Icons.Outlined.People,
                title = if (isSearching) "No customers found" else "No customers yet",
                description = if (isSearching) {
                    "No customers match \"$searchQuery\". Try checking the name or phone number."
                } else {
                    "Add customer accounts to manage client relationships, orders, and assigned molds."
                },
                actionLabel = if (isSearching) "Clear search" else "Add Customer",
                actionIcon = if (isSearching) Icons.Default.Clear else Icons.Default.Add,
                onActionClick = {
                    if (isSearching) {
                        onClearSearch()
                    } else {
                        onAddNewItem?.invoke(EntityType.Customer)
                    }
                }
            )
        }
        else -> {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(count = customers.itemCount, key = customers.itemKey { it.id }) { idx ->
                    val customer = customers[idx]
                    if (customer != null) {
                        CustomerItem(customer, onClick = { onCustomerClick(customer.id) })
                    }
                }
                if (customers.loadState.append is androidx.paging.LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                if (customers.loadState.append is androidx.paging.LoadState.Error) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { customers.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoldListContent(
    molds: androidx.paging.compose.LazyPagingItems<Mold>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    searchQuery: String,
    selectedMoldStatus: MoldStatus?,
    onMoldClick: (String) -> Unit,
    onAddNewItem: ((EntityType) -> Unit)?,
    onClearFilters: () -> Unit
) {
    val isFiltered = searchQuery.isNotBlank() || selectedMoldStatus != null
    when {
        molds.loadState.refresh is androidx.paging.LoadState.Loading -> {
            LoadingState(modifier = Modifier.padding(bottom = 88.dp))
        }
        molds.loadState.refresh is androidx.paging.LoadState.Error -> {
            val error = (molds.loadState.refresh as androidx.paging.LoadState.Error).error
            ErrorState(
                message = error.localizedMessage ?: "Failed to load molds",
                onRetry = { molds.retry() },
                modifier = Modifier.padding(bottom = 88.dp)
            )
        }
        molds.loadState.refresh is androidx.paging.LoadState.NotLoading && molds.itemCount == 0 -> {
            EmptyState(
                modifier = Modifier.padding(bottom = 88.dp),
                icon = if (isFiltered) Icons.Outlined.SearchOff else Icons.Outlined.PrecisionManufacturing,
                title = if (isFiltered) "No molds found" else "No molds yet",
                description = if (isFiltered) {
                    if (searchQuery.isNotBlank() && selectedMoldStatus != null) {
                        "No molds match \"$searchQuery\" with status \"${selectedMoldStatus.name}\"."
                    } else if (searchQuery.isNotBlank()) {
                        "No molds match \"$searchQuery\". Try checking the mold code or name."
                    } else {
                        "No molds found with status \"${selectedMoldStatus?.name}\"."
                    }
                } else {
                    "Register production molds to track cavity configurations, tooling status, and maintenance."
                },
                actionLabel = if (isFiltered) "Clear filters" else "Add Mold",
                actionIcon = if (isFiltered) Icons.Default.Clear else Icons.Default.Add,
                onActionClick = {
                    if (isFiltered) {
                        onClearFilters()
                    } else {
                        onAddNewItem?.invoke(EntityType.Mold)
                    }
                }
            )
        }
        else -> {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(count = molds.itemCount, key = molds.itemKey { it.id }) { idx ->
                    val mold = molds[idx]
                    if (mold != null) {
                        MoldItem(mold, onClick = { onMoldClick(mold.id) })
                    }
                }
                if (molds.loadState.append is androidx.paging.LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                if (molds.loadState.append is androidx.paging.LoadState.Error) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { molds.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
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
                    MoldStatus.Unknown -> StatusType.NEUTRAL
                }
            )
        }
    }
}


