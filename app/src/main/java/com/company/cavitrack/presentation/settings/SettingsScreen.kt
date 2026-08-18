package com.company.cavitrack.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.cavitrack.presentation.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(authViewModel: AuthViewModel = hiltViewModel()) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "User"
    val userEmail = currentUser?.email ?: "No Email"

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Person, contentDescription = "Profile", modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(userName, style = MaterialTheme.typography.headlineMedium)
                Text(userEmail, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Preferences", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dark Mode (System Default)", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = androidx.compose.foundation.isSystemInDarkTheme(), onCheckedChange = {  }, enabled = false)
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { authViewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }
    }
}
