package com.example.healthmonitor.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HealthData(
    val heartRate: Int,
    val systolicBp: Int,
    val diastolicBp: Int,
    val steps: Int,
    val sleepHours: Int,
    val sleepMinutes: Int,
    val riskLevel: String,
    val stressLevel: String
)

interface DataRepository {
  val data: Flow<HealthData?>
  suspend fun updateData(heartRate: Int, systolicBp: Int, diastolicBp: Int, steps: Int, sleepHours: Int, sleepMinutes: Int)
}

class DefaultDataRepository(
  private val apiService: HealthApiService = NetworkClient.apiService
) : DataRepository {
  
  private val _data = MutableStateFlow<HealthData?>(null)
  override val data: Flow<HealthData?> = _data.asStateFlow()

  override suspend fun updateData(
      heartRate: Int,
      systolicBp: Int,
      diastolicBp: Int,
      steps: Int,
      sleepHours: Int,
      sleepMinutes: Int
  ) {
      var riskLevel = "Unknown"
      var stressLevel = "Unknown"

      try {
          val response = apiService.predictHealth(
              PredictRequest(
                  heartRate = heartRate,
                  systolicBp = systolicBp,
                  diastolicBp = diastolicBp,
                  steps = steps,
                  sleepHours = sleepHours
              )
          )

          if (response.isSuccessful && response.body()?.status == "success") {
              val predictions = response.body()?.predictions
              if (predictions != null) {
                  riskLevel = predictions.riskLevel
                  stressLevel = predictions.stressLevel
              }
          }
      } catch (e: Exception) {
          // Local fallback prediction rule if Flask ML backend is unreachable
          stressLevel = when {
              heartRate > 100 || systolicBp > 140 -> "High"
              heartRate > 85 || systolicBp > 130 -> "Moderate"
              else -> "Normal"
          }
          riskLevel = when {
              stressLevel == "High" || sleepHours < 5 || steps < 3000 -> "High"
              stressLevel == "Moderate" || sleepHours < 6 || steps < 5000 -> "Medium"
              else -> "Low"
          }
      }

      _data.value = HealthData(
          heartRate = heartRate,
          systolicBp = systolicBp,
          diastolicBp = diastolicBp,
          steps = steps,
          sleepHours = sleepHours,
          sleepMinutes = sleepMinutes,
          riskLevel = riskLevel,
          stressLevel = stressLevel
      )
  }
}
