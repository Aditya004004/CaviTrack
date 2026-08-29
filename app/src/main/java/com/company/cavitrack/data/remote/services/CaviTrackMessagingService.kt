package com.company.cavitrack.data.remote.services






import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.work.ExistingWorkPolicy
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class CaviTrackMessagingService : FirebaseMessagingService() {

    @Inject
    @com.company.cavitrack.di.ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firebaseFirestore: FirebaseFirestore

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        applicationScope.launch {
            kotlinx.coroutines.withTimeoutOrNull(5.seconds) {
                try {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        firebaseFirestore.collection("users").document(user.uid)
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

    @Inject
    lateinit var syncScheduler: com.company.cavitrack.util.SyncScheduler

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Handle data payload for background sync triggers
        if (message.data.isNotEmpty()) {
            // Trigger a sync if requested
            if (message.data["action"] == "SYNC") {
                syncScheduler.scheduleOneTimeSync(ExistingWorkPolicy.REPLACE)
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

    override fun onCreate() {
        super.onCreate()
        val channelId = "cavi_track_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            "CaviTrack Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "cavi_track_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = android.content.Intent(this, com.company.cavitrack.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.company.cavitrack.R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId.incrementAndGet(), notificationBuilder.build())
    }
}








