package com.company.cavitrack.domain.repository

import com.company.cavitrack.util.DataResult

interface AuthRepository {
    suspend fun registerPushToken()
    suspend fun clearPushToken()
    suspend fun clearUserPhotos()
    suspend fun deleteAccount(): DataResult<Unit>
    suspend fun signIn(email: String, password: String): DataResult<Unit>
    suspend fun signUp(name: String, email: String, password: String): DataResult<Unit>
    suspend fun signOut()
    fun getCurrentUserUid(): String?
    fun getCurrentUserEmail(): String?
    fun getCurrentUserName(): String?
    fun isEmailVerified(): Boolean
    suspend fun reloadUser(): DataResult<Unit>
}