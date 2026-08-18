package com.company.cavitrack.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.cavitrack.data.remote.api.CaviTrackApi
import com.company.cavitrack.data.remote.dto.LoginRequestDto
import com.company.cavitrack.data.remote.dto.RegisterRequestDto
import com.company.cavitrack.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: CaviTrackApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        val token = tokenManager.getAccessToken()
        if (token != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.login(LoginRequestDto(email, pass))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        tokenManager.saveTokens(body.token, body.refreshToken ?: "")
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error("Empty response")
                    }
                } else {
                    _authState.value = AuthState.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.register(RegisterRequestDto(name, email, pass))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        tokenManager.saveTokens(body.token, body.refreshToken ?: "")
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error("Empty response")
                    }
                } else {
                    _authState.value = AuthState.Error("Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun logout() {
        tokenManager.clearTokens()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
