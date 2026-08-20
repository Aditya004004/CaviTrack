package com.company.cavitrack.presentation.addupdate.manual

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ManualUpdateScreen(
    entityType: String,
    entityId: String?,
    viewModel: ManualUpdateViewModel = hiltViewModel(),
    onUpdateComplete: () -> Unit = {}
) {
    var quantity by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    
    val isSaved by viewModel.isSaved.collectAsState()
    val error by viewModel.error.collectAsState()
    
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
        }

        OutlinedTextField(
            value = quantity,
            onValueChange = { 
                quantity = it
                hasError = it.toIntOrNull() == null
            },
            label = { Text("Quantity") },
            isError = hasError,
            supportingText = { if (hasError) Text("Must be a valid number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                val parsedQty = quantity.toIntOrNull()
                if (parsedQty != null) {
                    viewModel.updateQuantity(entityType, entityId, parsedQty, note)
                } else {
                    hasError = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Save Update")
        }
    }
}
