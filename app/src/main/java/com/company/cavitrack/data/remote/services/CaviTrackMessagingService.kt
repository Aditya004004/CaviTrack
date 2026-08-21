package com.company.cavitrack.data.remote.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.company.cavitrack.R

@AndroidEntryPoint
class CaviTrackMessagingService : FirebaseMessagingService() {

    
    

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")
        
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                kotlinx.coroutines.withTimeoutOrNull(5000) {
                    try {
                        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            db.collection("users").document(user.uid)
                                .collection("fcmTokens").document(token)
                                .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis()))
                                .await()
                            Log.d("FCM", "Token sent to Firestore for user: ${user.uid}")
                        }
                    } catch (e: Exception) {
                        Log.e("FCM", "Failed to send token", e)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received from: ${message.from}")

        // Handle data payload for background sync triggers
        if (message.data.isNotEmpty()) {
            Log.d("FCM", "Message Data payload: ${message.data}")
            // Trigger a sync if requested
            if (message.data["action"] == "SYNC") {
                val workManager = androidx.work.WorkManager.getInstance(applicationContext)
                val syncRequest = androidx.work.OneTimeWorkRequestBuilder<com.company.cavitrack.data.local.worker.SyncWorker>().build()
                workManager.enqueue(syncRequest)
            }
        }

        // Handle notification payload
        message.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            showNotification(it.title ?: "CaviTrack", it.body ?: "")
        }
    }

    companion object {
        private val notificationId = java.util.concurrent.atomic.AtomicInteger(0)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "cavitrack_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "CaviTrack Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(this, com.company.cavitrack.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId.incrementAndGet(), notificationBuilder.build())
    }
}


