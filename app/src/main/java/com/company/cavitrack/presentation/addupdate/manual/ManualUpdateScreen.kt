package com.company.cavitrack.presentation.addupdate.manual


import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ManualUpdateScreen(
    entityType: com.company.cavitrack.domain.model.EntityType,
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
    
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentQty by viewModel.currentQty.collectAsStateWithLifecycle()
    
    LaunchedEffect(entityId) {
        if (entityId != null) {
            viewModel.loadComponent(entityId)
        }
    }

    LaunchedEffect(currentQty) {
        if (currentQty != null && quantity.isEmpty()) {
            quantity = currentQty.toString()
        }
    }
    
    LaunchedEffect(isSaved) {
        if (isSaved) {
            onUpdateComplete()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Manual Update - $entityType", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        if (error != null) {
            Text(error ?: "", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        } else if (hasUnsupportedError) {
            Text("Update not supported for this entity yet.", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (entityId == null) {
            when (entityType) {
                com.company.cavitrack.domain.model.EntityType.Component -> {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU / Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category / Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                com.company.cavitrack.domain.model.EntityType.Customer -> {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                com.company.cavitrack.domain.model.EntityType.Mold -> {
                    OutlinedTextField(
                        value = moldCode,
                        onValueChange = { moldCode = it },
                        label = { Text("Mold Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                else -> {}
            }
        }

        if (entityType != com.company.cavitrack.domain.model.EntityType.Customer) {
            OutlinedTextField(
                value = quantity,
                onValueChange = { 
                    quantity = it
                    hasError = it.toIntOrNull()?.takeIf { v -> v >= 0 } == null
                },
                label = { Text(if (entityId == null) "Value / Count" else "Quantity") },
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
            label = { Text("Reason/Note") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val parsedQty = if (entityType == com.company.cavitrack.domain.model.EntityType.Customer) 0 else quantity.toIntOrNull()?.takeIf { v -> v >= 0 }
                if (parsedQty != null) {
                    if (entityId != null) {
                        if (entityType == com.company.cavitrack.domain.model.EntityType.Component) {
                            viewModel.updateComponentQuantity(entityId, parsedQty, note)
                        } else {
                            hasUnsupportedError = true // Only Component updates are supported right now
                        }
                    } else {
                        when (entityType) {
                            com.company.cavitrack.domain.model.EntityType.Component -> viewModel.createComponent(name, sku, category, parsedQty, note)
                            com.company.cavitrack.domain.model.EntityType.Customer -> viewModel.createCustomer(name, phone, email, address, note)
                            com.company.cavitrack.domain.model.EntityType.Mold -> viewModel.createMold(moldCode, parsedQty, location, note)
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
            Text(if (isSaving) "Saving..." else "Save Update")
        }
    }
}



