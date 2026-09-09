package com.company.cavitrack.presentation.settings








import androidx.compose.foundation.background
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
import androidx.core.net.toUri

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.ui.res.stringResource
import com.company.cavitrack.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(authViewModel: AuthViewModel = hiltViewModel()) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val userName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "User"
    val userEmail = currentUser?.email ?: "No Email"
    var showLogoutDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var showDeleteAccountDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val authError by authViewModel.authError.collectAsStateWithLifecycle()

    if (authError != null) {
        AlertDialog(
            onDismissRequest = { authViewModel.clearAuthError() },
            title = { Text(stringResource(R.string.title_error)) },
            text = { Text(authError ?: "") },
            confirmButton = {
                TextButton(onClick = { authViewModel.clearAuthError() }) {
                    Text(stringResource(R.string.btn_ok))
                }
            }
        )
    }

    val context = LocalContext.current
    val requestPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, R.string.toast_notifications_enabled, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, R.string.toast_notifications_denied, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
            text = stringResource(R.string.section_preferences),
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
                        headlineContent = { Text(stringResource(R.string.label_notifications)) },
                        supportingContent = { Text(stringResource(R.string.desc_notifications)) },
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
                                Toast.makeText(context, R.string.toast_notifications_already_enabled, Toast.LENGTH_SHORT).show()
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
                    headlineContent = { Text(stringResource(R.string.label_privacy_policy)) },
                    supportingContent = { Text(stringResource(R.string.desc_privacy_policy)) },
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
                            val uri = privacyPolicyUrl.toUri()
                            if (uri.scheme.equals("https", ignoreCase = true) || uri.scheme.equals("http", ignoreCase = true)) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, R.string.toast_invalid_privacy_url, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: android.content.ActivityNotFoundException) {
                            Toast.makeText(context, R.string.toast_no_browser, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, R.string.toast_unable_to_open_link, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Account Section
        Text(
            text = stringResource(R.string.section_account),
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
                            contentDescription = stringResource(R.string.label_logout),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.label_logout)) },
                    supportingContent = { Text(stringResource(R.string.desc_logout)) },
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

                val webDeletionUrl = stringResource(R.string.web_account_deletion_url)
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = stringResource(R.string.label_request_web_deletion),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.label_request_web_deletion)) },
                    supportingContent = { Text(stringResource(R.string.desc_request_web_deletion)) },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.clickable {
                        try {
                            val uri = webDeletionUrl.toUri()
                            if (uri.scheme.equals("https", ignoreCase = true) || uri.scheme.equals("http", ignoreCase = true)) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, R.string.toast_invalid_deletion_url, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: android.content.ActivityNotFoundException) {
                            Toast.makeText(context, R.string.toast_no_browser, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, R.string.toast_unable_to_open_link, Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Outlined.DeleteForever,
                            contentDescription = stringResource(R.string.label_delete_account),
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.label_delete_account), color = MaterialTheme.colorScheme.error) },
                    supportingContent = { Text(stringResource(R.string.desc_delete_account)) },
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
            text = "CaviTrack v${com.company.cavitrack.BuildConfig.VERSION_NAME}",
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
                    text = stringResource(R.string.label_logout),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.msg_confirm_logout_question),
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
                    Text(stringResource(R.string.btn_yes_logout))
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurface)
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
                    text = stringResource(R.string.label_delete_account),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.msg_delete_account),
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
                    Text(stringResource(R.string.btn_permanently_delete))
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}



