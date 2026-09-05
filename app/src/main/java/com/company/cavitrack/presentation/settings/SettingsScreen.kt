package com.company.cavitrack.presentation.settings








import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.clickable

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Policy

@OptIn(ExperimentalMaterial3Api::class)
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

    val context = LocalContext.current
    val requestPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Profile Card
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.padding(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    Text(
                        userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preferences Section
        Text(
            text = "PREFERENCES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    ListItem(
                        leadingContent = {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        headlineContent = { Text("Notifications") },
                        supportingContent = { Text("Receive alerts for low inventory and sync status") },
                        trailingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.clickable {
                            val permission = android.Manifest.permission.POST_NOTIFICATIONS
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                requestPermissionLauncher.launch(permission)
                            } else {
                                Toast.makeText(context, "Notifications are already enabled.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }

                val privacyPolicyUrl = androidx.compose.ui.res.stringResource(id = com.company.cavitrack.R.string.privacy_policy_url)
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Policy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    headlineContent = { Text("Privacy Policy") },
                    supportingContent = { Text("View terms and data privacy policy") },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.clickable {
                        try {
                            val uri = android.net.Uri.parse(privacyPolicyUrl)
                            if (uri.scheme.equals("https", ignoreCase = true) || uri.scheme.equals("http", ignoreCase = true)) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Invalid privacy policy URL.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: android.content.ActivityNotFoundException) {
                            Toast.makeText(context, "No web browser installed.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Unable to open link.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Account Section
        Text(
            text = "ACCOUNT",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Log Out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    headlineContent = { Text("Log Out") },
                    supportingContent = { Text("Sign out of your account on this device") },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.clickable { showLogoutDialog = true }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Outlined.DeleteForever,
                            contentDescription = "Delete Account",
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    headlineContent = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
                    supportingContent = { Text("Permanently delete account and all data") },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.clickable { showDeleteAccountDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "CaviTrack v1.1",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    if (showLogoutDialog) {
        ModalBottomSheet(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log Out",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Are you sure you want to log out of your account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Log Out")
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDeleteAccountDialog) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteAccountDialog = false },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Delete Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Are you sure you want to permanently delete your account and all associated data? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        authViewModel.deleteAccount()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Permanently Delete")
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}



