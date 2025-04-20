package com.businessprospector

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BusinessProspectorApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize WorkManager
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        )

        // Create notification channels
        createNotificationChannels()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel for communication sequence notifications
            val sequenceChannel = NotificationChannel(
                CHANNEL_COMMUNICATION_SEQUENCE,
                "Communication Sequences",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about ongoing communication sequences"
            }

            // Channel for response notifications
            val responseChannel = NotificationChannel(
                CHANNEL_RESPONSES,
                "Contact Responses",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about responses from contacts"
            }

            // Channel for analytics and reports
            val analyticsChannel = NotificationChannel(
                CHANNEL_ANALYTICS,
                "Analytics & Reports",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications about analytics and reports"
            }

            notificationManager.createNotificationChannels(listOf(
                sequenceChannel,
                responseChannel,
                analyticsChannel
            ))
        }
    }

    companion object {
        const val CHANNEL_COMMUNICATION_SEQUENCE = "communication_sequence"
        const val CHANNEL_RESPONSES = "responses"
        const val CHANNEL_ANALYTICS = "analytics"
    }
}