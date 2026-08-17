package com.company.cavitrack.data.remote.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CaviTrackMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")
        // TODO: Send this token to backend to associate with the user
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received from: ${message.from}")

        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            // Trigger local notification here
        }
    }
}
