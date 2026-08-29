package com.company.cavitrack.data.repository

import com.company.cavitrack.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firebaseMessaging: FirebaseMessaging
) : AuthRepository {
    
    @Suppress("DEPRECATION")
    override suspend fun registerFcmToken() {
        try {
            val token = firebaseMessaging.getToken().await()
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null && token.isNotEmpty()) {
                firebaseFirestore.collection("users").document(uid)
                    .collection("fcmTokens").document(token)
                    .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis()))
                    .await()
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.e("AuthRepository", "Failed to register FCM token", e)
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun clearFcmToken() {
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
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.e("AuthRepository", "Failed to delete FCM token", e)
        }
    }

    override suspend fun clearStorage() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        try {
            val storageRef = firebaseStorage.reference.child("photos/")
            val listResult = storageRef.listAll().await()
            for (item in listResult.items) {
                item.delete().await()
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.e("AuthRepository", "Failed to delete storage photos", e)
        }
    }

    override suspend fun deleteAccount() {
        firebaseAuth.currentUser?.delete()?.await()
    }
}
