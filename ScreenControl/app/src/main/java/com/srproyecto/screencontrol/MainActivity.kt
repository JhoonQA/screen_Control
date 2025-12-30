package com.srproyecto.screencontrol

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import com.srproyecto.screencontrol.uiz.screens.LoginScreen
import com.srproyecto.screencontrol.ui.theme.ScreenControlTheme
import com.srproyecto.screencontrol.uiz.screens.OnboardingScreen
import com.srproyecto.screencontrol.uiz.screens.PermissionScreen
import com.srproyecto.screencontrol.utils.PermissionManager
import androidx.activity.viewModels
import com.srproyecto.screencontrol.service.UsageMonitorService
import com.srproyecto.screencontrol.uiz.MainViewModel
import com.srproyecto.screencontrol.uiz.MainAppScaffold

class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        enableEdgeToEdge()
        setContent {
            ScreenControlTheme {
                // referencia de biometría
                val isBiometricEnabled = remember {
                    sharedPref.getBoolean("biometric_enabled", true)
                }

                var showOnboarding by remember {
                    mutableStateOf(!sharedPref.getBoolean("onboarding_completed", false))
                }

                var isAuthenticated by remember {
                    mutableStateOf(!isBiometricEnabled)
                }

                when {
                    showOnboarding -> {
                        OnboardingScreen(onFinished = {
                            sharedPref.edit().putBoolean("onboarding_completed", true).apply()
                            showOnboarding = false
                        })
                    }

                    !isAuthenticated && isBiometricEnabled -> {
                        LoginScreen(onAuthSuccess = {
                            isAuthenticated = true
                        })
                    }

                    else -> {
                        var permissionsOk by remember {
                            mutableStateOf(PermissionManager.hasUsageStatsPermission(this))
                        }

                        if (!permissionsOk) {
                            PermissionScreen(onAllPermissionsGranted = {
                                permissionsOk = true
                            })
                        } else {
                            MainAppScaffold(viewModel = mainViewModel)

                            LaunchedEffect(Unit) {
                                val serviceIntent =
                                    Intent(this@MainActivity, UsageMonitorService::class.java)
                                startForegroundService(serviceIntent)
                            }
                        }
                    }
                }
            }
        }
    }
}