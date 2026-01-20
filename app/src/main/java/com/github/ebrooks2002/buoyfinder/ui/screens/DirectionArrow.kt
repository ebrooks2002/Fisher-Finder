package com.github.ebrooks2002.buoyfinder.ui.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.outlined.North
import androidx.compose.material.icons.outlined.Straight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.minDimension
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun Arrow(
    rotation: Float?,
    headerDisplay: String,
    targetBearing: Float? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .border(2.dp, Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) { // Removed the parenthesis error here
        Text(
            text = headerDisplay,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (rotation != null) {
            // We use a Box to stack the "N" reference on top of the Arrow space
            Box(
                modifier = Modifier.size(100.dp), // Area for the compass
                contentAlignment = Alignment.Center
            ) {
                // FIXED NORTH REFERENCE
                Text(
                    text = "N",
                    modifier = Modifier
                        .align(Alignment.TopCenter),
                    fontWeight = Bold,
                    fontSize = 18.sp,
                    color = Color.Red // Red is standard for North
                )

                Icon(
                    imageVector = Icons.Outlined.Straight,
                    contentDescription = "Arrow",
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            rotationZ = rotation
                        }
                )

                Log.d("Bearing to Buoy",  targetBearing.toString())
                if (targetBearing != null) {
                    // We subtract the device rotation because the compass "rotates"
                    // relative to your physical orientation


                    val relativeAngle = targetBearing - rotation

                    Canvas(modifier = Modifier.fillMaxSize()){
                        val radius = size.minDimension / 2.2f // Slightly inside the border
                        val angleInRad = Math.toRadians(relativeAngle.toDouble())

                        val x = (center.x + radius * kotlin.math.sin(angleInRad)).toFloat()
                        val y = (center.y - radius * kotlin.math.cos(angleInRad)).toFloat()

                        drawCircle(
                            color = Color.Blue, // Target color
                            radius = 4.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Unable to load, likely due to missing magnetometer",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

    }
}



