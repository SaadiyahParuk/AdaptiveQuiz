package com.example.adaptivegamifiedlearningsystem.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class PredictRequest(val features: List<Int>)

data class PredictResponse(
    val result: String,
    @SerializedName("adaptive_action") val adaptiveAction: String
)

interface PredictApi {
    @POST("predict")
    suspend fun predict(@Body request: PredictRequest): PredictResponse
}

object Network {
    private const val BASE_URL = "https://adaptivegamifiedlearningsystem.onrender.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .callTimeout(150, TimeUnit.SECONDS)   // Render cold-start headroom
        .build()

    val api: PredictApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PredictApi::class.java)
}