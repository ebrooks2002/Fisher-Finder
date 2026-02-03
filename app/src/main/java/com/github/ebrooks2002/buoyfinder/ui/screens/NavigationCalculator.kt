package com.github.ebrooks2002.buoyfinder.ui.screens

import android.location.Location
import com.github.ebrooks2002.buoyfinder.model.Message

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
        return speedKmh
    }
    return 0.0
}


