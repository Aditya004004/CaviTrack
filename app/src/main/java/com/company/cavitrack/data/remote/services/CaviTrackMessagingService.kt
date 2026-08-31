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

    // Removed unused injected fields.

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        val workManager = androidx.work.WorkManager.getInstance(this)
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
            
        workManager.enqueueUniqueWork("FCMTokenSync", ExistingWorkPolicy.REPLACE, request)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

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

        val intent = android.content.Intent(this, com.company.cavitrack.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
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








