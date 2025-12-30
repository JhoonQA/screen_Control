package com.srproyecto.screencontrol

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class BlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })

        val appName = intent.getStringExtra("APP_NAME") ?: "Aplicación Bloqueada"


        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFFE53935),
                    onPrimary = Color.White,
                    surface = Color(0xFFFAFAFA),
                    onSurface = Color(0xFF212121),
                    error = Color(0xFFD32F2F),
                    errorContainer = Color(0xFFFFEBEE)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF5F5),
                                    Color(0xFFFFEBEE)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(120.dp)
                                .offset(x = (-40).dp, y = (-40).dp)
                                .background(
                                    color = Color(0xFFFFCDD2).copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(180.dp)
                                .offset(x = 60.dp, y = 60.dp)
                                .background(
                                    color = Color(0xFFEF9A9A).copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                        )
                    }

                    // Tarjeta principal
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .shadow(
                                elevation = 24.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = Color(0xFFE53935).copy(alpha = 0.3f),
                                spotColor = Color(0xFFC62828).copy(alpha = 0.4f)
                            ),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF212121)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Icono de alerta
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(
                                        color = Color(0xFFFFEBEE),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 3.dp,
                                        color = Color(0xFFFFCDD2),
                                        shape = CircleShape
                                    )
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.HourglassEmpty,
                                    contentDescription = "Tiempo agotado",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            // Título con énfasis
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TIEMPO AGOTADO",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = Color(0xFFD32F2F),
                                    textAlign = TextAlign.Center
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .height(4.dp)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFFFEBEE),
                                                    Color(0xFFE53935),
                                                    Color(0xFFFFEBEE)
                                                )
                                            ),
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }

                            // Nombre de la app bloqueada
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF5F5)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color(0xFFFFCDD2)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Block,
                                        contentDescription = null,
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = appName.uppercase(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFFB71C1C)
                                    )
                                }
                            }

                            // Mensaje principal
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Has alcanzado el límite diario establecido",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF424242)
                                )

                                // Consejos/reflexiones
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF5F5F5)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Lightbulb,
                                                contentDescription = "Consejo",
                                                tint = Color(0xFF1976D2),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Es momento de descansar la vista",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Nature,
                                                contentDescription = "Consejo",
                                                tint = Color(0xFF388E3C),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Aprovecha para hacer algo productivo",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.SelfImprovement,
                                                contentDescription = "Consejo",
                                                tint = Color(0xFF7B1FA2),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Tu bienestar digital es importante",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "La aplicación permanecerá bloqueada hasta mañana",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF757575)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Botón principal
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = Color(0xFFE53935).copy(alpha = 0.4f)
                                    ),
                                color = Color(0xFFE53935),
                                tonalElevation = 4.dp
                            ) {
                                TextButton(
                                    onClick = {
                                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                            addCategory(Intent.CATEGORY_HOME)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        startActivity(homeIntent)
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    colors = ButtonDefaults.textButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ExitToApp,
                                            contentDescription = "Salir",
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "CERRAR Y SALIR",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // Pie de página informativo
                            Text(
                                text = "ScreenControl · Bienestar Digital",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF9E9E9E),
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
            }
        }


    }
}