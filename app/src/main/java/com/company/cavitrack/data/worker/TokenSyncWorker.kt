package com.company.cavitrack.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class TokenSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val token = inputData.getString("fcm_token") ?: return Result.failure()
        
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                firestore.collection("users").document(user.uid)
                    .collection("fcmTokens").document(token)
                    .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis()))
                    .await()
                Result.success()
            } else {
                if (runAttemptCount > 3) {
                    Result.failure()
                } else {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            if (runAttemptCount > 3) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }
}
