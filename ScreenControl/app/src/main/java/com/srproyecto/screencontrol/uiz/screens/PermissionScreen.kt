package com.srproyecto.screencontrol.uiz.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srproyecto.screencontrol.utils.PermissionManager

@Composable
fun PermissionScreen(onAllPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    var usageGranted by remember { mutableStateOf(PermissionManager.hasUsageStatsPermission(context)) }
    var overlayGranted by remember { mutableStateOf(PermissionManager.hasOverlayPermission(context)) }

    // Re-verificar cuando el usuario regresa de Ajustes
    LaunchedEffect(Unit) {
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Advertencia",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }


        Text("Permisos Necesarios", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Para medir tu tiempo y aplicar límites, activa las siguientes opciones manualmente:",
            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        PermissionItem(
            title = "Acceso a Datos de Uso",
            isGranted = usageGranted,
            onAction = { PermissionManager.openUsageSettings(context) }
        )

        PermissionItem(
            title = "Mostrar sobre otras apps",
            isGranted = overlayGranted,
            onAction = { PermissionManager.openOverlaySettings(context) }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                // Actualizamos estados antes de validar
                usageGranted = PermissionManager.hasUsageStatsPermission(context)
                overlayGranted = PermissionManager.hasOverlayPermission(context)

                if (usageGranted) onAllPermissionsGranted()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verificar y Continuar")
        }
    }
}

@Composable
fun PermissionItem(title: String, isGranted: Boolean, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        if (!isGranted) {
            TextButton(onClick = onAction) { Text("Activar") }
        } else {
            Text("Listo", color = MaterialTheme.colorScheme.primary)
        }
    }
}