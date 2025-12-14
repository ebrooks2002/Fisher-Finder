package com.github.ebrooks2002.buoyfinder.network

import com.github.ebrooks2002.buoyfinder.model.AssetData
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// URI of the SPOT XML Feed hard coded atm.

private const val FEED_ID = "0r0YXhJmCiRJpmmJiaAdr6Ez6VIhahnMu"

private const val BASE_URL =
    "https://api.findmespot.com/spot-main-web/consumer/rest-api/2.0/public/feed/$FEED_ID/"

// logger creation
private val logger = HttpLoggingInterceptor().apply {
    // LEVEL.BODY prints the full raw XML response
    level = HttpLoggingInterceptor.Level.BODY
}

private val client = OkHttpClient.Builder()
    .addInterceptor(logger)
    .build()

// Create a Retrofit object:
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(SimpleXmlConverterFactory.create())
    .client(client)
    .build()

interface SPOTApiService {
    @GET("message.xml")
    suspend fun getData(): AssetData
}

object SPOTApi {
    val retrofitService: SPOTApiService by lazy {
        retrofit.create(SPOTApiService::class.java)
    }
}