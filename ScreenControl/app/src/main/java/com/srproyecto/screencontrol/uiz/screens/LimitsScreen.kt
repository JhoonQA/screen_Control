package com.srproyecto.screencontrol.uiz.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srproyecto.screencontrol.data.AppLimit
import com.srproyecto.screencontrol.data.AppUsageInfo
import com.srproyecto.screencontrol.uiz.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val savedLimits by viewModel.allLimits.collectAsState(initial = emptyList())
    val installedApps by viewModel.installedApps.collectAsState()

    var showAppPicker by remember { mutableStateOf(false) }
    var selectedAppForLimit by remember { mutableStateOf<AppUsageInfo?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Cargar apps al entrar
    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context.packageManager)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Límites de Uso",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAppPicker = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Añadir límite",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Estadísticas rápidas
            if (savedLimits.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "${savedLimits.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "límites activos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Límites",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Lista de límites
            if (savedLimits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Sin límites",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No hay límites configurados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Presiona + para establecer tu primer límite",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(savedLimits) { limit ->
                        LimitItem(limit = limit, onDelete = { viewModel.deleteLimit(it) })
                    }
                }
            }
        }

        // --------------------------- Diálogo de selección de apps
        if (showAppPicker) {
            AppSelectionDialog(
                showDialog = showAppPicker,
                installedApps = installedApps,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onAppSelected = { app ->
                    selectedAppForLimit = app
                    showAppPicker = false
                },
                onDismiss = { showAppPicker = false; searchQuery = "" }
            )
        }

        // ---------------------------- Diálogo para definir tiempo
        selectedAppForLimit?.let { app ->
            TimeSettingDialog(
                appName = app.appName,
                onDismiss = { selectedAppForLimit = null },
                onConfirm = { minutes ->
                    viewModel.saveLimit(AppLimit(app.packageName, app.appName, minutes))
                    selectedAppForLimit = null
                    searchQuery = ""
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSelectionDialog(
    showDialog: Boolean,
    installedApps: List<AppUsageInfo>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAppSelected: (AppUsageInfo) -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(24.dp),
            title = {
                Column {
                    Text(
                        "Seleccionar Aplicación",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Elige una app para limitar su uso",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text("Buscar aplicación...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )

                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (installedApps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Apps,
                                    contentDescription = "Sin apps",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "Cargando aplicaciones...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        val filtered = installedApps.filter {
                            it.appName.contains(searchQuery, ignoreCase = true) ||
                                    it.packageName.contains(searchQuery, ignoreCase = true)
                        }

                        if (filtered.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No se encontraron aplicaciones",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                items(filtered) { app ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { onAppSelected(app) },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(1.dp)
                                    ) {
                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    app.appName,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            supportingContent = {
                                                Text(
                                                    app.packageName,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            },
                                            colors = ListItemDefaults.colors(
                                                containerColor = Color.Transparent
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun TimeSettingDialog(
    appName: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var minutesInput by remember { mutableStateOf("60") }
    val minutes = minutesInput.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    "Establecer Límite",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "¿Cuántos minutos diarios permitir?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = minutesInput,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 3) {
                            minutesInput = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Minutos por día") },
                    supportingText = {
                        if (minutes > 0) {
                            val hours = minutes / 60
                            val remainingMinutes = minutes % 60
                            Text(
                                if (hours > 0)
                                    "≈ $hours h ${if (remainingMinutes > 0) "$remainingMinutes m" else ""}"
                                else "$minutes minutos",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )

                )

                // Presets rápidos
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(30, 60, 90, 120).forEach { preset ->
                        FilterChip(
                            selected = minutesInput == preset.toString(),
                            onClick = { minutesInput = preset.toString() },
                            label = { Text("${preset}m") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(minutes) },
                enabled = minutes > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Establecer Límite")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LimitItem(limit: AppLimit, onDelete: (AppLimit) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    limit.appName,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        limit.limitMinutes < 60 -> Color(0xFF4CAF50) // Verde
                                        limit.limitMinutes < 120 -> Color(0xFFFF9800) // Naranja
                                        else -> Color(0xFFF44336) // Rojo
                                    }
                                )
                        )
                        Text(
                            "${limit.limitMinutes} minutos diarios",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        limit.packageName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            trailingContent = {
                IconButton(
                    onClick = { onDelete(limit) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar límite",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}