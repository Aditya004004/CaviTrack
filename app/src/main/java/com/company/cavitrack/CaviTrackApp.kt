package com.company.cavitrack

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.company.cavitrack.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope

@HiltAndroidApp
class CaviTrackApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            com.google.firebase.appcheck.FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            com.google.firebase.appcheck.FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val syncChannel = android.app.NotificationChannel(
                "cavitrack_sync_channel",
                "Sync Notifications",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for background synchronization"
            }
            
            val alertsChannel = android.app.NotificationChannel(
                "cavi_track_alerts",
                "CaviTrack Alerts",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(syncChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
        
        // Clean up offline photos
        applicationScope.launch {
            try {
                val cacheDir = java.io.File(cacheDir, "offline_photos")
                if (cacheDir.exists() && cacheDir.isDirectory) {
                    val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                    cacheDir.listFiles()?.forEach { file ->
                        if (file.lastModified() < sevenDaysAgo) {
                            file.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.w("CaviTrackApp", "Offline photo cleanup failed", e)
                }
            }
        }
    }
}
