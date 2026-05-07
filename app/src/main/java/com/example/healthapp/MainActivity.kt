package com.example.healthapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var healthConnectManager: HealthConnectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        healthConnectManager = HealthConnectManager(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var hasPermissions by remember { mutableStateOf(false) }
                    val coroutineScope = rememberCoroutineScope()

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = healthConnectManager.requestPermissionsActivityContract()
                    ) { permissions ->
                        coroutineScope.launch {
                            hasPermissions = healthConnectManager.hasAllPermissions()
                        }
                    }

                    LaunchedEffect(Unit) {
                        if (healthConnectManager.hasAllPermissions()) {
                            hasPermissions = true
                        } else {
                            permissionLauncher.launch(healthConnectManager.permissions)
                        }
                    }

                    if (!healthConnectManager.isHealthConnectAvailable()) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Health Connect is not available on this device.")
                        }
                    } else if (hasPermissions) {
                        AppNavigation(healthConnectManager)
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Health Connect permissions are required to use this app.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(healthConnectManager: HealthConnectManager) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("🏠") },
                    label = { Text("Dashboard") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Text("📈") },
                    label = { Text("Trends") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> DashboardScreen(healthConnectManager)
                1 -> TrendsScreen(healthConnectManager)
            }
        }
    }
}
