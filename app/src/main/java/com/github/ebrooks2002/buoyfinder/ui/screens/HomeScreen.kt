package com.github.ebrooks2002.buoyfinder.ui.screens


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ebrooks2002.buoyfinder.model.AssetData
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

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
    var recentMessage = messages?.firstOrNull() // This must be either first or last message.

    val assetName = if (recentMessage != null) {
        "Asset Name: ${recentMessage.messengerName}"
    }
    else {"Asset Name not found."}

    val position = if (recentMessage != null) {
        "Most Recent Location: ${recentMessage.latitude}, ${recentMessage.longitude}"
    } else {"Buoy position not found."}

    val dateTime = if (recentMessage != null) {
        "Date/Time: ${recentMessage.dateTime}"
    } else {"Time not found."}

    Box(
        modifier = Modifier.fillMaxSize(),
        Alignment.Center
    ) {
        Column (
            modifier = Modifier.fillMaxWidth(0.9F)
                .fillMaxHeight(0.3F)
                .border(width = 2.dp, color = Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =  Arrangement.Center
        ) {
            Text(
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                text = assetName
            )
            Text(
                modifier = Modifier.padding(12.dp),
                fontSize = 24.sp,
                text = position
            )
            Text(
                modifier = Modifier.padding(12.dp),
                fontSize = 24.sp,
                text = dateTime
            )
        }
    }

}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(14.dp),
            fontSize = 45.sp,
            textAlign = TextAlign.Center,
            lineHeight = 50.sp,
            text = "Error retrieving data"
        )
    }
}
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Text(
            modifier = Modifier.padding(14.dp),
            fontSize = 45.sp,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp,
            text = "Loading"
        )
    }

}

@Preview(
    showBackground = true,       // 1. Adds a white background
    showSystemUi = true,         // 2. Adds status bar and nav bar
    device = "id:pixel_5"        // 3. Sets specific device dimensions (optional but helpful)
)
@Composable
fun HomeScreenPreview() {
    // 4. Wrap in your Theme and Surface to mimic the real app environment
    com.github.ebrooks2002.buoyfinder.ui.theme.BuoyFinderTheme {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            HomeScreen(
                // 5. Pass a fake state so you aren't stuck on "Loading" forever
                buoyFinderUiState = BuoyFinderUiState.Error
                // Or construct a dummy Success state if you want to see data
            )
        }
    }
}




