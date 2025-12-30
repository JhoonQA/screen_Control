package com.srproyecto.screencontrol.uiz.screens

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.srproyecto.screencontrol.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val buttonText: String,
    val imageRes: Int,
    val features: List<String> = emptyList(),
    val isPermissionPage: Boolean = false
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- ESTADOS DE PERMISOS ---
    var hasUsageStats by remember { mutableStateOf(checkUsageStats(context)) }
    var hasOverlay by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var isBatteryOptimized by remember { mutableStateOf(isIgnoringBattery(context)) }

    // Re-chequeo automático cuando el usuario vuelve de Ajustes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageStats = checkUsageStats(context)
                hasOverlay = Settings.canDrawOverlays(context)
                isBatteryOptimized = isIgnoringBattery(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pages = listOf(
        OnboardingPage(
            "¡Bienvenido!",
            "Toma el control de tu tiempo digital y mejora tu productividad.",
            "Continuar",
            R.drawable.inicio,
            features = listOf(
                "Control de descanso",
                "Enfoque en el trabajo",
                "Vuelve a tu propósito"
            )
        ),
        OnboardingPage(
            "Consejos de uso",
            "Configura límites diarios para tus apps y revisa tus estadísticas.",
            "Continuar",
            R.drawable.consejos,
            features = listOf("Límites por app", "Estadísticas reales", "Bloqueo inteligente")
        ),
        OnboardingPage(
            "Permisos Necesarios",
            "Para que ScreenControl funcione correctamente, activa estos accesos:",
            "Aceptar y Empezar",
            R.drawable.terminos,
            isPermissionPage = true
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val gradientColors = listOf(Color(0xFF106E6E), Color(0xFF40CCCC))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors, endY = 1200f))
    ) {
        // Título Logo
        Text(
            text = "ScreenControl",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
                .shadow(10.dp, RoundedCornerShape(12.dp))
                .background(Color(0xFF156262).copy(0.8f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = true
            ) { pos ->
                val page = pages[pos]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Imagen def
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(0.2f))
                    ) {
                        Image(
                            painter = painterResource(id = page.imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = page.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = page.description,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(0.9f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (page.isPermissionPage) {
                        // --- LISTA DE PERMISOS ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PermissionTile("Acceso a Uso", hasUsageStats) {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }
                            PermissionTile("Mostratr sobre otras Apps", hasOverlay) {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                )
                            }
                            PermissionTile("Ignorar Batería", isBatteryOptimized) {
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        }
                    } else if (page.features.isNotEmpty()) {
                        // Características pag_1...
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color.Black.copy(0.1f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            page.features.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = feature, color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Indicadores
            Row(modifier = Modifier.padding(vertical = 24.dp)) {
                repeat(pages.size) { i ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(
                                width = if (i == pagerState.currentPage) 24.dp else 8.dp,
                                height = 8.dp
                            )
                            .background(
                                if (i == pagerState.currentPage) Color.White else Color.White.copy(
                                    0.4f
                                ),
                                CircleShape
                            )
                    )
                }
            }

            // Botón de acción
            val isLastPage = pagerState.currentPage == pages.size - 1
            val canProceed = !isLastPage || (hasUsageStats && hasOverlay)

            Button(
                onClick = {
                    if (!isLastPage) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    else onFinished()
                },
                enabled = canProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF156262),
                    disabledContainerColor = Color.Gray.copy(0.5f)
                )
            ) {
                Text(
                    text = pages[pagerState.currentPage].buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp

                )
            }
        }
    }
}

@Composable
fun PermissionTile(title: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (granted) Color.White.copy(0.2f) else Color.Black.copy(0.2f))
            .clickable(enabled = !granted) { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (granted) Color(0xFF4CAF50) else Color(0xFFFFC107)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = Color.White,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )
        if (!granted) {
            Text(
                text = "ACTIVAR",
                color = Color(0xFF40CCCC),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- FUNCIONES AUXILIARES ---
private fun checkUsageStats(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    } else {
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun isIgnoringBattery(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}