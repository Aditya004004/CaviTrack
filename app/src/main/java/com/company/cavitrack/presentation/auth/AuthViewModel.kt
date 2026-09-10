package com.company.cavitrack.presentation.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.company.cavitrack.domain.usecase.auth.AuthUseCases
import com.company.cavitrack.util.SessionManager
import com.company.cavitrack.util.DataResult

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data object Deleting : AuthState()
    data object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(if (authUseCases.getCurrentUserUid() != null) AuthState.Authenticated else AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = sessionManager.currentUser
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = sessionManager.currentFirebaseUser
        )

    init {
        viewModelScope.launch {
            sessionManager.currentUser.collect { user ->
                if (user == null) {
                    if (_authState.value !is AuthState.Deleting && _authState.value !is AuthState.Loading) {
                        _authState.value = AuthState.Unauthenticated
                    }
                } else {
                    if (_authState.value !is AuthState.Deleting) {
                        _authState.value = AuthState.Authenticated
                    }
                }
            }
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun resetAuthState() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }



    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = authUseCases.login(email, password)) {
                is DataResult.Success -> {
                    _authState.value = AuthState.Authenticated
                }
                is DataResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = authUseCases.register(name, email, password)) {
                is DataResult.Success -> {
                    _authState.value = AuthState.Authenticated
                }
                is DataResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun checkAuthStatus() {
        if (authUseCases.getCurrentUserUid() != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun logout() {
        viewModelScope.launch {
            authUseCases.logout()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val uid = authUseCases.getCurrentUserUid()
            if (uid == null) {
                _authState.value = AuthState.Unauthenticated
                return@launch
            }

            _authState.value = AuthState.Deleting

            val result = authUseCases.deleteAccount()
            
            if (result is DataResult.Success) {
                _authState.value = AuthState.Unauthenticated
            } else if (result is DataResult.Error) {
                _authError.value = result.message
                _authState.value = AuthState.Authenticated
            }
        }
    }

    fun reloadUser() {
        viewModelScope.launch {
            authUseCases.reloadUser()
        }
    }

    fun sendPasswordReset(email: String, onResult: (DataResult<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = authUseCases.sendPasswordResetEmail(email)
            onResult(result)
        }
    }

    fun validateEmail(email: String): com.company.cavitrack.domain.usecase.auth.ValidationResult =
        authUseCases.validateEmail(email)

    fun validatePassword(password: String, isRegistration: Boolean = false): com.company.cavitrack.domain.usecase.auth.ValidationResult =
        authUseCases.validatePassword(password, isRegistration)

    fun validateName(name: String): com.company.cavitrack.domain.usecase.auth.ValidationResult =
        authUseCases.validateName(name)

    fun isPasswordStrong(password: String): Boolean =
        authUseCases.validatePassword(password, isRegistration = true) is com.company.cavitrack.domain.usecase.auth.ValidationResult.Success
}

