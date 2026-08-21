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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.clickable

@Composable
fun SettingsScreen(authViewModel: AuthViewModel = hiltViewModel()) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "User"
    val userEmail = currentUser?.email ?: "No Email"
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color(0xFF5C636B))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Preferences", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        ListItem(
            headlineContent = { Text("Units of Measure") },
            supportingContent = { Text("pcs (Default)") },
            trailingContent = { Icon(androidx.compose.material.icons.Icons.Default.ArrowDropDown, contentDescription = null) }
        )
        ListItem(
            headlineContent = { Text("Default Low-Stock Threshold") },
            supportingContent = { Text("10 items") }
        )
        ListItem(
            headlineContent = { Text("Notifications") },
            trailingContent = { Switch(checked = true, onCheckedChange = {}) }
        )

        val context = androidx.compose.ui.platform.LocalContext.current
        ListItem(
            headlineContent = { Text("Privacy Policy") },
            modifier = Modifier.clickable {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://policies.google.com/privacy"))
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFDC2626))
        ) {
            Text("Logout")
        }
    }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFDC2626))
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
