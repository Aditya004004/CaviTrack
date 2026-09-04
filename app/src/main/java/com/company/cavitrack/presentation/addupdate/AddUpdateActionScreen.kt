package com.company.cavitrack.presentation.addupdate





import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUpdateActionScreen(
    entityType: com.company.cavitrack.domain.model.EntityType?,
    onNavigateToManual: (com.company.cavitrack.domain.model.EntityType?) -> Unit,
    onNavigateToPhoto: (com.company.cavitrack.domain.model.EntityType?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        var selectedType by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(entityType ?: com.company.cavitrack.domain.model.EntityType.Component) }
        
        Text(
            text = "Update Entry",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        if (entityType == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(com.company.cavitrack.domain.model.EntityType.Component, com.company.cavitrack.domain.model.EntityType.Customer, com.company.cavitrack.domain.model.EntityType.Mold).forEachIndexed { index, type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.name, style = MaterialTheme.typography.labelLarge) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .height(32.dp)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Button(
            onClick = { onNavigateToManual(selectedType) },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                Text("Manual Update", style = MaterialTheme.typography.titleMedium)
                Text("Enter details using keyboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        val isPhotoUpdateSupported = selectedType == com.company.cavitrack.domain.model.EntityType.Component
        FilledTonalButton(
            onClick = { onNavigateToPhoto(selectedType) },
            enabled = isPhotoUpdateSupported,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                Text("Photo Update", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (isPhotoUpdateSupported) "Attach image from camera/gallery" else "Coming soon for ${selectedType.name}", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}


