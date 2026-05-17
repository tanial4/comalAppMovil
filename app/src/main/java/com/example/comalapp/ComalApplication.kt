package com.example.comalapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.comalapp.data.AppContainer

class ComalApplication : Application() {

    val container by lazy { AppContainer() }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ComalFirebaseMessagingService.CHANNEL_ID,
                "Órdenes",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notificaciones de pedidos ComalApp"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}