package com.srproyecto.screencontrol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Para guardar los límites establecidos por el usuario
@Entity(tableName = "app_limits")
data class AppLimit(
    @PrimaryKey val packageName: String,
    val appName: String,
    val limitMinutes: Int,
    val isActive: Boolean = true
)

// Para el historial de notificaciones internas
@Entity(tableName = "notifications")
data class NotificationEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_usage_stats")
data class DailyUsageEntity(
    @PrimaryKey val dateId: String, // Formato "yyyy-MM-dd"
    val totalMillis: Long,
    val mostUsedApp: String,
    val appCount: Int
)

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val icon: android.graphics.drawable.Drawable? = null,
    val category: String = "Otros"
)