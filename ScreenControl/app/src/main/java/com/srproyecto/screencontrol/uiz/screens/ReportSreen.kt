package com.srproyecto.screencontrol.uiz.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.srproyecto.screencontrol.uiz.MainViewModel
import com.srproyecto.screencontrol.utils.ExportUtils

@Composable
fun ReportScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedDays by remember { mutableIntStateOf(7) }
    val historyData by viewModel.historyState.collectAsState()

    // Cargamos los datos cuando cambia el rango
    LaunchedEffect(selectedDays) {
        viewModel.loadHistory(selectedDays)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Assessment,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Reportes y Exportación", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Exporta tus estadísticas a un archivo CSV compatible con Excel.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rango de exportación", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(7, 14, 30).forEach { days ->
                        FilterChip(
                            selected = selectedDays == days,
                            onClick = { selectedDays = days },
                            label = { Text("$days días") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Usamos historyData prev. cargada en el ViewModel
                ExportUtils.exportStatsToCSV(context, historyData)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(4.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Generar y compartir CSV")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "El archivo incluirá: Fecha, Tiempo total, Conteo de apps y la app más usada.",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}