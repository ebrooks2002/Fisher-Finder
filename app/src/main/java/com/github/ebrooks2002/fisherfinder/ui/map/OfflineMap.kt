package com.github.ebrooks2002.fisherfinder.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.github.ebrooks2002.fisherfinder.model.AssetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import java.io.File
import java.io.FileOutputStream
import com.github.ebrooks2002.fisherfinder.viewModel.FisherFinderViewModel
import kotlinx.coroutines.delay
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.geojson.LineString

@Composable
fun OfflineMap(
    modifier: Modifier = Modifier,
    assetData: AssetData,
    viewmodel: FisherFinderViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val assetState = viewmodel.processAssetData(assetData)
    val selectedName = assetState.displayName

    var isLocationEnabled by remember { mutableStateOf(false) }

    val (featureCollection, lineFeatureCollection) = remember(assetState.allMessages) {
        val messagesByAsset = assetState.allMessages
            .filter { it.messengerName != null }
            .groupBy { it.messengerName }

        val features = mutableListOf<Feature>()
        val lineFeatures = mutableListOf<Feature>()

        messagesByAsset.forEach { (name, messages) ->
            val sorted = messages.sortedBy { it.parseDate()?.time ?: 0L }
            // Pin (Latest message only)
            sorted.lastOrNull()?.let { message ->
                val feature = Feature.fromGeometry(Point.fromLngLat(message.longitude, message.latitude))
                feature.addStringProperty("name", message.messengerName?.substringAfterLast("_") ?: "Unknown")
                val time = message.parseDate()
                val diffMinutes = if (time != null) (System.currentTimeMillis() - time.time) / (1000 * 60) else 999L

                feature.addStringProperty("diffMinutes", diffMinutes.toString())
                feature.addStringProperty("time", message.formattedTime ?: "")
                feature.addStringProperty("date", message.formattedDate ?: "")
                features.add(feature)
            }
            // Line (Full history)
            if (sorted.size >= 2) {
                val points = sorted.map { Point.fromLngLat(it.longitude, it.latitude) }
                lineFeatures.add(Feature.fromGeometry(LineString.fromLngLats(points)))
            }
        }
        FeatureCollection.fromFeatures(features) to FeatureCollection.fromFeatures(lineFeatures)
    }

    var styleUrl by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        MapLibre.getInstance(context)
        onDispose { }
    }

    LaunchedEffect(context) {
        withContext(Dispatchers.IO) {
            val mbtilesFile = copyAssetToFiles(context, "worldborder3.mbtiles")
            val jsonFile = File(context.filesDir, "melissa_styles.json")
            context.assets.open("melissa_styles.json").use { input ->
                jsonFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            var jsonContent = jsonFile.readText()
            val absoluteMbtilesPath = "mbtiles://${mbtilesFile.absolutePath}"
            jsonContent = jsonContent.replace("{path_to_mbtiles}", absoluteMbtilesPath)
            jsonFile.writeText(jsonContent)
            Log.d("MapDebug", "FINAL JSON CONTENT: $jsonContent")
            styleUrl = "file://${jsonFile.absolutePath}"
        }
    }

    if (styleUrl != null) {
        val currentStyleUrl = styleUrl!!
        val mapView = remember {
            MapView(context).apply {
                onCreate(null)
            }
        }
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

        LaunchedEffect(styleUrl) {
            while (!isLocationEnabled) {
                mapView.getMapAsync { map ->
                    val style = map.style
                    if (style != null && style.isFullyLoaded) {
                        isLocationEnabled = enableLocationComponent(context, map, style)
                    }
                }
                if (isLocationEnabled) break
                delay(2000) // Check every 2 seconds until successful
            }
        }

        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { map ->
                        map.setStyle(Style.Builder().fromUri(currentStyleUrl)) { style ->
                            val sourceId = "assets-source"
                            val lineSourceId = "lines-source"
                            style.addSource(GeoJsonSource(lineSourceId, lineFeatureCollection))
                            val source = GeoJsonSource(sourceId, featureCollection)
                            style.addSource(source)
                            isLocationEnabled = enableLocationComponent(context, map, style)

                            val lineLayer = LineLayer("lines-layer", lineSourceId)
                            lineLayer.setProperties(
                                PropertyFactory.lineColor("#66000000"), // Semi-transparent black
                                PropertyFactory.lineWidth(2f),
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                            )

                            style.addLayer(lineLayer)

                            val circleLayer = CircleLayer("buoys-layer", sourceId)
                            circleLayer.setProperties(
                                PropertyFactory.circleRadius(6f),

                                PropertyFactory.circleColor(
                                    Expression.interpolate(
                                        Expression.linear(),
                                        Expression.toNumber(Expression.get("diffMinutes")),
                                        Expression.stop(0, Expression.rgb(0, 168, 107)),    // 0 mins: Vibrant Green
                                        Expression.stop(45, Expression.rgb(255, 211, 44)),  // 12 hrs: Yellow
                                        Expression.stop(120, Expression.rgb(255, 0, 0))     // 24 hrs: Red
                                    )
                                ),

                                PropertyFactory.circleOpacity(
                                    Expression.interpolate(
                                        Expression.linear(),
                                        Expression.toNumber(Expression.get("diffMinutes")),
                                        Expression.stop(0, 1.0f),   // New: Solid
                                        Expression.stop(120, 0.1f) // Old: Ghostly
                                    )
                                ),
                                PropertyFactory.circleStrokeColor(
                                    Expression.switchCase(
                                        Expression.eq(Expression.get("name"), Expression.literal(selectedName)),
                                        Expression.literal("#000000"),
                                        Expression.literal("#FFFFFF")
                                    )
                                )
                            )
                            style.addLayer(circleLayer)
                        }

                        val uiSettings = map.uiSettings
                        uiSettings.isZoomGesturesEnabled = true
                        uiSettings.isScrollGesturesEnabled = true
                        uiSettings.isRotateGesturesEnabled = true
                        uiSettings.isTiltGesturesEnabled = false
                        uiSettings.isLogoEnabled = false
                        uiSettings.isAttributionEnabled = false

                        map.addOnMapClickListener { latLng ->
                            // 1. Convert click location to screen pixels
                            val point = map.projection.toScreenLocation(latLng)
                            val hitBox =
                                RectF(point.x - 15, point.y - 15, point.x + 15, point.y + 15)
                            val features = map.queryRenderedFeatures(hitBox, "buoys-layer")
                            if (features.isNotEmpty()) {
                                val feature = features[0]
                                val name = feature.getStringProperty("name")
                                val time = feature.getStringProperty("time")
                                val diffMinutes = feature.getStringProperty("diffMinutes")
                                val date = feature.getStringProperty("date")
                                val info = "$name\n$time ($diffMinutes min. ago) \n$date"
                                showBuoyPopup(context, mapView, point.x, point.y, info)
                                true
                            } else {
                                false // let the map handle the click normally
                            }
                        }

                        setOnTouchListener { view, _ ->
                            view.parent.requestDisallowInterceptTouchEvent(true)
                            false
                        }

                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(5.00928, -0.78918))
                            .zoom(6.0)
                            .build()
                    }
                }
            },
            modifier = modifier.fillMaxSize(),
            update = { _ ->
                // Refresh pins when new data comes in
                mapView.getMapAsync { map ->
                    val style = map.style
                    if (style != null && style.isFullyLoaded) {
                        val source = map.style?.getSourceAs<GeoJsonSource>("assets-source")
                        source?.setGeoJson(featureCollection)
                        val lineSource = style.getSourceAs<GeoJsonSource>("lines-source")
                        lineSource?.setGeoJson(lineFeatureCollection)
                        val layer = style.getLayerAs<CircleLayer>("buoys-layer")
                        layer?.setProperties(
                            PropertyFactory.circleStrokeWidth(
                                Expression.switchCase(
                                    Expression.eq(
                                        Expression.get("name"),
                                        Expression.literal(selectedName)
                                    ),
                                    Expression.literal(3f),
                                    Expression.literal(1f)
                                )
                            ),
                            // DYNAMIC STROKE COLOR
                            PropertyFactory.circleStrokeColor(
                                Expression.switchCase(
                                    Expression.eq(
                                        Expression.get("name"),
                                        Expression.literal(selectedName)
                                    ),
                                    Expression.literal("#000000"),
                                    Expression.literal("#FFFFFF")
                                )
                            ),
//                            PropertyFactory.circleRadius(
//                                Expression.interpolate(
//                                    Expression.linear(),
//                                    Expression.toNumber(Expression.get("diffMinutes")),
//                                    Expression.stop(0, 7f),      // 0 mins ago (Newest): Largest radius
//                                    Expression.stop(90, 2f)    // 120+ mins ago (Oldest): Smallest radius
//                                )
//                            ),
                            PropertyFactory.circleOpacity(
                                Expression.interpolate(
                                    Expression.linear(),
                                    Expression.toNumber(Expression.get("diffMinutes")),
                                    Expression.stop(0, 1.0f),   // New: Solid
                                    Expression.stop(90, 0.1f) // Old: Ghostly
                                )
                            ),
                            PropertyFactory.circleColor(
                                Expression.interpolate(
                                    Expression.linear(),
                                    Expression.toNumber(Expression.get("diffMinutes")),
                                    Expression.stop(0, Expression.rgb(0, 168, 107)),    // 0 mins: Vibrant Green
                                    Expression.stop(45, Expression.rgb(255, 211, 44)),  // 12 hrs: Yellow
                                    Expression.stop(90, Expression.rgb(255, 0, 0))     // 24 hrs: Red
                                )
                            ),
                        )
                    }
                }
            }
        )
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

