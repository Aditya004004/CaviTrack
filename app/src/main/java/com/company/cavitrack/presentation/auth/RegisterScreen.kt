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
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onRegisterSuccess()
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
        Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.title_register), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        if (authState is AuthState.Error) {
            Text((authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        val emptyEmailError = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.error_email_empty)
        val invalidEmailError = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.error_email_invalid)
        val emptyPasswordError = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.error_password_empty)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = { Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.label_name)) },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

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
                if (name.isBlank()) { nameError = "Name cannot be empty"; hasError = true }
                
                if (email.isBlank()) { emailError = emptyEmailError; hasError = true }
                else if (!email.matches(EMAIL_REGEX)) { emailError = invalidEmailError; hasError = true }
                
                if (password.isBlank()) { passwordError = emptyPasswordError; hasError = true }
                else if (password.length < 8 || !password.any { it.isLetter() } || !password.any { it.isDigit() }) {
                    passwordError = "Password must be at least 8 characters with a letter and a number"
                    hasError = true
                }
                if (!hasError) authViewModel.register(name, email, password) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.btn_register))
            }
        }
        
        TextButton(onClick = onNavigateToLogin) {
            Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.link_to_login))
        }
    }
}





