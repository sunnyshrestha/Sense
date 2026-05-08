package com.sunny.sense

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun getTodayStepsAndCalories(): Pair<Long, Double> {
        val now = Instant.now()
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
        val timeRangeFilter = TimeRangeFilter.between(startOfDay, now)

        val stepsRequest = AggregateRequest(
            metrics = setOf(StepsRecord.COUNT_TOTAL),
            timeRangeFilter = timeRangeFilter
        )

        val caloriesRequest = AggregateRequest(
            metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
            timeRangeFilter = timeRangeFilter
        )

        var steps = 0L
        var calories = 0.0

        try {
            val stepsResponse = healthConnectClient.aggregate(stepsRequest)
            steps = stepsResponse[StepsRecord.COUNT_TOTAL] ?: 0L

            val caloriesResponse = healthConnectClient.aggregate(caloriesRequest)
            calories = caloriesResponse[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(steps, calories)
    }

    suspend fun getStepsForLastWeek(): List<Pair<Instant, Long>> {
        val now = Instant.now()
        val startOfPastWeek = ZonedDateTime.now().minusDays(7).truncatedTo(ChronoUnit.DAYS).toInstant()

        val list = mutableListOf<Pair<Instant, Long>>()

        for (i in 0..6) {
            val startOfDay = ZonedDateTime.now().minusDays(i.toLong()).truncatedTo(ChronoUnit.DAYS).toInstant()
            val endOfDay = startOfDay.plus(1, ChronoUnit.DAYS)

            val request = AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )
            try {
                val response = healthConnectClient.aggregate(request)
                val steps = response[StepsRecord.COUNT_TOTAL] ?: 0L
                list.add(Pair(startOfDay, steps))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return list.sortedBy { it.first }
    }
}
