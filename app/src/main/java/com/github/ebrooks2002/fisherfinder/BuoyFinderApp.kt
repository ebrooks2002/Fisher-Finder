/**
 *
 * Instantiates the view model and local context.
 * Calls the HomeScreen composable function.
 *
 * @Author E. Brooks
 */

package com.github.ebrooks2002.fisherfinder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.github.ebrooks2002.fisherfinder.viewModel.FisherFinderViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ebrooks2002.fisherfinder.model.AssetData
import com.github.ebrooks2002.fisherfinder.ui.HomeScreen
import com.github.ebrooks2002.fisherfinder.ui.theme.BuoyFinderTheme
import com.github.ebrooks2002.fisherfinder.viewModel.FisherFinderUiState

@Composable
fun BuoyFinderApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(android.graphics.Color.parseColor("#EFEDE8"))
    ) {
        val buoyFinderViewModel: FisherFinderViewModel = viewModel()
        val context = LocalContext.current

        HomeScreen(
            fisherFinderUiState = buoyFinderViewModel.fisherFinderUiState,
            onGetDataClicked = { buoyFinderViewModel.getAssetData(context) },
            userLocation = buoyFinderViewModel.userLocation,
            onStartLocationUpdates = { buoyFinderViewModel.startLocationTracking(context) },
            userRotation = buoyFinderViewModel.userRotation,
            userDirection = buoyFinderViewModel.headingDirection,
            onStartRotationUpdates = { buoyFinderViewModel.startRotationTracking(context) }
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "id:pixel_5"
)
@Composable
fun BuoyFinderAppPreview() {
    BuoyFinderTheme {
        HomeScreen(
            fisherFinderUiState = FisherFinderUiState.Success(
                AssetData()
            ),
            onGetDataClicked = {},
            userLocation = null,
            onStartLocationUpdates = {},
            userRotation = 0f,
            userDirection = "N",
            onStartRotationUpdates = {},
        )
    }
}