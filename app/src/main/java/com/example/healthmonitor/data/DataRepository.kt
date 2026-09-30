package com.example.healthmonitor.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
  val data: Flow<HealthData>
}

class DefaultDataRepository(
  private val apiService: HealthApiService = NetworkClient.apiService
) : DataRepository {
  override val data: Flow<HealthData> = flow { 
      while (true) {
          val heartRate = (70..105).random()
          val systolicBp = (110..145).random()
          val diastolicBp = (75..95).random()
          val steps = (3000..9000).random()
          val sleepHours = (4..8).random()
          val sleepMinutes = (10..45).random()

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

          emit(HealthData(
              heartRate = heartRate,
              systolicBp = systolicBp,
              diastolicBp = diastolicBp,
              steps = steps,
              sleepHours = sleepHours,
              sleepMinutes = sleepMinutes,
              riskLevel = riskLevel,
              stressLevel = stressLevel
          ))

          delay(3000L)
      }
  }
}
