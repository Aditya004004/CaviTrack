package com.company.cavitrack.domain.repository

interface AuthRepository {
    suspend fun registerFcmToken()
    suspend fun clearFcmToken()
    suspend fun clearStorage()
    suspend fun deleteAccount()
}