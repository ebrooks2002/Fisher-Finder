package com.github.ebrooks2002.fisherfinder.ui.screens

import android.content.Context
import android.util.Log

import com.github.ebrooks2002.fisherfinder.model.AssetData
import org.simpleframework.xml.core.Persister
import java.io.File

class DataPersistenceManager {
    private val serializer = Persister()
    private val fileName = "asset_data_cache.xml"
    private val MAX_MESSAGES = 500 // Approximately 40-50KB of XML data

    fun saveDataToDisk(context: Context, data: AssetData) {
        try {
            // 1. Create a trimmed version of the data
            val trimmedData = trimOldMessages(data)

            // 2. Save the trimmed version
            val file = File(context.filesDir, fileName)
            serializer.write(trimmedData, file)
            Log.d("Persistence", "Saved ${trimmedData.feedMessageResponse?.messages?.list?.size} messages to disk.")
        } catch (e: Exception) {
            Log.e("Persistence", "Error saving AssetData: ${e.message}")
        }
    }

    private fun trimOldMessages(data: AssetData): AssetData {
        val originalList = data.feedMessageResponse?.messages?.list ?: return data

        // Sort by date descending (Newest first) and take the top 250
        val trimmedList = originalList
            .filter { it.parseDate() != null } // Ensure we have a date to sort by
            .sortedByDescending { it.parseDate()?.time ?: 0L }
            .take(MAX_MESSAGES)
            .toMutableList()

        // Create a copy of the data structure with only the trimmed list
        // Note: SimpleXML usually requires the same object structure
        data.feedMessageResponse?.messages?.list = trimmedList
        return data
    }

    // loadAssetData stays the same as before...
    fun loadDataFromDisk(context: Context): AssetData?{
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val data = serializer.read(AssetData::class.java, file)
                Log.d("Persistence", "Successfully loaded AssetData from disk.")
                data
            } else {
                Log.d("Persistence", "No cache file found.")
                null
            }
        } catch (e: Exception) {
            Log.e("Persistence", "Error loading AssetData: ${e.message}")
            null
        }
    }
}