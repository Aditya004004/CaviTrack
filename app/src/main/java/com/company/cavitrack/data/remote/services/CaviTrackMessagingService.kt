package com.company.cavitrack.data.remote.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class CaviTrackMessagingService : FirebaseMessagingService() {

    @javax.inject.Inject
    @com.company.cavitrack.di.ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        applicationScope.launch {
            kotlinx.coroutines.withTimeoutOrNull(5.seconds) {
                try {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(user.uid)
                            .collection("fcmTokens").document(token)
                            .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis()))
                            .await()
                    }
                } catch (e: Exception) {
                    Log.e("FCM", "Failed to send token", e)
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Handle data payload for background sync triggers
        if (message.data.isNotEmpty()) {
            // Trigger a sync if requested
            if (message.data["action"] == "SYNC") {
                val workManager = androidx.work.WorkManager.getInstance(applicationContext)
                val constraints = androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
                val syncRequest = androidx.work.OneTimeWorkRequestBuilder<com.company.cavitrack.data.local.worker.SyncWorker>()
                    .setConstraints(constraints)
                    .build()
                workManager.enqueueUniqueWork("RemoteSync", androidx.work.ExistingWorkPolicy.REPLACE, syncRequest)
            }
        }

        // Handle notification payload
        message.notification?.let {
            showNotification(it.title ?: "CaviTrack", it.body ?: "")
        }
    }

    companion object {
        private val notificationId = java.util.concurrent.atomic.AtomicInteger(0)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "cavi_track_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "CaviTrack Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val intent = android.content.Intent(this, com.company.cavitrack.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.company.cavitrack.R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId.incrementAndGet(), notificationBuilder.build())
    }
}


