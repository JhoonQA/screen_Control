package com.srproyecto.screencontrol

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ScreenControlApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "usage_monitor_channel",
                "Monitoreo de Uso",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones de límites de tiempo"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}