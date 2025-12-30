package com.srproyecto.screencontrol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_history")
data class HistoryEntity(
    @PrimaryKey val date: String, // Formato "yyyy-MM-dd"
    val totalMillis: Long,
    val appCount: Int,
    val mostUsedApp: String,
    val dayName: String,
    val dateLabel: String //  "dd/MM"
)