// Helper to copy file from assets to internal storage
private fun copyAssetToFiles(context: Context, fileName: String): File {
    val file = File(context.filesDir, fileName)
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

@SuppressLint("MissingPermission")
private fun enableLocationComponent(context: Context, map: MapLibreMap, style: Style) : Boolean {
    // Check if permissions are granted (You should handle the request logic elsewhere in your UI)
    Log.d("LocationDebug", "Attempting to enable location comp")
    if (PermissionsManager.areLocationPermissionsGranted(context)) {
        val locationComponent = map.locationComponent
        // Activate with options
        val activationOptions = LocationComponentActivationOptions.builder(context, style)
            .useDefaultLocationEngine(true)
            .build()

        locationComponent.activateLocationComponent(activationOptions)
        // Enable to make it visible
        locationComponent.isLocationComponentEnabled = true

        // Set the render mode (COMPASS shows the blue dot with a direction bearing)
        locationComponent.renderMode = RenderMode.COMPASS

        locationComponent.cameraMode = CameraMode.TRACKING
        return true
    }
    return false
}

private fun showBuoyPopup(
    context: Context,
    parentView: View,
    anchorX: Float,
    anchorY: Float,
    content: String
) {
    val textView = TextView(context).apply {
        text = content
        textSize = 14f
        setPadding(32, 24, 32, 24)
        setTextColor(Color.WHITE)
        val shape = GradientDrawable().apply {
            setColor(Color.argb(225, 0, 0, 0))
            cornerRadius = 20f
        }
        background = shape
    }

    val popup = PopupWindow(
        textView,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        true
    )

    // NEW: Get the map's position on the actual phone screen
    val screenPos = IntArray(2)
    parentView.getLocationInWindow(screenPos)

    // Calculate absolute coordinates: Map Position + Pin Offset
    val finalX = (screenPos[0] + anchorX).toInt()
    val finalY = (screenPos[1] + anchorY).toInt()

    popup.showAtLocation(parentView, Gravity.NO_GRAVITY, finalX, finalY)
}

