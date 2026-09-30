package com.example.healthmonitor.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readLatestVitals(): HealthData? {
        if (!hasAllPermissions()) return null

        val now = Instant.now()
        val startOfDay = now.truncatedTo(ChronoUnit.DAYS)

        try {
            // Heart Rate
            val hrResponse = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(now.minus(1, ChronoUnit.HOURS), now)
                )
            )
            val hr = hrResponse.records.lastOrNull()?.samples?.lastOrNull()?.beatsPerMinute?.toInt() ?: 72

            // Steps
            val stepsResponse = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )
            val steps = stepsResponse.records.sumOf { it.count }.toInt()

            // BP
            val bpResponse = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = BloodPressureRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(now.minus(24, ChronoUnit.HOURS), now)
                )
            )
            val systolic = bpResponse.records.lastOrNull()?.systolic?.inMillimetersOfMercury?.toInt() ?: 120
            val diastolic = bpResponse.records.lastOrNull()?.diastolic?.inMillimetersOfMercury?.toInt() ?: 80

            // Sleep
            val sleepResponse = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(now.minus(24, ChronoUnit.HOURS), now)
                )
            )
            val latestSleep = sleepResponse.records.lastOrNull()
            val sleepDuration = latestSleep?.let { 
                java.time.Duration.between(it.startTime, it.endTime)
            }
            val sleepHours = sleepDuration?.toHours()?.toInt() ?: 0
            val sleepMinutes = sleepDuration?.toMinutesPart() ?: 0

            return HealthData(
                heartRate = hr,
                systolicBp = systolic,
                diastolicBp = diastolic,
                steps = steps,
                sleepHours = sleepHours,
                sleepMinutes = sleepMinutes,
                riskLevel = "Unknown",
                stressLevel = "Unknown"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
