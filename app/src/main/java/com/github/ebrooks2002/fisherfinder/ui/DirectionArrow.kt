package com.github.ebrooks2002.fisherfinder.ui

import android.graphics.Color.parseColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Straight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun Arrow(
    rotation: Float?,
    heading: Float?,
    color: String,
    headerDisplay: String,
    targetBearing: Float? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = headerDisplay,
            fontWeight = Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (rotation != null) {
            Box(
                modifier = Modifier.size(100.dp), // Area for the compass
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    modifier = Modifier
                        .align(Alignment.TopCenter),
                    fontWeight = Bold,
                    fontSize = 18.sp,
                    color = Color.Red // Red is standard for North
                )

                Text(
                    text = "S",
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                    fontWeight = Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = "E",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 14.dp),
                    fontWeight = Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = "W",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start=13.dp),
                    fontWeight = Bold,
                    fontSize = 18.sp,
                )
                Icon(
                    imageVector = Icons.Outlined.Straight,
                    contentDescription = "Arrow",
                    modifier = Modifier
                        .size(80.dp)
                        .alpha(0.5f)
                        .graphicsLayer {
                            rotationZ = rotation
                        },
                    tint = Color(0xFF254ACF)
                )

                Icon(
                    imageVector = Icons.Outlined.Straight,
                    contentDescription = "Arrow",
                    modifier = Modifier
                        .size(80.dp)
                        .alpha(0.5f)
                        .graphicsLayer {
                            rotationZ = heading?: 0f
                        },
                    tint = if (heading == null) {
                        Color.Gray
                    } else {
                        Color(0xFFCF2825)
                    }
                )

                if (targetBearing != null) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                    ){
                        val visualRadius = (size.minDimension / 2.0f) - 4.dp.toPx()
                        // 2. Draw the Path
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.3f), // Light gray path
                            radius = visualRadius,
                            center = center,
                            style = Stroke(
                                width = 1.dp.toPx() // Makes it a ring instead of a solid disk
                            )
                        )
                        val radius = size.minDimension / 2.0f // Slightly inside the border
                        val angleInRad = Math.toRadians(targetBearing.toDouble())
                        val x = (center.x + radius * sin(angleInRad)).toFloat()
                        val y = (center.y - radius * cos(angleInRad)).toFloat()
                        drawCircle(
                            color = Color(parseColor(color)),
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Unable to load, likely due to missing magnetometer",
                textAlign = TextAlign.Center
            )
        }

    }
}



