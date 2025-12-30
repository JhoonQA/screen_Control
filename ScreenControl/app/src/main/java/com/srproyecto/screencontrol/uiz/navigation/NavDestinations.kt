package com.srproyecto.screencontrol.uiz.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Limits : Screen("limits", "Límites", Icons.Default.Timer)
    object Stats : Screen("stats", "Estadísticas", Icons.Default.BarChart)
    object Notifications : Screen("notifications", "Notificaciones", Icons.Default.Notifications)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
    object Reports : Screen("reports", "Reportes", Icons.Default.Description)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Limits,
    Screen.Stats,
    Screen.Notifications,
    Screen.Settings,
    Screen.Reports
)