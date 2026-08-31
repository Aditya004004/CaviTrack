package com.company.cavitrack.presentation.settings








import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.cavitrack.presentation.auth.AuthViewModel
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.clickable

@Composable
fun SettingsScreen(authViewModel: AuthViewModel = hiltViewModel()) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val userName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "User"
    val userEmail = currentUser?.email ?: "No Email"
    var showLogoutDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var showDeleteAccountDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val authError by authViewModel.authError.collectAsStateWithLifecycle()
    val pendingAction by authViewModel.pendingDestructiveAction.collectAsStateWithLifecycle()

    if (authError != null) {
        AlertDialog(
            onDismissRequest = { authViewModel.clearAuthError() },
            title = { Text("Error") },
            text = { Text(authError ?: "") },
            confirmButton = {
                TextButton(onClick = { authViewModel.clearAuthError() }) {
                    Text("OK")
                }
            },
            dismissButton = {
                if (pendingAction != null) {
                    TextButton(onClick = {
                        val actionToPerform = pendingAction
                        authViewModel.clearAuthError()
                        if (actionToPerform == com.company.cavitrack.presentation.auth.PendingDestructiveAction.DELETE_ACCOUNT) {
                            authViewModel.deleteAccount(force = true)
                        } else if (actionToPerform == com.company.cavitrack.presentation.auth.PendingDestructiveAction.LOGOUT) {
                            authViewModel.logout(force = true)
                        }
                    }) {
                        Text("Discard & Proceed", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
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
                    Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        
        val context = LocalContext.current
        
        // Notification Permission Launcher
        val requestPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Notifications permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ListItem(
                headlineContent = { Text("Enable Notifications") },
                supportingContent = { Text("Receive alerts for low inventory and sync status.") },
                modifier = Modifier.clickable {
                    val permission = android.Manifest.permission.POST_NOTIFICATIONS
                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(permission)
                    } else {
                        Toast.makeText(context, "Notifications are already enabled.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        val privacyPolicyUrl = androidx.compose.ui.res.stringResource(id = com.company.cavitrack.R.string.privacy_policy_url)
        ListItem(
            headlineContent = { Text("Privacy Policy") },
            modifier = Modifier.clickable {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(privacyPolicyUrl))
                    context.startActivity(intent)
                } catch (e: android.content.ActivityNotFoundException) {
                    Toast.makeText(context, "No web browser installed.", Toast.LENGTH_SHORT).show()
                }
            }
        )
        
        ListItem(
            headlineContent = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
            modifier = Modifier.clickable { showDeleteAccountDialog = true }
        )

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to permanently delete your account and all associated data? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        authViewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}



