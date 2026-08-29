package com.company.cavitrack.presentation.auth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException






import kotlinx.coroutines.flow.MutableStateFlow
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.work.ExistingWorkPolicy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

enum class PendingDestructiveAction { LOGOUT, DELETE_ACCOUNT }

sealed class AuthState {

    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data object Deleting : AuthState()
    data object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val authRepository: com.company.cavitrack.domain.repository.AuthRepository,
    private val repository: com.company.cavitrack.domain.repository.InventoryRepository,
    sessionManager: com.company.cavitrack.util.SessionManager,
    private val syncScheduler: com.company.cavitrack.util.SyncScheduler
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(if (firebaseAuth.currentUser != null) AuthState.Authenticated else AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    
    private val _pendingDestructiveAction = MutableStateFlow<PendingDestructiveAction?>(null)
    val pendingDestructiveAction: StateFlow<PendingDestructiveAction?> = _pendingDestructiveAction.asStateFlow()

    val currentUser = sessionManager.currentUser

    fun clearAuthError() {
        _authError.value = null
        _pendingDestructiveAction.value = null
    }

    fun resetAuthState() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    init {
        viewModelScope.launch {
            sessionManager.currentUser.collect { user ->
                if (user != null) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                authRepository.registerFcmToken()
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                    is FirebaseAuthInvalidUserException -> "No account found with this email."
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
                
                authRepository.registerFcmToken()
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                val errorMessage = when (e) {
                    is FirebaseAuthUserCollisionException -> "An account already exists with this email."
                    is FirebaseAuthWeakPasswordException -> "Password is too weak."
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                    else -> e.message ?: "Registration failed"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun checkAuthStatus() {
        if (firebaseAuth.currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    @Suppress("DEPRECATION")
    fun logout(force: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!force && repository.hasPendingActions()) {
                    _pendingDestructiveAction.value = PendingDestructiveAction.LOGOUT
                    _authError.value = "You have unsynced changes. Please sync before logging out."
                    return@launch
                }
                if (force) {
                    repository.clearAllPendingActions()
                }
                authRepository.clearFcmToken()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                // Ignore failure if not connected
            }
            syncScheduler.cancelAll()
            firebaseAuth.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun syncData() {
        syncScheduler.scheduleManualSync()
    }

    fun deleteAccount(force: Boolean = false) {
        viewModelScope.launch {
            if (!force && repository.hasPendingActions()) {
                _pendingDestructiveAction.value = PendingDestructiveAction.DELETE_ACCOUNT
                _authError.value = "You have unsynced changes. Please sync before deleting your account."
                return@launch
            }
            if (force) {
                repository.clearAllPendingActions()
            }
            
            val user = firebaseAuth.currentUser
            if (user != null) {
                val lastSignIn = user.metadata?.lastSignInTimestamp ?: 0
                // Require login within the last 5 minutes to proceed
                if (System.currentTimeMillis() - lastSignIn > 5 * 60 * 1000) {
                    _authError.value = "Recent login required. Please log out, log back in, and try again."
                    return@launch
                }
            }

            _authState.value = AuthState.Deleting
            try {
                if (user != null) {
                    val uid = user.uid
                    // Clear Firestore and Room data first
                    repository.clearUserData(uid)
                    
                    // Clear Storage data
                    authRepository.clearStorage()

                    // Finally, delete the Auth user
                    authRepository.deleteAccount()
                }
                firebaseAuth.signOut()
                _authState.value = AuthState.Unauthenticated
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                _authError.value = "Recent login required. Please log out, log back in, and try again."
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _authError.value = e.message ?: "Failed to delete account."
                _authState.value = AuthState.Authenticated
            }
        }
    }
}







