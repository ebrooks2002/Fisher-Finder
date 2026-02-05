package com.github.ebrooks2002.fisherfinder.ui.screens
import android.location.Location
import androidx.compose.ui.text.AnnotatedString
import com.github.ebrooks2002.fisherfinder.model.Message

data class NavigationState(
    val allMessages: List<Message>,
    val messages : List<Message>,
    val selectedAssetName: String?,
    val displayName: String,
    val position: String,
    val gpsInfo: AnnotatedString,
    val uniqueAssets: List<String>,
    val formattedDate: String,
    val formattedTime: String,
    val diffMinutes: String,
    val movingHeading: Float?,
    val userRotation: Float?,
    val bearingToBuoy: Float,
    val assetSpeedDisplay: String,
    val color: String,
    val temaToAsset: Float,
    val userLocation: Location?,
    val userToAsset: Float,
    val assetPosition: Location
)