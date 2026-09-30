package com.example.healthmonitor.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class PredictRequest(
    val heartRate: Int,
    val systolicBp: Int,
    val diastolicBp: Int,
    val steps: Int,
    val sleepHours: Int
)

data class PredictionsData(
    @SerializedName("riskLevel") val riskLevel: String,
    @SerializedName("stressLevel") val stressLevel: String
)

data class PredictResponse(
    @SerializedName("status") val status: String,
    @SerializedName("predictions") val predictions: PredictionsData?
)

interface HealthApiService {
    @POST("predict")
    suspend fun predictHealth(@Body request: PredictRequest): Response<PredictResponse>
}

object NetworkClient {
    // Live Vercel ML Backend Production URL
    private const val BASE_URL = "https://backend-lime-xi-18.vercel.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    val apiService: HealthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HealthApiService::class.java)
    }
}
