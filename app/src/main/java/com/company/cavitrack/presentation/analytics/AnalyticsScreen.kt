package com.company.cavitrack.presentation.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.company.cavitrack.presentation.components.ListCard

@Composable
fun AnalyticsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Analytics & Reports", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        ListCard(onClick = {}) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Mold Utilization", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = { 0.75f }, modifier = Modifier.fillMaxWidth())
                Text("75% Active", style = MaterialTheme.typography.bodySmall)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ListCard(onClick = {}) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Stock Turnover Ratio", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("High", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Exports", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { /* Trigger backend /api/export/csv */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export Inventory to CSV")
        }
    }
}
