package com.github.ebrooks2002.fisherfinder.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AssetData(
    // The JSON starts with {"response": {...}}
    @SerialName("response")
    val response: FeedResponseWrapper
)
@Serializable
data class FeedResponseWrapper(
    // Level 2: Handles {"feedMessageResponse": ...}
    val feedMessageResponse: FeedMessageResponse
)

@Serializable
data class FeedMessageResponse(
    val count: Int = 0,
    val totalCount: Int = 0,
    val feed: Feed? = null,
    val messages: Messages? = null
)

@Serializable
data class Messages(
    // In your JSON, the array is under the key "message"
    @SerialName("message")
    var list: List<Message> = emptyList()
)

@Serializable
data class Message(
    val id: Long = 0,
    val messengerId: String = "",
    val messengerName: String = "",
    val unixTime: Long = 0,
    val messageType: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val dateTime: String = "",
    val batteryState: String = "",
    val altitude: Int = 0
) {
    fun parseDate(): java.util.Date? {
        if (dateTime.isBlank()) return null
        return try {
            // This format matches the JSON date: 2026-04-22T19:39:44+0000
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US).parse(dateTime)
        } catch (e: Exception) {
            null
        }
    }
    val formattedDate: String
        get() {
            val date = parseDate() ?: return "Date not available"
            val formatter = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
            formatter.timeZone = java.util.TimeZone.getTimeZone("Africa/Accra")
            return formatter.format(date)
        }

    val formattedTime: String
        get() {
            val date = parseDate() ?: return "Time not available"
            val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            formatter.timeZone = java.util.TimeZone.getTimeZone("Africa/Accra")
            return "${formatter.format(date)} GMT"
        }
}
    // Keep your parseDate(), formattedDate, and formattedTime functions here


@Serializable
data class Feed(
    val id: String = "",
    val name: String = "",
    val status: String = ""
)
