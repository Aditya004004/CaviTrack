package com.company.cavitrack.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String = "",
    val password: String
)

@Serializable
data class RegisterRequestDto(
    val name: String = "",
    val email: String = "",
    val password: String,
    val role: String = "Viewer"
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val refreshToken: String? = null,
    val user: UserDto
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponseDto(
    val token: String
)

@Serializable
data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String
)

