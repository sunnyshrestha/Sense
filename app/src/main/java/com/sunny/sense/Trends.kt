package com.sunny.sense

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

@Composable
fun TrendsScreen(healthConnectManager: HealthConnectManager) {
    var weekSteps by remember { mutableStateOf<List<Pair<Instant, Long>>>(emptyList()) }
    val modelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(Unit) {
        weekSteps = healthConnectManager.getStepsForLastWeek()

        if (weekSteps.isNotEmpty()) {
            withContext(Dispatchers.Default) {
                modelProducer.setEntries(weekSteps.mapIndexed { index, pair ->
                    FloatEntry(x = index.toFloat(), y = pair.second.toFloat())
                })
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Activity Trends", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        if (weekSteps.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Steps - Last 7 Days", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    Chart(
                        chart = columnChart(),
                        chartModelProducer = modelProducer,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    )
                }
            }
        } else {
            Text("Loading chart data...", modifier = Modifier.padding(16.dp))
        }
    }
}
