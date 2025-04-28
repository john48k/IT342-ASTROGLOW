package edu.cit.astroglow.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // For physical device, use your computer's IP address
//     const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BASE_URL = "https://astroglowfirebase-d2411.uc.r.appspot.com/"
//    private const val BASE_URL = "http://192.168.60.69:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: AstroGlowApi = retrofit.create(AstroGlowApi::class.java)
} 