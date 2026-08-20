package com.company.cavitrack.presentation.addupdate

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUpdateActionScreen(
    entityType: String?,
    onNavigateToManual: (String?) -> Unit,
    onNavigateToPhoto: (String?) -> Unit,
    onNavigateToScan: (String?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var selectedType by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(entityType ?: "Component") }
        
        Text(
            text = "How would you like to update?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (entityType == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Component", "Customer", "Mold").forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = { onNavigateToManual(selectedType) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Manual Update")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { onNavigateToPhoto(selectedType) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Photo Update")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { onNavigateToScan(selectedType) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Scan Barcode")
        }
    }
}
