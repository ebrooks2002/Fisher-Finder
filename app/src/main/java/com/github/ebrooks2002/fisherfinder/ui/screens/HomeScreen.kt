/**
 * @author Ethan Brooks
 * The main screen of the Buoy Finder app.
 * Creates UI elements using composable functions to display:
 * List of tracked assets, refresh button, asset data, user distance and heading assets,
 * and error/loading messages.
 */

package com.github.ebrooks2002.fisherfinder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ebrooks2002.fisherfinder.model.AssetData
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ebrooks2002.fisherfinder.ui.map.OfflineMap
import com.github.ebrooks2002.fisherfinder.ui.theme.BuoyFinderTheme


/**
 * Asks for permission for location and compass, displays dropdown menu, asset and location data,
 * refresh button, and error/loading message.
 *
 * @param modifier Modifier to apply to this layout node.
 * @param buoyFinderUiState The current state of the UI.
 * @param onGetDataClicked A function to be called when the refresh button is clicked.
 * @param userLocation The current location of the user.
 * @param onStartLocationUpdates A function to start location updates.
 * @param userRotation The current rotation of the user.
 * @param userDirection The current direction of the user.
 * @param onStartRotationUpdates A function to start rotation updates.
 */
@Composable
fun HomeScreen(
    buoyFinderUiState: FisherFinderUiState,
    onGetDataClicked: () -> Unit,
    modifier: Modifier = Modifier, userLocation: Location?,
    onStartLocationUpdates: () -> Unit,
    userRotation: Float?,
    userDirection: String?,
    onStartRotationUpdates: () -> Unit,
    viewModel: BuoyFinderViewModel = viewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocationGranted || coarseLocationGranted) {
            onStartLocationUpdates()
            onStartRotationUpdates()
        }
    }
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BODY_SENSORS
            )
        )
    }

    var currentAssetData by remember { mutableStateOf<AssetData?>(null) }

    if (buoyFinderUiState is FisherFinderUiState.Success) {
        currentAssetData = buoyFinderUiState.assetData
    }

    if (currentAssetData != null) {
        ResultScreen(
            assetData = currentAssetData!!,
            viewModel = viewModel, // Pass the viewModel here
            onGetDataClicked = onGetDataClicked,
            loading = buoyFinderUiState is FisherFinderUiState.Loading,
            error = buoyFinderUiState is FisherFinderUiState.Error
        )
    } else {
        when (buoyFinderUiState) {
            is FisherFinderUiState.Loading -> ErrorLoadingMessage(message = "Loading")
            is FisherFinderUiState.Error -> ErrorLoadingMessage(message = "Error Fetching Data")
            else -> {}
        }
    }
}

@Composable
fun ResultScreen(
    assetData: AssetData,
    viewModel: BuoyFinderViewModel, // Pass the ViewModel in
    onGetDataClicked: () -> Unit,
    loading: Boolean,
    error: Boolean
) {
    // Everything is processed here in one line
    val navState = viewModel.processAssetData(assetData)

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DropDownMenu(
                availableAssets = navState.uniqueAssets,
                onAssetSelected = { viewModel.selectAsset(it) }, // Update selection in VM
                currentSelection = navState.displayName
            )
            Spacer(modifier = Modifier.width(8.dp))
            RefreshFeedButton(onGetDataClicked = onGetDataClicked)
        }

        if (loading) DisplayRefreshMessage(Color.Gray, "Refreshing data...")
        if (error) DisplayRefreshMessage(Color.Red, "Offline - Showing last known data")

        // Pass the pre-formatted strings directly
        DisplayAssetData(
            assetName = navState.displayName,
            position = navState.position,
            outputDateFormat = navState.formattedDate,
            outputTimeFormat = navState.formattedTime,
            gpsInfo = navState.gpsInfo,
            diffMinutes = navState.diffMinutes,
            userRotation = navState.userRotation,
            movingHeading = navState.movingHeading,
            bearingToBuoy = navState.bearingToBuoy,
            assetSpeed = navState.assetSpeedDisplay,
            color = navState.color,
            temaToAsset = navState.temaToAsset,
            userToAsset = navState.userToAsset,
            userPosition = navState.userLocation,
            assetPosition = navState.assetPosition
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
        )
        {
            OfflineMap(
                modifier = Modifier
                    .height(375.dp),
                assetData = assetData,
                viewmodel = viewModel
            )
        }
    }
}

