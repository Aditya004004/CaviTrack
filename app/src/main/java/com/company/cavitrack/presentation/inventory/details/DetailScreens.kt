package com.company.cavitrack.presentation.inventory.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.company.cavitrack.R
import com.company.cavitrack.presentation.components.ErrorState
import com.company.cavitrack.presentation.components.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentDetailScreen(
    viewModel: ComponentDetailViewModel = hiltViewModel(),
    onNavigateToUpdate: (String) -> Unit = {},
    onNavigateToPhotoUpdate: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_component_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
            }
            is UiState.Success -> {
                val component = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    if (!component.photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(component.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Component Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text("${stringResource(R.string.label_name)}: ${component.name}", style = MaterialTheme.typography.titleMedium)
                    Text("${stringResource(R.string.label_sku)}: ${component.sku}", style = MaterialTheme.typography.bodyLarge)
                    Text("${stringResource(R.string.label_category)}: ${component.category}", style = MaterialTheme.typography.bodyLarge)
                    Text("${stringResource(R.string.label_quantity)}: ${component.qty} ${component.unit}", style = MaterialTheme.typography.bodyLarge)
                    Text("Min Stock: ${component.minStockThreshold}", style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToPhotoUpdate(component.id) },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Text(stringResource(R.string.label_update_photo))
                        }
                        Button(
                            onClick = { onNavigateToUpdate(component.id) },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Text(stringResource(R.string.label_adjust_stock))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    viewModel: CustomerDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_customer_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
            }
            is UiState.Success -> {
                val customer = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    if (!customer.photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(customer.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Customer Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text("${stringResource(R.string.label_name)}: ${customer.name}", style = MaterialTheme.typography.titleMedium)
                    Text("${stringResource(R.string.label_phone)}: ${customer.phone}", style = MaterialTheme.typography.bodyLarge)
                    Text("${stringResource(R.string.label_email)}: ${customer.email}", style = MaterialTheme.typography.bodyLarge)
                    Text("${stringResource(R.string.label_address)}: ${customer.address}", style = MaterialTheme.typography.bodyLarge)
                    Text("Notes: ${customer.notes}", style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.msg_v1_limitation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoldDetailScreen(
    viewModel: MoldDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_mold_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
            }
            is UiState.Success -> {
                val mold = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    if (!mold.photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(mold.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Mold Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text("${stringResource(R.string.label_mold_code)}: ${mold.moldCode}", style = MaterialTheme.typography.titleMedium)
                    Text("${stringResource(R.string.label_cavity_count)}: ${mold.cavityCount}", style = MaterialTheme.typography.bodyLarge)
                    Text("Status: ${mold.status.name}", style = MaterialTheme.typography.bodyLarge)
                    Text("${stringResource(R.string.label_location)}: ${mold.location}", style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.msg_v1_limitation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
