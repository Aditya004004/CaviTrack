package com.company.cavitrack.presentation.addupdate.manual

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.cavitrack.R
import com.company.cavitrack.presentation.theme.*
import com.company.cavitrack.domain.model.EntityType
import com.company.cavitrack.domain.model.MoldStatus

@Composable
fun ManualUpdateScreen(
    entityType: EntityType,
    entityId: String?,
    viewModel: ManualUpdateViewModel = hiltViewModel(),
    onUpdateComplete: () -> Unit = {}
) {
    var quantity by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var hasError by rememberSaveable { mutableStateOf(false) }

    var name by rememberSaveable { mutableStateOf("") }
    var sku by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var moldCode by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var moldStatus by rememberSaveable { mutableStateOf(MoldStatus.Active) }

    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentQty by viewModel.currentQty.collectAsStateWithLifecycle()
    val loadedComponent by viewModel.loadedComponent.collectAsStateWithLifecycle()
    val loadedCustomer by viewModel.loadedCustomer.collectAsStateWithLifecycle()
    val loadedMold by viewModel.loadedMold.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(entityId, entityType) {
        if (entityId != null) {
            when (entityType) {
                EntityType.Component -> viewModel.loadComponent(entityId)
                EntityType.Customer -> viewModel.loadCustomer(entityId)
                EntityType.Mold -> viewModel.loadMold(entityId)
                else -> {}
            }
        }
    }

    LaunchedEffect(currentQty) {
        if (currentQty != null && quantity.isEmpty()) {
            quantity = currentQty.toString()
        }
    }

    LaunchedEffect(loadedComponent) {
        loadedComponent?.let {
            if (name.isEmpty()) name = it.name
            if (sku.isEmpty()) sku = it.sku
            if (category.isEmpty()) category = it.category
            if (quantity.isEmpty()) quantity = it.qty.toString()
        }
    }

    LaunchedEffect(loadedCustomer) {
        loadedCustomer?.let {
            if (name.isEmpty()) name = it.name
            if (phone.isEmpty()) phone = it.phone
            if (email.isEmpty()) email = it.email
            if (address.isEmpty()) address = it.address
            if (note.isEmpty() && it.notes.isNotEmpty()) note = it.notes
        }
    }

    LaunchedEffect(loadedMold) {
        loadedMold?.let {
            if (moldCode.isEmpty()) moldCode = it.moldCode
            if (location.isEmpty()) location = it.location
            if (quantity.isEmpty()) quantity = it.cavityCount.toString()
            moldStatus = it.status
        }
    }

    LaunchedEffect(Unit) {
        viewModel.isSaved.collect {
            onUpdateComplete()
        }
    }

    val screenTitle = if (entityId != null) {
        stringResource(R.string.title_edit_item, entityType.name)
    } else {
        stringResource(R.string.title_new_item, entityType.name)
    }

    val inputColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = if (isDark) SlateDark else SlateLight,
        focusedContainerColor = if (isDark) SlateDark else SlateLight,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
        focusedBorderColor = MaterialTheme.colorScheme.primary
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Atmospheric radial gradient
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AmbientGlowPrimary.copy(alpha = if (isDark) 0.08f else 0.16f),
                        AmbientGlowSecondary.copy(alpha = if (isDark) 0.03f else 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.90f, size.height * 0.15f),
                    radius = size.width * 0.50f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header with circular back button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(42.dp)
                ) {
                    IconButton(onClick = onUpdateComplete) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.subtitle_manual_update),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (error != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error?.asString() ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    when (entityType) {
                        EntityType.Component -> {
                            Text(
                                text = stringResource(R.string.label_name),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("Component name") },
                                leadingIcon = { Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = inputColors,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = stringResource(R.string.label_sku),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                placeholder = { Text("e.g. B72, M-102") },
                                leadingIcon = { Icon(Icons.Outlined.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = inputColors,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = stringResource(R.string.label_category),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                placeholder = { Text("Category / Location") },
                                leadingIcon = { Icon(Icons.Outlined.Category, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = inputColors,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                        EntityType.Customer -> {
                            Text(
                                text = stringResource(R.string.label_name),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("Customer or company name") },
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = inputColors,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = stringResource(R.string.label_phone),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                placeholder = { Text("Phone number") },
                                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = inputColors,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = stringResource(R.string.label_email),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("Email address") },
                                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = inputColors,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = stringResource(R.string.label_address),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                placeholder = { Text("Business address") },
                                leadingIcon = { Icon(Icons.Outlined.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = inputColors,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                        EntityType.Mold -> {
                            Text(
                                text = stringResource(R.string.label_mold_code),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = moldCode,
                                onValueChange = { moldCode = it },
                                placeholder = { Text("e.g. MOLD-001") },
                                leadingIcon = { Icon(Icons.Outlined.PrecisionManufacturing, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = inputColors,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = stringResource(R.string.label_location),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                placeholder = { Text("Rack / Shelf location") },
                                leadingIcon = { Icon(Icons.Outlined.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = inputColors,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = stringResource(R.string.label_mold_status),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val statuses = listOf(
                                    MoldStatus.Active to stringResource(R.string.mold_status_active),
                                    MoldStatus.InMaintenance to stringResource(R.string.mold_status_maintenance),
                                    MoldStatus.Retired to stringResource(R.string.mold_status_retired)
                                )
                                statuses.forEach { (status, label) ->
                                    FilterChip(
                                        selected = moldStatus == status,
                                        onClick = { moldStatus = status },
                                        label = { Text(label) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                        else -> {}
                    }

                    if (entityType != EntityType.Customer) {
                        val labelRes = if (entityType == EntityType.Mold)
                            R.string.label_cavity_count
                        else
                            R.string.label_quantity
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = {
                                quantity = it
                                hasError = it.toIntOrNull()?.let { v -> if (entityType == EntityType.Mold) v > 0 else v >= 0 } != true
                            },
                            placeholder = { Text("0") },
                            leadingIcon = { Icon(Icons.Outlined.Numbers, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                            isError = hasError,
                            supportingText = {
                                if (hasError) Text(stringResource(R.string.error_invalid_positive_number), color = MaterialTheme.colorScheme.error)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = inputColors,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Text(
                        text = stringResource(R.string.label_note),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("Optional notes or description") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = inputColors,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val parsedQty = if (entityType == EntityType.Customer) 0 else quantity.toIntOrNull()?.takeIf { v -> if (entityType == EntityType.Mold) v > 0 else v >= 0 }
                            if (parsedQty != null) {
                                if (entityId != null) {
                                    when (entityType) {
                                        EntityType.Component -> viewModel.updateComponent(entityId, name, sku, category, parsedQty, note)
                                        EntityType.Customer -> viewModel.updateCustomer(entityId, name, phone, email, address, note)
                                        EntityType.Mold -> viewModel.updateMold(entityId, moldCode, parsedQty, location, moldStatus, note)
                                        else -> {}
                                    }
                                } else {
                                    when (entityType) {
                                        EntityType.Component -> viewModel.createComponent(name, sku, category, parsedQty, note)
                                        EntityType.Customer -> viewModel.createCustomer(name, phone, email, address, note)
                                        EntityType.Mold -> viewModel.createMold(moldCode, parsedQty, location, moldStatus, note)
                                        else -> {}
                                    }
                                }
                            } else {
                                hasError = true
                            }
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.btn_save),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}



