package com.company.cavitrack.presentation.addupdate.manual


import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.cavitrack.domain.model.EntityType

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
    var hasUnsupportedError by rememberSaveable { mutableStateOf(false) }
    
    var name by rememberSaveable { mutableStateOf("") }
    var sku by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var moldCode by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentQty by viewModel.currentQty.collectAsStateWithLifecycle()
    
    val scrollState = androidx.compose.foundation.rememberScrollState()
    
    LaunchedEffect(entityId, entityType) {
        if (entityId != null && entityType == EntityType.Component) {
            viewModel.loadComponent(entityId)
        }
    }

    LaunchedEffect(currentQty) {
        if (currentQty != null && quantity.isEmpty()) {
            quantity = currentQty.toString()
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.isSaved.collect {
            onUpdateComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(scrollState)
    ) {
        val title = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.manual_update_title)
        Text("$title - $entityType", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            Text(error?.asString() ?: "", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        } else if (hasUnsupportedError) {
            Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.unsupported_update), color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (entityId == null) {
            when (entityType) {
                EntityType.Component -> {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_sku)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_category)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                EntityType.Customer -> {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_phone)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_email)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_address)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                EntityType.Mold -> {
                    OutlinedTextField(
                        value = moldCode,
                        onValueChange = { moldCode = it },
                        label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_mold_code)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_location)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                else -> {}
            }
        }

        if (entityType != EntityType.Customer) {
            OutlinedTextField(
                value = quantity,
                onValueChange = { 
                    quantity = it
                    hasError = it.toIntOrNull()?.takeIf { v -> v >= 0 } == null
                },
                label = { 
                    val labelRes = if (entityType == EntityType.Mold) 
                        com.company.cavitrack.R.string.label_cavity_count 
                    else 
                        com.company.cavitrack.R.string.label_quantity
                    Text(androidx.compose.ui.res.stringResource(labelRes)) 
                },
                isError = hasError,
                supportingText = { if (hasError) Text("Must be a valid positive number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_note)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val parsedQty = if (entityType == EntityType.Customer) 0 else quantity.toIntOrNull()?.takeIf { v -> v >= 0 }
                if (parsedQty != null) {
                    if (entityId != null) {
                        if (entityType == EntityType.Component) {
                            viewModel.updateComponentQuantity(entityId, parsedQty, note)
                        } else {
                            hasUnsupportedError = true // Only Component updates are supported right now
                        }
                    } else {
                        when (entityType) {
                            EntityType.Component -> viewModel.createComponent(name, sku, category, parsedQty, note)
                            EntityType.Customer -> viewModel.createCustomer(name, phone, email, address, note)
                            EntityType.Mold -> viewModel.createMold(moldCode, parsedQty, location, note)
                            else -> hasUnsupportedError = true
                        }
                    }
                } else {
                    hasError = true
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(if (isSaving) androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.msg_saving) else androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.action_save_update))
        }
    }
}



