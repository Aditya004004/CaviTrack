package com.company.cavitrack.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.util.TokenManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {

    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(if (tokenManager.hasValidToken()) AuthState.Authenticated else AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "No account found with this email."
                    else -> e.message ?: "Login failed"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Firebase creates the user and signs them in
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                
                // Optionally save the 'name' to the User profile
                val user = firebaseAuth.currentUser
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user?.updateProfile(profileUpdates)?.await()
                
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "An account already exists with this email."
                    is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "Password is too weak."
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                    else -> e.message ?: "Registration failed"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun checkAuthStatus() {
        if (tokenManager.hasValidToken()) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun logout() {
        tokenManager.clearToken()
        _authState.value = AuthState.Unauthenticated
    }
}

