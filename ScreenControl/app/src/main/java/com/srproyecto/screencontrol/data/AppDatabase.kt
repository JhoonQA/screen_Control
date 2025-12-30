package com.srproyecto.screencontrol.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenTimeDao {
    // --- LÍMITES ---
    @Query("SELECT * FROM app_limits")
    fun getAllLimits(): Flow<List<AppLimit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(limit: AppLimit)

    @Delete
    suspend fun deleteLimit(limit: AppLimit)

    // --- NOTIFICACIONES ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntry>>

    @Insert
    suspend fun insertNotification(notification: NotificationEntry)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    // --- HISTORIAL ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("SELECT * FROM usage_history ORDER BY date DESC LIMIT :days")
    suspend fun getHistory(days: Int): List<HistoryEntity>

    @Query("DELETE FROM usage_history WHERE date < :thresholdDate")
    suspend fun deleteOldHistory(thresholdDate: String)
}

@Database(
    entities = [AppLimit::class, NotificationEntry::class, HistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ScreenTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "screentime_db"
                )
                    // WARNING
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}