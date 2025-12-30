package com.srproyecto.screencontrol.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import java.text.SimpleDateFormat
import java.util.*

class UsageRepository(private val context: Context, private val dao: ScreenTimeDao) {

    val allLimits = dao.getAllLimits()
    val allNotifications = dao.getAllNotifications()

    suspend fun deleteLimit(limit: AppLimit) {
        dao.deleteLimit(limit)
    }

    suspend fun saveLimit(limit: AppLimit) = dao.insertLimit(limit)
    // -------------------------------------------
    fun getHistoryStats(daysBack: Int): List<DailyStat> {
        val statsList = mutableListOf<DailyStat>()
        val nowMillis = System.currentTimeMillis()

        for (i in 0 until daysBack) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)

            // Inicio del día: 00:00:00.000
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            val end = if (i == 0) nowMillis else {
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val endTime = cal.timeInMillis
                endTime
            }

            // Pasamos un flag para saber si es el día actual
            val dailyApps = getUsageStatsOptimized(start, end, isToday = (i == 0))
            val totalMillis = dailyApps.sumOf { it.usageTimeMillis }.coerceAtMost(86400000L)

            statsList.add(
                DailyStat(
                    dateLabel = SimpleDateFormat("dd/MM", Locale.getDefault()).format(cal.time),
                    dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time),
                    totalMillis = totalMillis,
                    appCount = dailyApps.size,
                    mostUsedApp = dailyApps.maxByOrNull { it.usageTimeMillis }?.appName ?: "N/A"
                )
            )
            // -----------------------------------
            statsList.add(
                DailyStat(
                    dateLabel = SimpleDateFormat("dd/MM", Locale.getDefault()).format(cal.time),
                    dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time),
                    totalMillis = totalMillis,
                    appCount = dailyApps.size,
                    mostUsedApp = dailyApps.maxByOrNull { it.usageTimeMillis }?.appName ?: "N/A",
                    appList = dailyApps
                )
            )
        }
        return statsList.reversed()
    }

    fun getUsageStats(startTime: Long, endTime: Long): List<AppUsageInfo> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        if (stats.isNullOrEmpty()) return emptyList()

        val pm = context.packageManager

        return stats
            .filter { it.totalTimeInForeground > 0 && it.lastTimeUsed >= startTime && it.lastTimeUsed <= endTime }
            .groupBy { it.packageName }
            .map { (pkg, usageList) ->
                val bestStat = usageList.maxByOrNull { it.lastTimeUsed }
                val totalTime = bestStat?.totalTimeInForeground ?: 0L

                val appName = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                } catch (e: Exception) {
                    pkg
                }
                AppUsageInfo(pkg, appName, totalTime)
            }
            .sortedByDescending { it.usageTimeMillis }
    }

    private fun getUsageStatsOptimized(
        startTime: Long,
        endTime: Long,
        isToday: Boolean
    ): List<AppUsageInfo> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryEnd = if (isToday) endTime + 60000 else endTime
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, queryEnd)

        if (stats.isNullOrEmpty()) return emptyList()

        val pm = context.packageManager

        val filteredStats = stats.filter { it.totalTimeInForeground > 0 }
            .filter {
                if (isToday) {
                    it.lastTimeUsed >= startTime
                } else {
                    it.lastTimeUsed in startTime..endTime
                }
            }

        if (isToday && filteredStats.isEmpty()) {
            return stats.filter { it.totalTimeInForeground > 0 }
                .groupBy { it.packageName }
                .map { (pkg, list) ->
                    val appName = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                    } catch (e: Exception) {
                        pkg
                    }
                    AppUsageInfo(pkg, appName, list.maxOf { it.totalTimeInForeground })
                }
        }
        return filteredStats
            .groupBy { it.packageName }
            .map { (pkg, usageList) ->
                val totalTime = usageList.maxOf { it.totalTimeInForeground }
                val appName = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                } catch (e: Exception) {
                    pkg
                }

                AppUsageInfo(pkg, appName, totalTime)
            }
            .sortedByDescending { it.usageTimeMillis }
    }

    // --------------------------------logica_sincronizacion
    suspend fun getHistoryStatsWithCache(daysBack: Int): List<DailyStat> {
        val result = mutableListOf<DailyStat>()
        val todayLabel = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date())

        for (i in 0 until daysBack) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateLabel = SimpleDateFormat("dd/MM", Locale.getDefault()).format(cal.time)
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

            // 1. Intentar buscar en la base de datos (excepto si es HOY)
            val cachedEntry = if (dateLabel != todayLabel) {
                dao.getHistory(1).find { it.date == dateKey }
            } else null

            if (cachedEntry != null) {
                // Usar datos de Room
                result.add(
                    DailyStat(
                        dateLabel = cachedEntry.dateLabel,
                        dayName = cachedEntry.dayName,
                        totalMillis = cachedEntry.totalMillis,
                        appCount = cachedEntry.appCount,
                        mostUsedApp = cachedEntry.mostUsedApp,
                        appList = emptyList()
                    )
                )
            } else {
                // 2. Si no hay caché o es HOY
                val start = getStartOfDay(cal)
                val end = if (i == 0) System.currentTimeMillis() else getEndOfDay(cal)

                val apps = getUsageStatsOptimized(start, end, isToday = (i == 0))
                val total = apps.sumOf { it.usageTimeMillis }

                val stat = DailyStat(
                    dateLabel = dateLabel,
                    dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time),
                    totalMillis = total,
                    appCount = apps.size,
                    mostUsedApp = apps.maxByOrNull { it.usageTimeMillis }?.appName ?: "N/A",
                    appList = apps
                )

                // 3. Guardar en Room si  (i > 0)
                if (i > 0) {
                    dao.insertHistory(
                        HistoryEntity(
                            date = dateKey,
                            totalMillis = stat.totalMillis,
                            appCount = stat.appCount,
                            mostUsedApp = stat.mostUsedApp,
                            dayName = stat.dayName,
                            dateLabel = stat.dateLabel
                        )
                    )
                }
                result.add(stat)
            }
        }
        return result.reversed()
    }

    // Funciones auxiliares para limpiar el tiempo
    private fun getStartOfDay(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun getEndOfDay(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }


    // ----------------------------------------------------------------
    fun getAppCategory(packageName: String): String {
        return try {
            val ai = context.packageManager.getApplicationInfo(packageName, 0)
            when (ai.category) {
                ApplicationInfo.CATEGORY_GAME -> "Juegos"
                ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_AUDIO -> "Multimedia"
                ApplicationInfo.CATEGORY_SOCIAL -> "Redes Sociales"
                ApplicationInfo.CATEGORY_MAPS, //ApplicationInfo.CATEGORY_NAVIGATION -> "Viajes"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productividad"

                else -> if ((ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0) "Sistema" else "Otros"
            }
        } catch (e: Exception) {
            "Otros"
        }
    }

    suspend fun deleteAllNotifications() {
        dao.deleteAllNotifications()
    }
}
// --------------------------------------------------------------
data class DailyStat(
    val dateLabel: String,
    val dayName: String,
    val totalMillis: Long,
    val appCount: Int,
    val mostUsedApp: String,
    val appList: List<AppUsageInfo> = emptyList(),
    //val dateMillis: Long = 0L
) {
    constructor(
        dateLabel: String,
        dayName: String,
        totalMillis: Long,
        appCount: Int,
        mostUsedApp: String
    ) : this(dateLabel, dayName, totalMillis, appCount, mostUsedApp, emptyList())
}