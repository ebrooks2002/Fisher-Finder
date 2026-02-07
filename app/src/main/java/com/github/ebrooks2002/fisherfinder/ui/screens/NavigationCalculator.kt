package com.github.ebrooks2002.fisherfinder.ui.screens

import android.location.Location
import com.github.ebrooks2002.fisherfinder.model.Message

fun getCurrentSpeed(latest: Message, previous: Message): Double {
    val time1 = latest.parseDate()?.time ?: 0L
    val time2 = previous.parseDate()?.time ?: 0L
    val timeDiffMs = time1 - time2
    if (timeDiffMs > 0) {
        val loc1 = Location("PointA").apply {
            latitude = latest.latitude
            longitude = latest.longitude
        }
        val loc2 = Location("PointB").apply {
            latitude = previous.latitude
            longitude = previous.longitude
        }
        val distanceMeters = loc1.distanceTo(loc2)
        val distanceKm = distanceMeters / 1000.0
        val timeHours = timeDiffMs / (1000.0 * 60.0 * 60.0)
        val speedKmh = distanceKm / timeHours
        val speedKnots = speedKmh * 0.539957
        return speedKnots
    }
    return 0.0
}

fun getFreshnessColor(diffMinutes: Long): String {
    return when {
        diffMinutes <= 15 -> "#00A86B"
        diffMinutes <= 30 -> "#ccae16"
        else -> "#FF0000"
    }
}


