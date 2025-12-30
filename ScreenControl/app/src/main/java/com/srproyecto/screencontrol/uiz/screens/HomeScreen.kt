package com.srproyecto.screencontrol.uiz.screens

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.srproyecto.screencontrol.data.AppUsageInfo
import com.srproyecto.screencontrol.uiz.MainViewModel
import com.srproyecto.screencontrol.utils.TimeUtils

@Composable
fun HomeScreen(viewModel: MainViewModel) {

    val usageStats by viewModel.filteredUsageList.collectAsState()

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val totalTime = usageStats.sumOf { it.usageTimeMillis }

    val context = LocalContext.current
    val packageManager = context.packageManager
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("Todas", "Sistema", "Juegos", "Redes Sociales", "Multimedia", "Productividad")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (selectedCategory == "Todas") "Tiempo Total Hoy" else "Uso en $selectedCategory",
                style = MaterialTheme.typography.titleMedium
            )

            val displayTime = if (selectedCategory == "Todas") {
                totalTime
            } else {
                usageStats.sumOf { it.usageTimeMillis }
            }

            Text(
                text = TimeUtils.formatMillisToHHmm(displayTime),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SELECTOR DE CATEGORÍAS
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Categoría: $selectedCategory")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                viewModel.setCategory(category)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- GRÁFICO DE TOP APPS ---
            if (usageStats.isNotEmpty()) {
                TopAppsChart(usageStats.take(5))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Desglose de Aplicaciones",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (usageStats.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxHeight(0.4f), contentAlignment = Alignment.Center) {
                    Text("No hay datos en esta categoría", color = Color.Gray)
                }
            }
        }

        items(usageStats) { app ->
            AppUsageRow(app = app, packageManager = packageManager)
        }
    }
}

@Composable
fun TopAppsChart(topApps: List<AppUsageInfo>) {
    val maxTime = topApps.maxOfOrNull { it.usageTimeMillis } ?: 1L

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Más utilizadas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            topApps.forEach { app ->
                val progress = app.usageTimeMillis.toFloat() / maxTime

                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        app.appName,
                        modifier = Modifier.width(80.dp),
                        maxLines = 1,
                        fontSize = 11.sp
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                    )
                                )
                        )
                    }

                    Text(
                        text = TimeUtils.formatMillisToHHmm(app.usageTimeMillis),
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AppUsageRow(app: AppUsageInfo, packageManager: PackageManager) {
    var appIcon by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(app.packageName) {
        try {
            val icon = packageManager.getApplicationIcon(app.packageName)
            appIcon = icon.toBitmap().asImageBitmap()
        } catch (e: Exception) { /* Fallback handled in UI */ }
    }

    ListItem(
        headlineContent = { Text(app.appName, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(app.packageName, fontSize = 11.sp, color = Color.Gray) },
        trailingContent = {
            Text(TimeUtils.formatMillisToHHmm(app.usageTimeMillis), fontWeight = FontWeight.Bold)
        },
        leadingContent = {
            if (appIcon != null) {
                Image(bitmap = appIcon!!, contentDescription = null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)))
            } else {
                Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Text(app.appName.take(1))
                }
            }
        }
    )
}