package com.example.healthapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant

@Composable
fun DashboardScreen(healthConnectManager: HealthConnectManager) {
    var todaySteps by remember { mutableLongStateOf(0L) }
    var todayCalories by remember { mutableDoubleStateOf(0.0) }
    var weekSteps by remember { mutableStateOf<List<Pair<Instant, Long>>>(emptyList()) }
    var insights by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        val (steps, calories) = healthConnectManager.getTodayStepsAndCalories()
        todaySteps = steps
        todayCalories = calories

        weekSteps = healthConnectManager.getStepsForLastWeek()
        insights = InsightsAnalyzer.generateInsights(todaySteps, todayCalories, weekSteps)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Today's Overview", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Steps", style = MaterialTheme.typography.titleMedium)
                        Text("$todaySteps", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calories", style = MaterialTheme.typography.titleMedium)
                        Text("${todayCalories.toInt()} kcal", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Actionable Insights", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        items(insights) { insight ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    text = insight,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
