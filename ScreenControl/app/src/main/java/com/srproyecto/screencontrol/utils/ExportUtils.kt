package com.srproyecto.screencontrol.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.srproyecto.screencontrol.data.DailyStat
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ExportUtils {
    fun exportStatsToCSV(context: Context, stats: List<DailyStat>) {
        val fileName = "Reporte_screenControl_${System.currentTimeMillis()}.csv"
        // Encabezados en minúsculas
        val csvHeader = "fecha,dia,tiempo_total_ms,tiempo_formateado,apps_usadas,app_mas_usada\n"

        try {
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)

            outputStream.write(csvHeader.toByteArray())

            stats.forEach { stat ->
                // Formatear tiempo a "12h:35m"
                val hours = stat.totalMillis / 3600000
                val minutes = (stat.totalMillis % 3600000) / 60000
                val formattedTime = "${hours}h:${minutes}m"

                // fila_minusC
                val row = "${stat.dateLabel}," +
                        "${stat.dayName.lowercase(Locale.getDefault())}," +
                        "${stat.totalMillis}," +
                        "$formattedTime," +
                        "${stat.appCount}," +
                        "${stat.mostUsedApp.lowercase(Locale.getDefault())}\n"

                outputStream.write(row.toByteArray())
            }
            outputStream.close()
            shareFile(context, file)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "reporte de uso")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "compartir reporte"))
    }
}