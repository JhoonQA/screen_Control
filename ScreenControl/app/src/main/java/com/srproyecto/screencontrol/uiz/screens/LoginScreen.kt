
package com.srproyecto.screencontrol.uiz.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.srproyecto.screencontrol.utils.BiometricHelper

@Composable
fun LoginScreen(onAuthSuccess: () -> Unit) {
    val context = LocalActivity.current as FragmentActivity
    val biometricHelper = BiometricHelper(context)

    // Definición del color de la paleta
    val primaryColor = Color(0xFF77BBBB)
    val darkTextColor = Color(0xFF2D4444)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            color = primaryColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(60.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier
                    .padding(30.dp)
                    .fillMaxSize(),
                tint = primaryColor
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Acceso Protegido",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = darkTextColor,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Confirma tu identidad para acceder a tu panel de control y estadísticas de uso.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(56.dp))

        // Botón con color sólido y bordes redondeados estilizados
        Button(
            onClick = {
                biometricHelper.showBiometricPrompt(
                    onSuccess = { onAuthSuccess() },
                    onError = { /* Opcional: mostrar un Toast o Snackbar */ }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "DESBLOQUEAR",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}