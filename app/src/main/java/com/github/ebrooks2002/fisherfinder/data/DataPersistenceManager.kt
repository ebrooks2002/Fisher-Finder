package com.github.ebrooks2002.fisherfinder.data

import android.content.Context
import android.util.Log
import com.github.ebrooks2002.fisherfinder.model.AssetData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Manages the saving and loading of asset data from phone's internal storage
 * uses Kotlinx Serialization for JSON persistence.
 */
class DataPersistenceManager(val context: Context) {

    // Create a JSON instance configured to be safe and clean
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false // Set to true only for debugging
    }

    // Change extension to .json to reflect the new format
    private val fileName = "asset_data_cache.json"
    private val MAX_MESSAGES = 500

    fun saveDataToDisk(data: AssetData) {
        try {
            // 1. Create a trimmed version of the data
            val trimmedData = trimOldMessages(data)

            // 2. Convert object to JSON string
            val jsonString = json.encodeToString(trimmedData)

            // 3. Write to internal storage
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                output.write(jsonString.toByteArray())
            }
            val count = trimmedData.response.feedMessageResponse.messages?.list?.size ?: 0
            Log.d("Persistence", "Saved $count messages to disk.")
        } catch (e: Exception) {
            Log.e("Persistence", "Error saving AssetData: ${e.message}")
        }
    }

    /**
     * Ensures AssetData stays under 500 messages by deleting oldest messages.
     */
    private fun trimOldMessages(data: AssetData): AssetData {
        // Accessing via the new structure: response -> feedMessageResponse -> messages -> list
        val messagesObj = data.response.feedMessageResponse.messages
        val originalList = messagesObj?.list ?: return data
        val trimmedList = originalList
            .filter { it.parseDate() != null }
            .sortedByDescending { it.parseDate()?.time ?: 0L }
            .take(MAX_MESSAGES)

        // Return a copy with the new list (assuming you used 'val' in data classes)
        // If you used 'var', you can assign directly, but 'copy' is safer.
        return data.copy(
            response = data.response.copy(
                feedMessageResponse = data.response.feedMessageResponse.copy(
                    messages = data.response.feedMessageResponse.messages.copy(
                        list = trimmedList))
            )
        )
    }

    fun loadDataFromDisk(): AssetData? {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val jsonString = file.readText()
                val data = json.decodeFromString<AssetData>(jsonString)
                Log.d("Persistence", "Successfully loaded AssetData from disk.")
                data
            } else {
                Log.d("Persistence", "No cache file found.")
                null
            }
        } catch (e: Exception) {
            Log.e("Persistence", "Error loading AssetData: ${e.message}")
            // If the file is corrupted (e.g. old XML format), delete it
            context.deleteFile(fileName)
            null
        }
    }

}