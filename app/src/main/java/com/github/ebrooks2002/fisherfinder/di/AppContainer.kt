package com.github.ebrooks2002.fisherfinder.di

import android.app.Application
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.github.ebrooks2002.fisherfinder.data.AssetRepository
import com.github.ebrooks2002.fisherfinder.data.DataPersistenceManager
import com.github.ebrooks2002.fisherfinder.location.LocationFinder
import com.github.ebrooks2002.fisherfinder.location.RotationSensor
import com.github.ebrooks2002.fisherfinder.network.SPOTApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

interface AppContainer{
    val assetDataRepository: AssetRepository
    val locationFinder: LocationFinder
    val rotationSensor: RotationSensor
}

class DefaultAppContainer(val context: Context): AppContainer {

    private  val FEED_ID = "0r0YXhJmCiRJpmmJiaAdr6Ez6VIhahnMu"
    private val BASE_URL =
        "https://api.findmespot.com/spot-main-web/consumer/rest-api/2.0/public/feed/$FEED_ID/"
    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Contains the full raw XML Response.
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .client(client)
        .build()
    val dataPersistenceManager = DataPersistenceManager(context)

    override val locationFinder = LocationFinder(context)
    override val rotationSensor = RotationSensor(context)

    override val assetDataRepository: AssetRepository by lazy {
        AssetRepository(retrofitService = retrofitService, dataPersistenceManager=dataPersistenceManager)
    }
    val retrofitService: SPOTApiService by lazy {
        retrofit.create(SPOTApiService::class.java)
    }

}

