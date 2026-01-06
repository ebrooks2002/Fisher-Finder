package com.github.ebrooks2002.buoyfinder.ui.map

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.io.File
import java.io.FileOutputStream

@Composable
fun OfflineMap(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Initialize MapLibre
    DisposableEffect(Unit) {
        MapLibre.getInstance(context)
        onDispose { }
    }

    // 2. Prepare the files (Copy .mbtiles and style.json to internal storage)
    val styleUrl = remember(context) {
        // Copy the raw map data
        val mbtilesFile = copyAssetToFiles(context, "ghana_offline_3857.mbtiles")
        // Copy the style definition
        val jsonFile = copyAssetToFiles(context, "style.json")

        // Inject the absolute path of the mbtiles file into the JSON
        var jsonContent = jsonFile.readText()
        jsonContent = jsonContent.replace("{path_to_mbtiles}", mbtilesFile.absolutePath)
        jsonFile.writeText(jsonContent)

        // Return the path to the JSON file
        "file://${jsonFile.absolutePath}"
    }

    // 3. Create the MapView
    val mapView = remember {
        MapView(context)
    }

    // 4. Manage Lifecycle (Required for MapView to work)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 5. Render the Map
    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
        update = { mv ->
            mv.getMapAsync { map ->
                // Load the style from the local JSON file
                map.setStyle(Style.Builder().fromUri(styleUrl))

                // Optional: Set a default camera position (Center of Ghana)
                // so the map doesn't open looking at the ocean at (0,0)
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(7.9465, -1.0232)) // Approx center of Ghana
                    .zoom(6.0)
                    .build()
            }
        }
    )
}

// Helper to copy file from assets to internal storage
private fun copyAssetToFiles(context: Context, fileName: String): File {
    val file = File(context.filesDir, fileName)
    // Only copy if it doesn't exist (to save startup time)
    if (!file.exists()) {
        try {
            context.assets.open(fileName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return file
}
