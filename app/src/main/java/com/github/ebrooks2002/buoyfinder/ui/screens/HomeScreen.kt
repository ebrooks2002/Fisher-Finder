package com.github.ebrooks2002.buoyfinder.ui.screens

import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ebrooks2002.buoyfinder.model.AssetData

@Composable
fun HomeScreen(
    buoyFinderUiState: BuoyFinderUiState = BuoyFinderUiState.Loading,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    when (buoyFinderUiState) {
        is BuoyFinderUiState.Loading -> LoadingScreen()
        is BuoyFinderUiState.Success -> ResultScreen(
            buoyFinderUiState.assetData)
        is BuoyFinderUiState.Error -> ErrorScreen()
    }
}


@Composable
fun ResultScreen(assetData: AssetData, modifier: Modifier = Modifier) {
    val messages = assetData.feedMessageResponse?.messages?.list
    val displayText = if (!messages.isNullOrEmpty()) {
        "Found ${messages.size} location(s):\n\n" +
                messages.joinToString(separator = "\n\n") { message ->
                    "Lat: ${message.latitude}\nLon: ${message.longitude}\nTime: ${message.dateTime}"
                }
    } else {
        "No buoy data found."
    }
    Text(
        modifier = Modifier.padding(14.dp),
        text = displayText
    )
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Text(
        modifier = Modifier.padding(14.dp),
        text = "Error"
    )
}
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Text(
        modifier = Modifier.padding(14.dp),
        text = "Loading"
    )
}
@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}




