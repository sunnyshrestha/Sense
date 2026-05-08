package com.sunny.sense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var firebaseAuthManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        healthConnectManager = HealthConnectManager(this)
        firebaseAuthManager = FirebaseAuthManager(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var user by remember { mutableStateOf(firebaseAuthManager.getCurrentUser()) }
                    var hasPermissions by remember { mutableStateOf(false) }
                    var hasSkippedLogin by remember { mutableStateOf(false) }
                    var authError by remember { mutableStateOf<String?>(null) }
                    val coroutineScope = rememberCoroutineScope()

                    val signInLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)!!
                            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                            firebaseAuthManager.auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    user = firebaseAuthManager.getCurrentUser()
                                    authError = null
                                } else {
                                    authError = "Sign in failed"
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            authError = "Error during sign-in: ${e.message}"
                        }
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = healthConnectManager.requestPermissionsActivityContract()
                    ) { permissions ->
                        coroutineScope.launch {
                            hasPermissions = healthConnectManager.hasAllPermissions()
                        }
                    }

                    LaunchedEffect(user, hasSkippedLogin) {
                        if ((user != null || hasSkippedLogin) && healthConnectManager.isHealthConnectAvailable()) {
                            if (healthConnectManager.hasAllPermissions()) {
                                hasPermissions = true
                            } else {
                                permissionLauncher.launch(healthConnectManager.permissions)
                            }
                        }
                    }

                    if (user == null && !hasSkippedLogin) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(onClick = {
                                    val signInIntent = firebaseAuthManager.getGoogleSignInClient().signInIntent
                                    signInLauncher.launch(signInIntent)
                                }) {
                                    Text("Sign in with Google")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = { hasSkippedLogin = true }) {
                                    Text("Skip Sign In")
                                }
                                if (authError != null) {
                                    Text("Error: $authError", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    } else if (!healthConnectManager.isHealthConnectAvailable()) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Health Connect is not available on this device.")
                        }
                    } else if (hasPermissions) {
                        AppNavigation(healthConnectManager, firebaseAuthManager)
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
fun AppNavigation(healthConnectManager: HealthConnectManager, firebaseAuthManager: FirebaseAuthManager) {
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
                0 -> DashboardScreen(healthConnectManager, firebaseAuthManager)
                1 -> TrendsScreen(healthConnectManager)
            }
        }
    }
}
