
package com.github.ebrooks2002.fisherfinder.ui.screens

import android.hardware.GeomagneticField
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ebrooks2002.fisherfinder.model.AssetData
import com.github.ebrooks2002.fisherfinder.network.SPOTApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import android.location.Location
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.github.ebrooks2002.fisherfinder.location.LocationFinder
import com.github.ebrooks2002.fisherfinder.location.RotationSensor
import com.github.ebrooks2002.fisherfinder.model.Message

sealed interface FisherFinderUiState {
    data class Success(val assetData: AssetData) : FisherFinderUiState
    object Error : FisherFinderUiState
    object Loading : FisherFinderUiState
}

/**
 * The view model for the Buoy Finder app.
 *
 * Tracks the state of the UI, launches coroutine to
 * asynchronously retrieve and hold user location and rotation data, and asset data.
 *
 * @author E. Brooks
 */

class BuoyFinderViewModel : ViewModel(){
    var buoyFinderUiState: FisherFinderUiState by mutableStateOf(FisherFinderUiState.Loading)
        private set
    var userLocation: Location? by mutableStateOf(null)
        private set
    var userRotation: Float? by mutableStateOf(null)
        private set
    var selectedAssetName by mutableStateOf<String?>(null)
        private set
    private var lastRequestTime: Long = 0
    private val locUpdateInterval = 3000L // user location updates every 3 seconds.
    val headingDirection: String
        get() {
            val rot = userRotation ?: return "No Magnetometer"
            val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
            val index = kotlin.math.round(rot / 45f).toInt() % 8
            val safeIndex = (index + 8) % 8
            return directions[safeIndex]
        }

    // Constructor to call getAssetData
    init {
        getAssetData()
    }

    /**
     * Collects the flow from getRotationUpdates and updates userRotation.
     *
     * @param context The application context to retrieve the SensorManager object.
     */
    fun startRotationTracking(context: android.content.Context) {
        val rotationClient = RotationSensor(context)
        viewModelScope.launch {
            rotationClient.getRotationUpdates().collect { rotation ->
                val loc = userLocation
                if (loc != null) {
                    val field = GeomagneticField(
                        loc.latitude.toFloat(),
                        loc.longitude.toFloat(),
                        loc.altitude.toFloat(),
                        System.currentTimeMillis()
                    )
                    val adjustedRotation = (rotation + field.declination +360f) % 360f
                    userRotation = adjustedRotation
                }
                else {
                    userRotation = rotation
                }
            }
        }
    }

    /**
     * Collects the flow from getLocationUpdates and updates userLocation.
     *
     * @param context The application context to retrieve the LocationServices object.
     */
    fun startLocationTracking(context: android.content.Context) {
        val locationClient = LocationFinder(context)
        viewModelScope.launch {
            // Update every 2 seconds (2000ms)
            locationClient.getLocationUpdates(locUpdateInterval).collect { location ->
                userLocation = location
            }
        }
    }

