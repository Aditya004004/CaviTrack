package com.company.cavitrack.presentation.addupdate

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AddUpdateActionScreen(entityType: String?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "How would you like to update?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { /* TODO navigate to manual update */ },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Manual Update")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { /* TODO navigate to photo update */ },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Photo Update")
        }
    }
}