@Composable
fun DisplayRefreshMessage(color: Color, message: String) {
    Text(
        text = message,
        fontSize = 12.sp,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
fun DisplayAssetData(
    assetName: String,
    movingHeading: Float?,
    bearingToBuoy: Float,
    userRotation: Float?,
    position: String,
    outputDateFormat: String,
    outputTimeFormat: String,
    gpsInfo: AnnotatedString? = null,
    userToAsset: Float,
    userPosition: Location?,
    color: String,
    assetSpeed: String? = null,
    diffMinutes: String? = null,
    temaToAsset: Float,
    assetPosition: Location
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = cardElevation(defaultElevation = 0.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color(android.graphics.Color.parseColor("#EFEDE8")))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // This Row forces both Columns to be the same height
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)

            ) {
                // Left Column: Tracker
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TrackerInfo(
                        assetName, position, outputDateFormat,
                        outputTimeFormat, color, assetSpeed,
                        diffMinutes, temaToAsset, assetPosition
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DeviceInfo(
                        userToAsset, userRotation, bearingToBuoy,
                        userPosition, movingHeading
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.97f)
                    .padding(top=5.dp)
                    .wrapContentHeight(),
                border = BorderStroke(1.dp, Color.DarkGray),
                colors = cardColors(containerColor = Color.White)
            ) {
                Column() {
                    Arrow(
                        rotation = userRotation,
                        heading = movingHeading,
                        headerDisplay = "Compass:",
                        targetBearing = bearingToBuoy,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun TrackerInfo(assetName: String,
                position: String,
                outputDateFormat: String,
                outputTimeFormat: String,
                color: String,
                assetSpeed: String? = null,
                diffMinutes: String? = null,
                temaToAsset: Float,
                assetPosition: Location
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .fillMaxHeight(),
        colors = cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.DarkGray)
    ) {
        Text(
            modifier = Modifier
                .padding(top=5.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            text = assetName
        )
        Text(
            modifier = Modifier
                .padding(start=4.dp),
            fontSize = 15.sp,
            text = "Lat: ${assetPosition.latitude.toString()}"
        )

        Text(
            modifier = Modifier
                .padding(start=4.dp),
            fontSize = 15.sp,
            text = "Lon: ${assetPosition.longitude.toString()}"
        )

        Text(
            modifier = Modifier
                .padding(start=4.dp),
            fontSize = 15.sp,
            text = "To Tema: %.1f km".format(temaToAsset)
        )
        Text(
            modifier = Modifier
                .padding(start=4.dp),
            fontSize = 15.sp, text = outputDateFormat
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .padding(start=4.dp),
                fontSize = 15.sp, text = outputTimeFormat
            )
            if (diffMinutes != null) {
                Text(
                    text = "(" + diffMinutes.toString() + " min. ago" + ")",
                    fontSize = 12.sp,
                    color = Color(android.graphics.Color.parseColor(color)) // move import to top.
                )
            }
        }
        Text(
            modifier = Modifier
                .padding(start=4.dp)
                .fillMaxWidth(),
            fontSize = 15.sp,
            text = assetSpeed ?: "none"
        )
    }
}

@Composable
fun DeviceInfo(
               userToAsset: Float,
               userRotation: Float?,
               bearingToBuoy: Float,
               userPosition: Location?,
               movingHeading: Float?
               ) {
    Card(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.97f),
        colors = cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.DarkGray)
    ) {

        Text(
            modifier = Modifier
                .padding(top=5.dp)
                .align(Alignment.CenterHorizontally),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            text = "My Device:"
        )

        val lat = userPosition?.latitude?.let { "%.4f".format(it) } ?: "N/A";
        val lon = userPosition?.longitude?.let { "%.4f".format(it) } ?: "N/A"

        Text(
            modifier = Modifier
                .padding(start=4.dp),
            fontSize = 15.sp,
            text = "Lat: $lat"
        )
        Text(
            modifier = Modifier
                .padding(start=4.dp),
            fontSize = 15.sp,
            text = "Long $lon"
        )
        Text(
            modifier = Modifier
                .padding(start=4.dp),
            fontSize = 15.sp,
            text = "To Asset: $userToAsset km"
        )
        Text(
            modifier = Modifier
                .padding(start=4.dp),
            fontSize = 15.sp,
            text = "Bearing: ${bearingToBuoy.toInt()}°"
        )
        Text(
            modifier = Modifier
                .padding(start=4.dp),
            fontSize = 15.sp,
            color = Color.Red,
            text = "Course: ${movingHeading?.toInt() ?: "N/A"}°"
        )
        Text(
            modifier = Modifier
                .padding(start=4.dp),
            fontSize = 15.sp,
            color = Color.Blue,
            text = "Heading: ${userRotation?.toInt() ?: "N/A"}°"
        )
    }
}

@Composable
fun RefreshFeedButton(onGetDataClicked: () -> Unit) {
    Button(
        onClick = onGetDataClicked,
        modifier = Modifier.padding(top = 40.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0XFF495583))
    )
    {
        Text(
            text = "Refresh",
            maxLines = 1
        )
    }
}

@Composable
fun DropDownMenu(
    availableAssets: List<String>,
    onAssetSelected: (String) -> Unit,
    currentSelection: String?,
    modifier: Modifier = Modifier,

) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.padding(
            top = 40.dp,
            start = 5.dp
        )
    ) { // Adjust this padding to move it up/down
        Button(
            onClick = { expanded = true },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0XFF495583))
        ) {
            Text(text = currentSelection ?: "Select Asset")
            Icon(
                imageVector = Icons.Filled.ArrowDropDown, contentDescription = null
            )
        }
        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            availableAssets.forEach { assetName ->
                DropdownMenuItem(text = { Text(text = assetName) }, onClick = {
                    onAssetSelected(assetName)
                    expanded = false })
            }
        }
    }
}

@Composable
fun ErrorLoadingMessage(modifier: Modifier = Modifier, message: String) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(14.dp),
            fontSize = 45.sp,
            textAlign = TextAlign.Center,
            lineHeight = 50.sp,
            text = message
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "id:pixel_5"
)
@Composable
fun HomeScreenPreview() {
    BuoyFinderTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            val viewModel: BuoyFinderViewModel = viewModel()
            val context = LocalContext.current
            HomeScreen(
                buoyFinderUiState = FisherFinderUiState.Success(AssetData()),
                onGetDataClicked = { viewModel.getAssetData() },
                userLocation = viewModel.userLocation,
                onStartLocationUpdates = {
                    viewModel.startLocationTracking(context)
                },
                userRotation = viewModel.userRotation,
                userDirection = viewModel.headingDirection,
                onStartRotationUpdates = {
                    viewModel.startRotationTracking(context)
                }
            )
        }
    }
}