    /**
     * Launches a coroutine to asynchronously retrieve and hold asset data, while tracking UI State.
     */
    fun getAssetData() {
        val FIVE_MINUTES_MS = 5 * 60 * 1000
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRequestTime < FIVE_MINUTES_MS) { return }
        lastRequestTime = currentTime
        viewModelScope.launch {
            buoyFinderUiState = FisherFinderUiState.Loading
            buoyFinderUiState = try {
                val allMessages = mutableListOf<Message>()
                var listResult: AssetData? = null
                for (i in 0..0) {
                    val start = i * 50
                    val result = SPOTApi.retrofitService.getData(start = start)
                    if (listResult == null) {
                        listResult = result
                    }
                    val messages = result.feedMessageResponse?.messages?.list ?: emptyList()
                    allMessages.addAll(messages)
                    if (messages.size < 50) break
                }
                if (listResult != null) {
                    listResult.feedMessageResponse?.messages?.list = allMessages
                    Log.d("BuoyDebug", "Final combined count being sent to UI: ${allMessages.size}")
                    FisherFinderUiState.Success(listResult)
                }
                else {
                    FisherFinderUiState.Error
                }
            } catch (e: HttpException) {
                Log.e("BuoyViewModel", "Network request failed: ${e.code()} ${e.message()}", e)
                FisherFinderUiState.Error
            } catch (e: Exception) {
                Log.e("BuoyViewModel", "Error fetching message ${e.message}", e)
                FisherFinderUiState.Error
            }
        }
    }

    fun selectAsset(name: String) {
        selectedAssetName = name
    }

    /**
     * Processes raw AssetData and returns a NavigationState data object
     */
    fun processAssetData(assetData: AssetData): NavigationState {
        val messageList = assetData.feedMessageResponse?.messages?.list ?: emptyList()
        var assetSpeedDisplay = "0.00 km/h"
        val assetHistory = messageList
            .filter { it.messengerName == selectedAssetName }
            .sortedByDescending { it.parseDate()?.time ?: 0L }
        if (assetHistory.size >= 2) {
            val latest = assetHistory[0]
            val previous = assetHistory[1]
            val currentSpeed = getCurrentSpeed(latest, previous)
            if (currentSpeed > 0.0) {
                assetSpeedDisplay = "%.2f km/h".format(currentSpeed)
            }
        } else {
            assetSpeedDisplay = "Calculating..."
        }
        val latestMessagesPerAsset = messageList.distinctBy { it.messengerName }
        val uniqueAssets = messageList.mapNotNull { it.messengerName }.distinct().sorted()
        if (selectedAssetName == null && uniqueAssets.isNotEmpty()) {
            selectedAssetName = uniqueAssets.first()
        }
        val selectedMessage = messageList.find { it.messengerName == selectedAssetName }
        val time = selectedMessage?.parseDate()
        val now = System.currentTimeMillis()
        var myHeading: Float? by mutableStateOf(null)
        var bearingToBuoy = 0f
        val diffMinutes = if (time != null) {
            (now - time.time) / (1000 * 60)
        } else {
            Long.MAX_VALUE // If no date, treat as "very old"
        }
        val color = getFreshnessColor(diffMinutes)
        val displayName = selectedMessage?.messengerName?.substringAfterLast("_") ?: "Select an Asset"
        val position = selectedMessage?.let {"Lat: ${it.latitude},\nLong: ${it.longitude}"} ?: "Position unavailable"
        var temaToAsset = 0f
        var assetPosition = Location("Asset")

        var userToAsset = 0f
        var gpsInfo: AnnotatedString = buildAnnotatedString { append("Waiting for GPS...") }
        if (userLocation != null && selectedMessage != null) {
            assetPosition = Location("Asset").apply { latitude = selectedMessage.latitude; longitude = selectedMessage.longitude}
            val temaPortCoords = Location("Tema Harbour").apply { latitude = 5.63438; longitude = 0.01674 }
            userToAsset = userLocation!!.distanceTo(assetPosition) / 1000
            temaToAsset = temaPortCoords.distanceTo(assetPosition) / 1000
            bearingToBuoy = userLocation!!.bearingTo(assetPosition)
            if (userLocation!!.hasSpeed() && userLocation!!.speed > 0.5f) {
                myHeading = userLocation!!.bearing
            }
        }
            return NavigationState(
                allMessages = messageList,
                messages = latestMessagesPerAsset,
                selectedAssetName = selectedAssetName,
                displayName = displayName,
                position = position,
                gpsInfo = gpsInfo,
                uniqueAssets = uniqueAssets,
                movingHeading = myHeading,
                formattedDate = selectedMessage?.formattedDate ?: "Date not available",
                formattedTime = selectedMessage?.formattedTime ?: "Time not available",
                diffMinutes = diffMinutes.toString(),
                userRotation = userRotation,
                bearingToBuoy = bearingToBuoy,
                assetSpeedDisplay = assetSpeedDisplay,
                color = color,
                temaToAsset = temaToAsset,
                userLocation = userLocation,
                userToAsset = userToAsset,
                assetPosition = assetPosition
            )
        }
    }
