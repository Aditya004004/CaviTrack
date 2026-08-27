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
import com.company.cavitrack.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {

    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data object Deleting : AuthState()
    data object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val firebaseAuth: FirebaseAuth,
    private val repository: com.company.cavitrack.domain.repository.InventoryRepository,
    sessionManager: com.company.cavitrack.util.SessionManager,
    private val syncScheduler: com.company.cavitrack.util.SyncScheduler,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firebaseMessaging: com.google.firebase.messaging.FirebaseMessaging
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(if (tokenManager.hasValidToken()) AuthState.Authenticated else AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val currentUser = sessionManager.currentUser

    fun clearAuthError() {
        _authError.value = null
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
        if (tokenManager.hasValidToken()) {
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
                    _authError.value = "You have unsynced changes. Please sync before logging out."
                    return@launch
                }
                if (force) {
                    repository.clearAllPendingActions()
                }
                val token = firebaseMessaging.token.await()
                val uid = firebaseAuth.currentUser?.uid
                if (uid != null && token.isNotEmpty()) {
                    firebaseFirestore
                        .collection("users").document(uid)
                        .collection("fcmTokens").document(token)
                        .delete().await()
                }
                firebaseMessaging.deleteToken().await()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                // Ignore failure if not connected
            }
            tokenManager.clearToken()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun syncData() {
        syncScheduler.scheduleOneTimeSync(ExistingWorkPolicy.REPLACE)
    }

    fun deleteAccount(force: Boolean = false) {
        viewModelScope.launch {
            if (!force && repository.hasPendingActions()) {
                _authError.value = "You have unsynced changes. Please sync before deleting your account."
                return@launch
            }
            if (force) {
                repository.clearAllPendingActions()
            }
            _authState.value = AuthState.Deleting
            try {
                val user = firebaseAuth.currentUser
                if (user != null) {
                    val uid = user.uid
                    // Clear Firestore and Room data first
                    repository.clearUserData(uid)
                    
                    // Clear Storage data
                    try {
                        val storageRef = firebaseStorage.reference.child("photos/$uid")
                        storageRef.listAll().await().items.forEach { it.delete().await() }
                    } catch (e: Exception) {
                        // Ignore storage errors if folder doesn't exist
                    }

                    // Finally, delete the Auth user
                    user.delete().await()
                }
                tokenManager.clearToken()
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







