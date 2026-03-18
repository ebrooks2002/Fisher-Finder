package com.github.ebrooks2002.fisherfinder

import com.github.ebrooks2002.fisherfinder.model.Message
import com.github.ebrooks2002.fisherfinder.model.getFreshnessColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone
import kotlin.math.round

/**
 * Exhaustive unit tests for core logic in Fisher Finder.
 */
class ExampleUnitTest {

    @Test
    fun testGetFreshnessColor() {
        // Edge cases for 15 minutes
        assertEquals("#00A86B", getFreshnessColor(0))
        assertEquals("#00A86B", getFreshnessColor(15))
        assertEquals("#ccae16", getFreshnessColor(16))

        // Edge cases for 30 minutes
        assertEquals("#ccae16", getFreshnessColor(30))
        assertEquals("#FF0000", getFreshnessColor(31))

        // Large values
        assertEquals("#FF0000", getFreshnessColor(100))
        assertEquals("#FF0000", getFreshnessColor(Long.MAX_VALUE))
    }

    @Test
    fun testMessageParseDate() {
        // Valid date format: yyyy-MM-dd'T'HH:mm:ssZ
        val validDateStr = "2023-10-27T10:00:00+0000"
        val message = Message(dateTime = validDateStr)
        val date = message.parseDate()
        assertNotNull("Date should not be null for valid format", date)
        
        // Check specific time
        val expectedTime = 1698400800000L // 2023-10-27T10:00:00Z
        assertEquals("Date time should match expected UTC time", expectedTime, date?.time)

        // Invalid date format
        val invalidMessage = Message(dateTime = "2023-10-27")
        assertNull("Date should be null for invalid format", invalidMessage.parseDate())

        // Empty date
        val emptyMessage = Message(dateTime = "")
        assertNull("Date should be null for empty string", emptyMessage.parseDate())
    }

    @Test
    fun testMessageFormatting() {
        // Use a fixed date to test formatting
        // 2023-10-27T10:00:00+0000 is 10:00 AM UTC
        // Africa/Accra is GMT (UTC+0), so it should be 10:00
        val message = Message(dateTime = "2023-10-27T10:00:00+0000")
        
        // The formattedDate uses MM/dd/yyyy
        assertEquals("10/27/2023", message.formattedDate)
        
        // The formattedTime uses HH:mm and appends " GMT"
        assertEquals("10:00 GMT", message.formattedTime)
    }

    @Test
    fun testHeadingDirectionLogic() {
        // Logic copied from FisherFinderViewModel to test in isolation
        fun getHeadingDirection(rot: Float?): String {
            val r = rot ?: return "No Magnetometer"
            val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
            val index = round(r / 45f).toInt() % 8
            val safeIndex = (index + 8) % 8
            return directions[safeIndex]
        }

        // Test cardinal directions
        assertEquals("N", getHeadingDirection(0f))
        assertEquals("E", getHeadingDirection(90f))
        assertEquals("S", getHeadingDirection(180f))
        assertEquals("W", getHeadingDirection(270f))

        // Test ordinal directions
        assertEquals("NE", getHeadingDirection(45f))
        assertEquals("SE", getHeadingDirection(135f))
        assertEquals("SW", getHeadingDirection(225f))
        assertEquals("NW", getHeadingDirection(315f))

        // Test boundaries / rounding
        assertEquals("N", getHeadingDirection(22.4f))
        assertEquals("NE", getHeadingDirection(22.6f))
        assertEquals("N", getHeadingDirection(337.6f)) // (337.6/45) = 7.502 -> round to 8 -> index 8 % 8 = 0 -> N
        assertEquals("NW", getHeadingDirection(337.4f)) // (337.4/45) = 7.497 -> round to 7 -> index 7 -> NW

        // Test overflow / wrap around
        assertEquals("N", getHeadingDirection(360f))
        assertEquals("NE", getHeadingDirection(405f))

        // Test negative values
        assertEquals("NW", getHeadingDirection(-45f))
        assertEquals("W", getHeadingDirection(-90f))
        
        // Null case
        assertEquals("No Magnetometer", getHeadingDirection(null))
    }
}