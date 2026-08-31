package com.company.cavitrack.presentation.auth


import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.saveable.rememberSaveable

private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    val scrollState = androidx.compose.foundation.rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.title_login), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        if (authState is AuthState.Error) {
            Text((authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        val emptyEmailError = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.error_email_empty)
        val invalidEmailError = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.error_email_invalid)
        val emptyPasswordError = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.error_password_empty)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_email)) },
            isError = emailError != null,
            supportingText = { emailError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_password)) },
            isError = passwordError != null,
            supportingText = { passwordError?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { 
                var hasError = false
                if (email.isBlank()) { emailError = emptyEmailError; hasError = true }
                else if (!email.matches(EMAIL_REGEX)) { emailError = invalidEmailError; hasError = true }
                
                if (password.isBlank()) { passwordError = emptyPasswordError; hasError = true }
                if (!hasError) authViewModel.login(email, password) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.btn_login))
            }
        }
        
        TextButton(onClick = onNavigateToRegister) {
            Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.link_to_register))
        }
    }
}





