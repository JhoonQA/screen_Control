package com.srproyecto.screencontrol.service

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.srproyecto.screencontrol.BlockActivity
import com.srproyecto.screencontrol.data.AppDatabase
import com.srproyecto.screencontrol.data.UsageRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.*

class UsageMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: UsageRepository
    private var monitoringJob: Job? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "usage_monitor_channel"
        // Reducido a 2-3 segundos para que el bloqueo sea "inmediato" al abrir la app
        private const val MONITORING_INTERVAL = 3000L
    }

    override fun onCreate() {
        super.onCreate()
        val dao = AppDatabase.getDatabase(this).dao()
        repository = UsageRepository(this, dao)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
    }
// -----------------------------------------------------------
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitoreo de Tiempo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoreando el uso de aplicaciones"
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ScreenControl Activo")
            .setContentText("Protegiendo tu tiempo digital...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }
// ----------------------------------------------------------------
    private suspend fun checkAppLimits() {
        // get limits d Room with flow
        repository.allLimits.collect { limits ->
            if (limits.isNotEmpty()) {
                val now = System.currentTimeMillis()
                val startOfDay = getStartOfDay()

                // 2. Obtener estadísticas de uso una sola vez
                val stats = repository.getUsageStats(startOfDay, now)

                // 3. Verificar cada límite
                limits.filter { it.isActive }.forEach { limit ->
                    val appUsage = stats.find { it.packageName == limit.packageName }
                    val usedMinutes = (appUsage?.usageTimeMillis ?: 0L) / 60000

                    if (usedMinutes >= limit.limitMinutes) {
                        // Notificar y bloquear si se supera el límite
                        showBlockNotification(limit.appName)

                        if (isAppInForeground(limit.packageName)) {
                            launchBlockActivity(limit.appName)
                        }
                    }
                }
            }
        }
    }


    private fun isAppInForeground(packageName: String): Boolean {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - 60000, // Último minuto
                time
            )

            stats?.maxByOrNull { it.lastTimeUsed }?.packageName == packageName
        } catch (e: Exception) {
            false
        }
    }

    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Obtener la app actual en primer plano PRIMERO
                    val currentForegroundApp = getForegroundApp()

                    // Solo procesar si el usuario no está en nuestra propia app de bloqueo
                    if (currentForegroundApp != packageName) {
                        checkAppLimits(currentForegroundApp)
                    }

                    delay(MONITORING_INTERVAL)
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(5000)
                }
            }
        }
    }
    private fun getForegroundApp(): String? {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            // Consultamos 5 ms
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 5000, time)
            stats?.maxByOrNull { it.lastTimeUsed }?.packageName
        } catch (e: Exception) {
            null
        }
    }

// --------------------------------------------------------
    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun showBlockNotification(appName: String) {
        try {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Crear canal para notificaciones de bloqueo si no existe
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val blockChannel = NotificationChannel(
                    "block_channel",
                    "Notificaciones de Bloqueo",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones cuando se superan límites"
                }
                notificationManager.createNotificationChannel(blockChannel)
            }

            val notification = NotificationCompat.Builder(this, "block_channel")
                .setContentTitle("¡Límite alcanzado!")
                .setContentText("Has superado el tiempo diario en $appName")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(appName.hashCode(), notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

// -------------------------------------------------------------------

    private val notifiedLimits = mutableSetOf<String>()

    private suspend fun checkAppLimits(currentApp: String?) {
        val limits = repository.allLimits.first()
        val now = System.currentTimeMillis()
        val startOfDay = getStartOfDay()
        val stats = repository.getUsageStats(startOfDay, now)

        limits.filter { it.isActive }.forEach { limit ->
            val appUsage = stats.find { it.packageName == limit.packageName }
            val usedMinutes = (appUsage?.usageTimeMillis ?: 0L) / 60000
            val percentage = (usedMinutes.toFloat() / limit.limitMinutes) * 100

            val limitKey = "${limit.packageName}_${limit.limitMinutes}"

            if (percentage >= 80 && percentage < 100 && !notifiedLimits.contains("${limitKey}_80")) {
                showSimpleNotification(
                    "¡Cuidado!",
                    "Has usado el 80% del tiempo permitido para ${limit.appName}"
                )
                notifiedLimits.add("${limitKey}_80")
            }

//------------------------------------------ Lógica de Bloqueo
            if (usedMinutes >= limit.limitMinutes) {
                if (currentApp == limit.packageName) {
                    launchBlockActivity(limit.appName)
                }
            }
        }
    }

    private fun showSimpleNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(this, "usage_monitor_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

// -------------------------------------------------------------------
private fun launchBlockActivity(appName: String) {
    try {
        val intent = Intent(this, BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            putExtra("APP_NAME", appName)
        }
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        // limpieza_override
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
        serviceScope.cancel()
    }
}