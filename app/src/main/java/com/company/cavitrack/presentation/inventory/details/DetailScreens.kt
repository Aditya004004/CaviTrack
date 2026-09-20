package com.company.cavitrack.presentation.inventory.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.company.cavitrack.R
import com.company.cavitrack.domain.model.MoldStatus
import com.company.cavitrack.presentation.components.ErrorState
import com.company.cavitrack.presentation.components.UiState
import java.text.NumberFormat

@Composable
fun ComponentDetailScreen(
    viewModel: ComponentDetailViewModel = hiltViewModel(),
    onNavigateToUpdate: (String) -> Unit = {},
    onNavigateToPhotoUpdate: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.loadComponent(isRefresh = true)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.title_delete_item, "Component"), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.msg_confirm_delete_item, "component")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteComponent(onSuccess = onBack)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_delete), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AmbientDetailBackground(isDark)

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DetailTopBar(
                title = stringResource(R.string.title_component_details),
                onBack = onBack,
                onDelete = if (uiState is UiState.Success) ({ showDeleteDialog = true }) else null
            )

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorState(message = state.message, onRetry = { viewModel.retry() }, onBack = onBack)
                    }
                }
                is UiState.Success -> {
                    val component = state.data
                    val isLowStock = component.qty <= component.minStockThreshold

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        // Hero Image or Placeholder
                        HeroImageOrPlaceholder(
                            photoUrl = component.photoUrl,
                            placeholderIcon = Icons.Outlined.Inventory2,
                            placeholderTint = MaterialTheme.colorScheme.primary,
                            placeholderBg = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                            contentDescription = component.name
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title & SKU Header
                        Text(
                            text = component.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "SKU: ${component.sku}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Details Card
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                DetailRow(
                                    label = stringResource(R.string.label_category),
                                    value = component.category,
                                    icon = Icons.Outlined.Category
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))

                                DetailRow(
                                    label = stringResource(R.string.label_quantity),
                                    value = "${NumberFormat.getIntegerInstance().format(component.qty)} ${component.unit}",
                                    icon = Icons.Outlined.Numbers,
                                    trailingBadge = {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isLowStock) (if (isDark) Color(0xFF3B1818) else Color(0xFFFEF2F2)) else (if (isDark) Color(0xFF0F2E1E) else Color(0xFFDCFCE7))
                                        ) {
                                            Text(
                                                text = if (isLowStock) stringResource(R.string.inventory_low_stock_status) else stringResource(R.string.inventory_in_stock),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLowStock) Color(0xFFDC2626) else Color(0xFF16A34A),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))

                                DetailRow(
                                    label = stringResource(R.string.label_min_stock),
                                    value = "${NumberFormat.getIntegerInstance().format(component.minStockThreshold)} ${component.unit}",
                                    icon = Icons.Outlined.WarningAmber
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToPhotoUpdate(component.id) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.label_update_photo))
                            }
                            Button(
                                onClick = { onNavigateToUpdate(component.id) },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.label_edit_component))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerDetailScreen(
    viewModel: CustomerDetailViewModel = hiltViewModel(),
    onNavigateToUpdate: (String) -> Unit = {},
    onNavigateToPhotoUpdate: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.loadCustomer(isRefresh = true)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.title_delete_item, "Customer"), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.msg_confirm_delete_item, "customer")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCustomer(onSuccess = onBack)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_delete), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AmbientDetailBackground(isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            DetailTopBar(
                title = stringResource(R.string.title_customer_details),
                onBack = onBack,
                onDelete = if (uiState is UiState.Success) ({ showDeleteDialog = true }) else null
            )

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorState(message = state.message, onRetry = { viewModel.retry() }, onBack = onBack)
                    }
                }
                is UiState.Success -> {
                    val customer = state.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        // Hero Image or Placeholder
                        HeroImageOrPlaceholder(
                            photoUrl = customer.photoUrl,
                            placeholderIcon = Icons.Outlined.People,
                            placeholderTint = Color(0xFF16A34A),
                            placeholderBg = if (isDark) Color(0xFF0E281E) else Color(0xFFDCFCE7),
                            contentDescription = customer.name
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Customer Name
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Details Card
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                DetailRow(
                                    label = stringResource(R.string.label_phone),
                                    value = customer.phone.ifEmpty { "N/A" },
                                    icon = Icons.Outlined.Phone
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))

                                DetailRow(
                                    label = stringResource(R.string.label_email),
                                    value = customer.email.ifEmpty { "N/A" },
                                    icon = Icons.Outlined.Email
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))

                                DetailRow(
                                    label = stringResource(R.string.label_address),
                                    value = customer.address.ifEmpty { "N/A" },
                                    icon = Icons.Outlined.Place
                                )

                                if (customer.notes.isNotEmpty()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))
                                    DetailRow(
                                        label = stringResource(R.string.label_notes_prefix),
                                        value = customer.notes,
                                        icon = Icons.AutoMirrored.Outlined.Notes
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Button(
                            onClick = { onNavigateToUpdate(customer.id) },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.label_edit_customer), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { onNavigateToPhotoUpdate(customer.id) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.label_update_photo))
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MoldDetailScreen(
    viewModel: MoldDetailViewModel = hiltViewModel(),
    onNavigateToUpdate: (String) -> Unit = {},
    onNavigateToPhotoUpdate: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.loadMold(isRefresh = true)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.title_delete_item, "Mold"), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.msg_confirm_delete_item, "mold")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteMold(onSuccess = onBack)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_delete), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AmbientDetailBackground(isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            DetailTopBar(
                title = stringResource(R.string.title_mold_details),
                onBack = onBack,
                onDelete = if (uiState is UiState.Success) ({ showDeleteDialog = true }) else null
            )

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorState(message = state.message, onRetry = { viewModel.retry() }, onBack = onBack)
                    }
                }
                is UiState.Success -> {
                    val mold = state.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        // Hero Image or Placeholder
                        HeroImageOrPlaceholder(
                            photoUrl = mold.photoUrl,
                            placeholderIcon = Icons.Outlined.PrecisionManufacturing,
                            placeholderTint = Color(0xFF9333EA),
                            placeholderBg = if (isDark) Color(0xFF261838) else Color(0xFFF3E8FF),
                            contentDescription = mold.moldCode
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mold Code
                        Text(
                            text = mold.moldCode,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Details Card
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                DetailRow(
                                    label = stringResource(R.string.label_cavity_count),
                                    value = "${mold.cavityCount} cavities",
                                    icon = Icons.Outlined.Numbers
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))

                                DetailRow(
                                    label = stringResource(R.string.label_status_prefix),
                                    value = mold.status.name,
                                    icon = Icons.Outlined.Info,
                                    trailingBadge = {
                                        val (statusBg, statusTint) = when (mold.status) {
                                            MoldStatus.Active -> if (isDark) Pair(Color(0xFF0F2E1E), Color(0xFF34D399)) else Pair(Color(0xFFDCFCE7), Color(0xFF16A34A))
                                            MoldStatus.InMaintenance -> if (isDark) Pair(Color(0xFF38230D), Color(0xFFFBBF24)) else Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                                            MoldStatus.Retired, MoldStatus.Unknown -> if (isDark) Pair(Color(0xFF3B1818), Color(0xFFF87171)) else Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = statusBg
                                        ) {
                                            Text(
                                                text = mold.status.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = statusTint,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 16.dp))

                                DetailRow(
                                    label = stringResource(R.string.label_location),
                                    value = mold.location.ifEmpty { "N/A" },
                                    icon = Icons.Outlined.Place
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Button(
                            onClick = { onNavigateToUpdate(mold.id) },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.label_edit_mold), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { onNavigateToPhotoUpdate(mold.id) },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.label_update_photo))
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(42.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (onDelete != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                modifier = Modifier.size(42.dp)
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.label_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AmbientDetailBackground(isDark: Boolean) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF818CF8).copy(alpha = if (isDark) 0.08f else 0.16f),
                    Color(0xFFC7D2FE).copy(alpha = if (isDark) 0.03f else 0.06f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.90f, size.height * 0.15f),
                radius = size.width * 0.50f
            )
        )
    }
}

@Composable
private fun HeroImageOrPlaceholder(
    photoUrl: String?,
    placeholderIcon: ImageVector,
    placeholderTint: Color,
    placeholderBg: Color,
    contentDescription: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (!photoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(placeholderBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = placeholderIcon,
                    contentDescription = null,
                    tint = placeholderTint,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector? = null,
    trailingBadge: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (trailingBadge != null) {
            trailingBadge()
        }
    }
}

