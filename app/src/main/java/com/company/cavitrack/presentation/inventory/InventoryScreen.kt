package com.company.cavitrack.presentation.inventory

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.company.cavitrack.R
import com.company.cavitrack.domain.model.Component
import com.company.cavitrack.domain.model.Customer
import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.domain.model.Mold
import com.company.cavitrack.domain.model.MoldStatus
import com.company.cavitrack.presentation.components.EmptyState
import com.company.cavitrack.presentation.components.ErrorState
import com.company.cavitrack.presentation.components.SkeletonList
import com.company.cavitrack.presentation.components.StatusBadge
import com.company.cavitrack.presentation.components.StatusType
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onComponentClick: (String) -> Unit = {},
    onCustomerClick: (String) -> Unit = {},
    onMoldClick: (String) -> Unit = {},
    onAddNewItem: ((EntityType) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val isDark = isSystemInDarkTheme()

    val componentsListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val customersListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val moldsListState = androidx.compose.foundation.lazy.rememberLazyListState()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val lowStockOnly by viewModel.lowStockOnly.collectAsStateWithLifecycle()
    val selectedMoldStatus by viewModel.selectedMoldStatus.collectAsStateWithLifecycle()

    val hasActiveFilter = (pagerState.currentPage == 0 && lowStockOnly) ||
            (pagerState.currentPage == 2 && selectedMoldStatus != null)

    val components = viewModel.componentsFlow.collectAsLazyPagingItems()
    val customers = viewModel.customersFlow.collectAsLazyPagingItems()
    val molds = viewModel.moldsFlow.collectAsLazyPagingItems()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Atmospheric radial gradient in top-right corner
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF818CF8).copy(alpha = if (isDark) 0.08f else 0.16f),
                        Color(0xFFC7D2FE).copy(alpha = if (isDark) 0.03f else 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.90f, size.height * 0.15f),
                    radius = size.width * 0.48f
                )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.inventory_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.inventory_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Refresh Button
                IconButton(
                    onClick = {
                        when (pagerState.currentPage) {
                            0 -> components.refresh()
                            1 -> customers.refresh()
                            2 -> molds.refresh()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Search Bar with Integrated Filter Trigger
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                shadowElevation = 0.5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.placeholder_search_inventory),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateSearchQuery("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (hasActiveFilter) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterAlt,
                                contentDescription = "Filter",
                                tint = if (hasActiveFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Segmented Pill Tab Bar
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabData = listOf(
                        Triple(stringResource(R.string.title_components), Icons.Outlined.Inventory2, 0),
                        Triple(stringResource(R.string.title_customers), Icons.Outlined.People, 1),
                        Triple(stringResource(R.string.title_molds), Icons.Outlined.Settings, 2)
                    )

                    tabData.forEach { (title, icon, index) ->
                        val isSelected = pagerState.currentPage == index
                        val containerColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            label = "tabContainerColor"
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                            label = "tabContentColor"
                        )

                        Surface(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = containerColor,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }
            }

            // Section Subheader: Item Count
            val currentCount = when (pagerState.currentPage) {
                0 -> components.itemCount
                1 -> customers.itemCount
                else -> molds.itemCount
            }
            val currentEntityLabel = when (pagerState.currentPage) {
                0 -> stringResource(R.string.title_components)
                1 -> stringResource(R.string.title_customers)
                else -> stringResource(R.string.title_molds)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.inventory_items_count, currentCount, currentEntityLabel),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Horizontal Pager with Items
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
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
                            onClearFilters = { viewModel.updateSearchQuery("") }
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
    }

    // Filter Options Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.title_filter_options),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
                        Text(
                            stringResource(R.string.label_mold_status),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
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
                                RadioButton(
                                    selected = selectedMoldStatus == status,
                                    onClick = { viewModel.updateMoldStatusFilter(status) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(labels[index])
                            }
                        }
                    }
                    else -> {
                        Text(
                            stringResource(R.string.msg_no_customer_filters),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showFilterSheet = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_apply_filters))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ComponentListContent(
    components: LazyPagingItems<Component>,
    listState: LazyListState,
    searchQuery: String,
    lowStockOnly: Boolean,
    onComponentClick: (String) -> Unit,
    onAddNewItem: ((EntityType) -> Unit)?,
    onClearFilters: () -> Unit
) {
    val isFiltered = searchQuery.isNotBlank() || lowStockOnly
    when {
        components.loadState.refresh is LoadState.Loading -> {
            SkeletonList(modifier = Modifier.padding(bottom = 88.dp))
        }
        components.loadState.refresh is LoadState.Error -> {
            val error = (components.loadState.refresh as LoadState.Error).error
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
        components.loadState.refresh is LoadState.NotLoading && components.itemCount == 0 -> {
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
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(count = components.itemCount, key = components.itemKey { it.id }) { idx ->
                    val component = components[idx]
                    if (component != null) {
                        ComponentItem(component, onClick = { onComponentClick(component.id) })
                    }
                }
                if (components.loadState.append is LoadState.Loading) {
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
                if (components.loadState.append is LoadState.Error) {
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
    customers: LazyPagingItems<Customer>,
    listState: LazyListState,
    searchQuery: String,
    onCustomerClick: (String) -> Unit,
    onAddNewItem: ((EntityType) -> Unit)?,
    onClearFilters: () -> Unit
) {
    val isFiltered = searchQuery.isNotBlank()
    when {
        customers.loadState.refresh is LoadState.Loading -> {
            SkeletonList(modifier = Modifier.padding(bottom = 88.dp))
        }
        customers.loadState.refresh is LoadState.Error -> {
            val error = (customers.loadState.refresh as LoadState.Error).error
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
        customers.loadState.refresh is LoadState.NotLoading && customers.itemCount == 0 -> {
            EmptyState(
                modifier = Modifier.padding(bottom = 88.dp),
                icon = if (isFiltered) Icons.Outlined.SearchOff else Icons.Outlined.People,
                title = stringResource(
                    if (isFiltered) R.string.empty_customers_filtered_title else R.string.empty_customers_title
                ),
                description = if (isFiltered) {
                    stringResource(R.string.empty_customers_search_desc, searchQuery)
                } else {
                    stringResource(R.string.empty_customers_desc)
                },
                actionLabel = stringResource(
                    if (isFiltered) R.string.action_clear_search else R.string.action_add_customer
                ),
                actionIcon = if (isFiltered) Icons.Default.Clear else Icons.Default.Add,
                onActionClick = {
                    if (isFiltered) {
                        onClearFilters()
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
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(count = customers.itemCount, key = customers.itemKey { it.id }) { idx ->
                    val customer = customers[idx]
                    if (customer != null) {
                        CustomerItem(customer, onClick = { onCustomerClick(customer.id) })
                    }
                }
                if (customers.loadState.append is LoadState.Loading) {
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
                if (customers.loadState.append is LoadState.Error) {
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
    molds: LazyPagingItems<Mold>,
    listState: LazyListState,
    searchQuery: String,
    selectedMoldStatus: MoldStatus?,
    onMoldClick: (String) -> Unit,
    onAddNewItem: ((EntityType) -> Unit)?,
    onClearFilters: () -> Unit
) {
    val isFiltered = searchQuery.isNotBlank() || selectedMoldStatus != null
    when {
        molds.loadState.refresh is LoadState.Loading -> {
            SkeletonList(modifier = Modifier.padding(bottom = 88.dp))
        }
        molds.loadState.refresh is LoadState.Error -> {
            val error = (molds.loadState.refresh as LoadState.Error).error
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
        molds.loadState.refresh is LoadState.NotLoading && molds.itemCount == 0 -> {
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
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(count = molds.itemCount, key = molds.itemKey { it.id }) { idx ->
                    val mold = molds[idx]
                    if (mold != null) {
                        MoldItem(mold, onClick = { onMoldClick(mold.id) })
                    }
                }
                if (molds.loadState.append is LoadState.Loading) {
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
                if (molds.loadState.append is LoadState.Error) {
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
    val isDark = isSystemInDarkTheme()
    val isLowStock = component.qty <= component.minStockThreshold
    val formattedQty = remember(component.qty) {
        NumberFormat.getNumberInstance(Locale.US).format(component.qty)
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Item Icon / Image Badge
            if (!component.photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = component.photoUrl,
                    contentDescription = "Component Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF132035) else Color(0xFFEFF6FF),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center Content: Name, SKU, Tag Badge
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "SKU: ${component.sku}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = component.category.ifBlank { stringResource(R.string.inventory_tag_component) },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Column: Quantity Badge & Navigation Chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLowStock) {
                        if (isDark) Color(0xFF331818) else Color(0xFFFEF2F2)
                    } else {
                        if (isDark) Color(0xFF0E281E) else Color(0xFFF0FDF4)
                    },
                    modifier = Modifier.wrapContentSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$formattedQty ${component.unit.ifBlank { "PCS" }}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) {
                                if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                            } else {
                                if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
                            }
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = if (isLowStock) {
                                stringResource(R.string.inventory_low_stock_status)
                            } else {
                                stringResource(R.string.inventory_in_stock)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLowStock) {
                                if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun CustomerItem(customer: Customer, onClick: () -> Unit = {}) {
    val isDark = isSystemInDarkTheme()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!customer.photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = customer.photoUrl,
                    contentDescription = "Customer Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0E281E) else Color(0xFFDCFCE7),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = customer.phone.ifBlank { customer.email },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.inventory_tag_customer),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun MoldItem(mold: Mold, onClick: () -> Unit = {}) {
    val isDark = isSystemInDarkTheme()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!mold.photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = mold.photoUrl,
                    contentDescription = "Mold Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF261838) else Color(0xFFF3E8FF),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.PrecisionManufacturing,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFFC084FC) else Color(0xFF9333EA),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mold.moldCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${mold.cavityCount} cavities" + if (mold.location.isNotBlank()) " • ${mold.location}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.inventory_tag_mold),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    text = mold.status.name,
                    statusType = when (mold.status) {
                        MoldStatus.Active -> StatusType.SUCCESS
                        MoldStatus.InMaintenance -> StatusType.WARNING
                        MoldStatus.Retired -> StatusType.NEUTRAL
                        MoldStatus.Unknown -> StatusType.NEUTRAL
                    }
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
