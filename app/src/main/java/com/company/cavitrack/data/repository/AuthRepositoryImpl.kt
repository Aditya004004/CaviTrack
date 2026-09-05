package com.company.cavitrack.data.repository

import android.content.Context
import android.util.Log
import com.company.cavitrack.domain.repository.AuthRepository
import com.company.cavitrack.util.DataResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging,
    private val storageRepository: com.company.cavitrack.domain.repository.StorageRepository
) : AuthRepository {

    override suspend fun registerPushToken() {
        try {
            val token = firebaseMessaging.getToken().await()
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null && token.isNotEmpty()) {
                val workManager = androidx.work.WorkManager.getInstance(context)
                val data = androidx.work.Data.Builder()
                    .putString("fcm_token", token)
                    .build()
                val request = androidx.work.OneTimeWorkRequestBuilder<com.company.cavitrack.data.worker.TokenSyncWorker>()
                    .setInputData(data)
                    .setConstraints(
                        androidx.work.Constraints.Builder()
                            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                            .build()
                    )
                    .setBackoffCriteria(
                        androidx.work.BackoffPolicy.EXPONENTIAL,
                        androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                        java.util.concurrent.TimeUnit.MILLISECONDS
                    )
                    .build()

                workManager.enqueueUniqueWork("FCMTokenSync", androidx.work.ExistingWorkPolicy.REPLACE, request)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (com.company.cavitrack.BuildConfig.DEBUG) {
                Log.e("AuthRepository", "Failed to register push token", e)
            }
        }
    }

    override suspend fun clearPushToken() {
        try {
            val token = firebaseMessaging.getToken().await()
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null && token.isNotEmpty()) {
                firebaseFirestore.collection("users").document(uid)
                    .collection("fcmTokens").document(token)
                    .delete()
                    .await()
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (com.company.cavitrack.BuildConfig.DEBUG) {
                Log.e("AuthRepository", "Failed to clear push token", e)
            }
        }
    }

    override suspend fun clearUserPhotos() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        try {
            storageRepository.deleteUserPhotos(uid)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (com.company.cavitrack.BuildConfig.DEBUG) {
                Log.e("AuthRepository", "Failed to clear user photos", e)
            }
        }
    }

    override suspend fun deleteAccount(): DataResult<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return DataResult.Error("No authenticated user")
            user.delete().await()
            DataResult.Success(Unit)
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            DataResult.Error("Please sign out and sign back in, then try again.", code = 401)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to delete account. You may need to re-authenticate.")
        }
    }

    override suspend fun signIn(email: String, password: String): DataResult<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Sign in failed")
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): DataResult<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = firebaseAuth.currentUser
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user?.updateProfile(profileUpdates)?.await()
            user?.sendEmailVerification()?.await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Sign up failed")
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUserUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    override fun getCurrentUserName(): String? {
        return firebaseAuth.currentUser?.displayName
    }

    override fun isEmailVerified(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified == true
    }

    override suspend fun reloadUser(): DataResult<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.reload().await()
                DataResult.Success(Unit)
            } else {
                DataResult.Error("No authenticated user")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            DataResult.Error(e.message ?: "Failed to reload user")
        }
    }
}
