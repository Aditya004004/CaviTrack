package com.company.cavitrack.presentation.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.company.cavitrack.R
import com.company.cavitrack.domain.usecase.auth.ValidationReason
import com.company.cavitrack.domain.usecase.auth.ValidationResult

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by rememberSaveable { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    val emptyEmailError = stringResource(R.string.error_email_empty)
    val invalidEmailError = stringResource(R.string.error_email_invalid)
    val emptyPasswordError = stringResource(R.string.error_password_empty)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Atmospheric radial gradients in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top-left subtle indigo/blue glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF818CF8).copy(alpha = if (isDark) 0.08f else 0.14f),
                        Color(0xFFC7D2FE).copy(alpha = if (isDark) 0.03f else 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.08f),
                    radius = size.width * 0.65f
                )
            )
            // Mid-right subtle soft arc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF93C5FD).copy(alpha = if (isDark) 0.06f else 0.12f),
                        Color(0xFFE0E7FF).copy(alpha = if (isDark) 0.02f else 0.04f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.96f, size.height * 0.22f),
                    radius = size.width * 0.52f
                )
            )
            // Bottom-left soft blur
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFBFDBFE).copy(alpha = if (isDark) 0.05f else 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.94f),
                    radius = size.width * 0.48f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Logo Squircle Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFDBEAFE)),
                shadowElevation = 1.dp,
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = stringResource(R.string.cd_app_logo),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two-tone App Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Cavi",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Track",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Tagline
            Text(
                text = stringResource(R.string.tagline_app),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Welcome Back Header
            Text(
                text = stringResource(R.string.welcome_back_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Welcome Subtitle
            Text(
                text = stringResource(R.string.welcome_back_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Elevated Form Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Email Section
                    Text(
                        text = stringResource(R.string.label_email),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                            authViewModel.resetAuthState()
                        },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.placeholder_enter_email),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        isError = emailError != null,
                        supportingText = {
                            emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                            focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Section
                    Text(
                        text = stringResource(R.string.label_password),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            authViewModel.resetAuthState()
                        },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.placeholder_enter_password),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = stringResource(
                                        if (passwordVisible) R.string.cd_hide_password else R.string.cd_show_password
                                    ),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = {
                            passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                            focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Auth State Error Display
                    if (authState is AuthState.Error) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = (authState as AuthState.Error).message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Action Button
                    Button(
                        onClick = {
                            var hasError = false
                            when (val emailResult = authViewModel.validateEmail(email)) {
                                is ValidationResult.Error -> {
                                    emailError = when (emailResult.reason) {
                                        ValidationReason.EMPTY_EMAIL -> emptyEmailError
                                        ValidationReason.INVALID_EMAIL -> invalidEmailError
                                        else -> invalidEmailError
                                    }
                                    hasError = true
                                }
                                is ValidationResult.Success -> {
                                    emailError = null
                                }
                            }

                            when (authViewModel.validatePassword(password)) {
                                is ValidationResult.Error -> {
                                    passwordError = emptyPasswordError
                                    hasError = true
                                }
                                is ValidationResult.Success -> {
                                    passwordError = null
                                }
                            }

                            if (!hasError) {
                                authViewModel.login(email, password)
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_login),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Forgot Password Link
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = stringResource(R.string.btn_forgot_password),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showForgotPasswordDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // OR Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                        Text(
                            text = stringResource(R.string.label_or),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Register Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.label_dont_have_account_prefix),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.btn_register_highlight),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateToRegister() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy Policy Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.agreement_prefix),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
                Text(
                    text = stringResource(R.string.label_privacy_policy),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        try {
                            val uri = privacyPolicyUrl.toUri()
                            if (uri.scheme.equals("https", ignoreCase = true) || uri.scheme.equals("http", ignoreCase = true)) {
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, R.string.toast_invalid_privacy_url, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, R.string.toast_no_browser, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, R.string.toast_unable_to_open_link, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            var resetEmail by rememberSaveable { mutableStateOf(email) }
            var isSendingReset by remember { mutableStateOf(false) }
            var resetError by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = {
                    if (!isSendingReset) showForgotPasswordDialog = false
                },
                title = {
                    Text(
                        text = stringResource(R.string.title_forgot_password_dialog),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.dialog_desc_forgot_password),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = {
                                resetEmail = it
                                resetError = null
                            },
                            label = { Text(stringResource(R.string.label_email)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            singleLine = true,
                            isError = resetError != null,
                            supportingText = {
                                if (resetError != null) {
                                    Text(resetError!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when (val validation = authViewModel.validateEmail(resetEmail)) {
                                is ValidationResult.Error -> {
                                    resetError = when (validation.reason) {
                                        ValidationReason.EMPTY_EMAIL -> emptyEmailError
                                        ValidationReason.INVALID_EMAIL -> invalidEmailError
                                        else -> invalidEmailError
                                    }
                                    return@Button
                                }
                                is ValidationResult.Success -> {}
                            }
                            isSendingReset = true
                            resetError = null
                            authViewModel.sendPasswordReset(resetEmail.trim()) { result ->
                                isSendingReset = false
                                when (result) {
                                    is com.company.cavitrack.util.DataResult.Success -> {
                                        showForgotPasswordDialog = false
                                        Toast.makeText(context, R.string.toast_password_reset_sent, Toast.LENGTH_LONG).show()
                                    }
                                    is com.company.cavitrack.util.DataResult.Error -> {
                                        resetError = result.message
                                    }
                                }
                            }
                        },
                        enabled = !isSendingReset && resetEmail.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSendingReset) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.btn_send_reset_link))
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showForgotPasswordDialog = false },
                        enabled = !isSendingReset
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}





