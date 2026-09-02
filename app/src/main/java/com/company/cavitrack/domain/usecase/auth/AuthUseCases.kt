package com.company.cavitrack.domain.usecase.auth

import com.company.cavitrack.domain.repository.AuthRepository
import com.company.cavitrack.domain.repository.InventoryRepository
import com.company.cavitrack.util.DataResult
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): DataResult<Unit> {
        val result = authRepository.signIn(email, password)
        if (result is DataResult.Success) {
            authRepository.registerPushToken()
        }
        return result
    }
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): DataResult<Unit> {
        val result = authRepository.signUp(name, email, password)
        if (result is DataResult.Success) {
            authRepository.registerPushToken()
        }
        return result
    }
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val localMetricsRepository: com.company.cavitrack.data.local.LocalMetricsRepository
) {
    suspend operator fun invoke() {
        try {
            authRepository.clearPushToken()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // Ignore failure if not connected
        }
        localMetricsRepository.clear()
        authRepository.signOut()
    }
}

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val inventoryRepository: InventoryRepository,
    private val localMetricsRepository: com.company.cavitrack.data.local.LocalMetricsRepository
) {
    suspend operator fun invoke(): DataResult<Unit> {
        val uid = authRepository.getCurrentUserUid()
        if (uid == null) {
            return DataResult.Error("User not authenticated")
        }

        try {
            inventoryRepository.clearUserData() // UID is resolved internally
            authRepository.clearUserPhotos()
            localMetricsRepository.clear()
        } catch (e: Exception) {
            if (com.company.cavitrack.BuildConfig.DEBUG) {
                android.util.Log.e("DeleteAccountUseCase", "Failed to clear user data.", e)
            }
        }
        
        // Delete the Auth user after data is cleared
        val result = authRepository.deleteAccount()
        
        if (result is DataResult.Success) {
            authRepository.signOut()
        }
        return result
    }
}

class GetCurrentUserUidUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): String? {
        return authRepository.getCurrentUserUid()
    }
}

class IsEmailVerifiedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.isEmailVerified()
    }
}

class ReloadUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): DataResult<Unit> {
        return authRepository.reloadUser()
    }
}

data class AuthUseCases @Inject constructor(
    val login: LoginUseCase,
    val register: RegisterUseCase,
    val logout: LogoutUseCase,
    val deleteAccount: DeleteAccountUseCase,
    val getCurrentUserUid: GetCurrentUserUidUseCase,
    val isEmailVerified: IsEmailVerifiedUseCase,
    val reloadUser: ReloadUserUseCase
)
