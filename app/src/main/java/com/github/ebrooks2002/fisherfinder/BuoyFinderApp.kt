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
import com.github.ebrooks2002.fisherfinder.data.AssetRepository
import com.github.ebrooks2002.fisherfinder.data.DataPersistenceManager
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
        val fisherFinderViewModel: FisherFinderViewModel = viewModel(factory = FisherFinderViewModel.Factory)
        val context = LocalContext.current
        val assetRepository = fisherFinderViewModel.assetRepository
        HomeScreen(
            assetRepository =  assetRepository ,
            fisherFinderUiState = fisherFinderViewModel.fisherFinderUiState,
            onGetDataClicked = { fisherFinderViewModel.getAssetData(context) },
            userLocation = fisherFinderViewModel.userLocation,
            onStartLocationUpdates = { fisherFinderViewModel.startLocationTracking(context) },
            userRotation = fisherFinderViewModel.userRotation,
            userDirection = fisherFinderViewModel.headingDirection,
            onStartRotationUpdates = { fisherFinderViewModel.startRotationTracking(context) }
        )
    }
}

