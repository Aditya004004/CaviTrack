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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.res.stringResource
import com.company.cavitrack.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onComponentClick: (String) -> Unit = {},
    onCustomerClick: (String) -> Unit = {},
    onMoldClick: (String) -> Unit = {},
    onAddNewItem: ((EntityType) -> Unit)? = null
) {
    val pagerState = rememberPagerState { 3 }
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf(
        stringResource(R.string.title_components),
        stringResource(R.string.title_customers),
        stringResource(R.string.title_molds)
    )

    val componentsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val customersListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val moldsListState = androidx.compose.foundation.lazy.rememberLazyListState()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val lowStockOnly by viewModel.lowStockOnly.collectAsStateWithLifecycle()
    val selectedMoldStatus by viewModel.selectedMoldStatus.collectAsStateWithLifecycle()

    val hasActiveFilter = (pagerState.currentPage == 0 && lowStockOnly) ||
            (pagerState.currentPage == 2 && selectedMoldStatus != null)

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text(stringResource(R.string.placeholder_search_inventory)) },
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
        
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> {
                    val components = viewModel.componentsFlow.collectAsLazyPagingItems()
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
                    val customers = viewModel.customersFlow.collectAsLazyPagingItems()
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
                    val molds = viewModel.moldsFlow.collectAsLazyPagingItems()
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
                    Text(stringResource(R.string.title_filter_options), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (hasActiveFilter) {
                        TextButton(onClick = {
                            if (pagerState.currentPage == 0) viewModel.updateLowStockFilter(false)
                            if (pagerState.currentPage == 2) viewModel.updateMoldStatusFilter(null)
                        }) {
                            Text(stringResource(R.string.btn_reset))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                when (pagerState.currentPage) {
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
                            Text(stringResource(R.string.filter_low_stock_only))
                        }
                    }
                    2 -> {
                        Text(stringResource(R.string.label_mold_status), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        val statuses = listOf(null, MoldStatus.Active, MoldStatus.InMaintenance, MoldStatus.Retired)
                        val labels = listOf(
                            stringResource(R.string.mold_status_all),
                            stringResource(R.string.mold_status_active),
                            stringResource(R.string.mold_status_maintenance),
                            stringResource(R.string.mold_status_retired)
                        )
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
                        Text(stringResource(R.string.msg_no_customer_filters), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { showFilterSheet = false }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.btn_apply_filters))
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
            SkeletonList(modifier = Modifier.padding(bottom = 88.dp))
        }
        components.loadState.refresh is androidx.paging.LoadState.Error -> {
            val error = (components.loadState.refresh as androidx.paging.LoadState.Error).error
            val errorMessage = when (error) {
                is java.io.IOException -> stringResource(R.string.error_network)
                else -> stringResource(R.string.error_load_components)
            }
            ErrorState(
                message = errorMessage,
                onRetry = { components.retry() },
                modifier = Modifier.padding(bottom = 88.dp)
            )
        }
        components.loadState.refresh is androidx.paging.LoadState.NotLoading && components.itemCount == 0 -> {
            EmptyState(
                modifier = Modifier.padding(bottom = 88.dp),
                icon = if (isFiltered) Icons.Outlined.SearchOff else Icons.Outlined.Category,
                title = stringResource(
                    if (isFiltered) R.string.empty_components_filtered_title else R.string.empty_components_title
                ),
                description = if (isFiltered) {
                    if (searchQuery.isNotBlank() && lowStockOnly) {
                        stringResource(R.string.empty_components_search_low_stock_desc, searchQuery)
                    } else if (searchQuery.isNotBlank()) {
                        stringResource(R.string.empty_components_search_desc, searchQuery)
                    } else {
                        stringResource(R.string.empty_components_low_stock_desc)
                    }
                } else {
                    stringResource(R.string.empty_components_desc)
                },
                actionLabel = stringResource(
                    if (isFiltered) R.string.action_clear_filters else R.string.action_add_component
                ),
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
                                Text(stringResource(R.string.btn_retry))
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
            SkeletonList(modifier = Modifier.padding(bottom = 88.dp))
        }
        customers.loadState.refresh is androidx.paging.LoadState.Error -> {
            val error = (customers.loadState.refresh as androidx.paging.LoadState.Error).error
            val errorMessage = when (error) {
                is java.io.IOException -> stringResource(R.string.error_network)
                else -> stringResource(R.string.error_load_customers)
            }
            ErrorState(
                message = errorMessage,
                onRetry = { customers.retry() },
                modifier = Modifier.padding(bottom = 88.dp)
            )
        }
        customers.loadState.refresh is androidx.paging.LoadState.NotLoading && customers.itemCount == 0 -> {
            EmptyState(
                modifier = Modifier.padding(bottom = 88.dp),
                icon = if (isSearching) Icons.Outlined.PersonSearch else Icons.Outlined.People,
                title = stringResource(
                    if (isSearching) R.string.empty_customers_filtered_title else R.string.empty_customers_title
                ),
                description = if (isSearching) {
                    stringResource(R.string.empty_customers_search_desc, searchQuery)
                } else {
                    stringResource(R.string.empty_customers_desc)
                },
                actionLabel = stringResource(
                    if (isSearching) R.string.action_clear_search else R.string.action_add_customer
                ),
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
                                Text(stringResource(R.string.btn_retry))
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
            SkeletonList(modifier = Modifier.padding(bottom = 88.dp))
        }
        molds.loadState.refresh is androidx.paging.LoadState.Error -> {
            val error = (molds.loadState.refresh as androidx.paging.LoadState.Error).error
            val errorMessage = when (error) {
                is java.io.IOException -> stringResource(R.string.error_network)
                else -> stringResource(R.string.error_load_molds)
            }
            ErrorState(
                message = errorMessage,
                onRetry = { molds.retry() },
                modifier = Modifier.padding(bottom = 88.dp)
            )
        }
        molds.loadState.refresh is androidx.paging.LoadState.NotLoading && molds.itemCount == 0 -> {
            EmptyState(
                modifier = Modifier.padding(bottom = 88.dp),
                icon = if (isFiltered) Icons.Outlined.SearchOff else Icons.Outlined.PrecisionManufacturing,
                title = stringResource(
                    if (isFiltered) R.string.empty_molds_filtered_title else R.string.empty_molds_title
                ),
                description = if (isFiltered) {
                    if (searchQuery.isNotBlank() && selectedMoldStatus != null) {
                        stringResource(R.string.empty_molds_search_status_desc, selectedMoldStatus.name, searchQuery)
                    } else if (searchQuery.isNotBlank()) {
                        stringResource(R.string.empty_molds_search_desc, searchQuery)
                    } else {
                        stringResource(R.string.empty_molds_status_desc)
                    }
                } else {
                    stringResource(R.string.empty_molds_desc)
                },
                actionLabel = stringResource(
                    if (isFiltered) R.string.action_clear_filters else R.string.action_add_mold
                ),
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
                                Text(stringResource(R.string.btn_retry))
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
            val isLowStock = component.qty <= component.minStockThreshold
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


