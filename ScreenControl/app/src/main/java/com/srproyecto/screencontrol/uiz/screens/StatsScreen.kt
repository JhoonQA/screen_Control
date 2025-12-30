package com.srproyecto.screencontrol.uiz.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srproyecto.screencontrol.data.DailyStat
import com.srproyecto.screencontrol.uiz.MainViewModel
import com.srproyecto.screencontrol.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(viewModel: MainViewModel) {
    var selectedRange by remember { mutableIntStateOf(7) }
    val historyData by viewModel.historyState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedStat by remember { mutableStateOf<DailyStat?>(null) }

    // Cálculos basados en el rango seleccionado
    val totalRangeTime = historyData.sumOf { it.totalMillis }
    val averageTimePerDay = if (historyData.isNotEmpty()) totalRangeTime / historyData.size else 0L
    val maxDayData = historyData.maxByOrNull { it.totalMillis }
    val averageAppCount = if (historyData.isNotEmpty()) {
        historyData.map { it.appCount }.average().toInt()
    } else 0

    // Carga de datos al cambiar el rango
    LaunchedEffect(selectedRange) {
        viewModel.loadHistory(selectedRange)
    }

    // Actualizar selección por defecto (HOY) cuando carguen los datos
    LaunchedEffect(historyData) {
        if (historyData.isNotEmpty() && selectedStat == null) {
            selectedStat = historyData.last()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Scroll principal de la pantalla
    ) {
        Text("Panel de Análisis", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de días
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(7, 14, 30).forEach { days ->
                FilterChip(
                    selected = selectedRange == days,
                    onClick = { selectedRange = days },
                    label = { Text("$days días") }
                )
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico de Barras
        EnhancedBarChart(
            data = historyData,
            selectedStat = selectedStat,
            onStatClick = { selectedStat = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de detalle del día seleccionado
        selectedStat?.let { stat ->
            DailyDetailSection(stat)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Resumen del Periodo", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.height(350.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                item {
                    MetricCard(
                        "Tiempo Total",
                        TimeUtils.formatMillisToHHmm(totalRangeTime),
                        Color(0xFF6200EE)
                    )
                }
                item {
                    MetricCard(
                        "Promedio Diario",
                        TimeUtils.formatMillisToHHmm(averageTimePerDay),
                        Color(0xFF03DAC5)
                    )
                }
                item {
                    MetricCard(
                        "Apps Usadas (Prom)",
                        averageAppCount.toString(),
                        Color(0xFFCF6679)
                    )
                }
                item { MetricCard("Más Usada", maxDayData?.mostUsedApp ?: "-", Color(0xFFFFB74D)) }
                item { MetricCard("Día Pico", maxDayData?.dayName ?: "-", Color(0xFF4CAF50)) }
                item { MetricCard("Racha", calculateStreak(historyData), Color(0xFF2196F3)) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Periodo: $selectedRange días | Datos de ${historyData.size} días registrados",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EnhancedBarChart(
    data: List<DailyStat>,
    selectedStat: DailyStat?,
    onStatClick: (DailyStat) -> Unit
) {

    val uniqueData = data.distinctBy { it.dateLabel }

    val maxVal = data.maxOfOrNull { it.totalMillis } ?: 1L
    val scrollState = rememberScrollState()

    // Auto-scroll al final
    LaunchedEffect(uniqueData) {
        if (uniqueData.isNotEmpty()) scrollState.animateScrollTo(scrollState.maxValue)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Uso Diario (Horas)", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                uniqueData.forEach { stat ->
                    val isSelected = selectedStat?.dateLabel == stat.dateLabel
                    val barHeight = (stat.totalMillis.toFloat() / maxVal).coerceAtLeast(0.05f)
                    val hours = (stat.totalMillis / 3600000f)
                    val isToday = stat.dateLabel == SimpleDateFormat(
                        "dd/MM",
                        Locale.getDefault()
                    ).format(Date())

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(40.dp)
                            .clickable { onStatClick(stat) }
                    ) {
                        Text(
                            text = String.format("%.1fh", hours),
                            fontSize = 9.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(barHeight * 0.75f)
                                .width(28.dp)
                                .background(
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stat.dayName,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
                        )
                        Text(
                            text = stat.dateLabel,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyDetailSection(stat: DailyStat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Detalle del ${stat.dayName} ${stat.dateLabel}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            val topApps = stat.appList.take(5)
            if (topApps.isEmpty()) {
                Text(
                    "No hay registros detallados para este día.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            topApps.forEach { app ->
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        app.appName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = TimeUtils.formatMillisToHHmm(app.usageTimeMillis),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(modifier = Modifier.alpha(0.1f))
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(16.dp, 4.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun calculateStreak(data: List<DailyStat>): String {
    if (data.isEmpty()) return "0 días"
    var streak = 0
    for (day in data.reversed()) {
        if (day.totalMillis > 0) streak++ else if (streak > 0) break
    }
    return "$streak días"
}