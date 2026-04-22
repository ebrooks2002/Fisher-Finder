package com.github.ebrooks2002.fisherfinder.data

import android.content.Context
import com.github.ebrooks2002.fisherfinder.model.AssetData
import com.github.ebrooks2002.fisherfinder.model.Message
import com.github.ebrooks2002.fisherfinder.network.SPOTApiService

class AssetRepository(
    val retrofitService: SPOTApiService,
    val dataPersistenceManager: DataPersistenceManager) {

    /**
     * Tries to fetch call .getData(), saves result to disk, the loads the data to
     * the disk for offline use. If the .getDat() request fails,
     * we load data from disk and return cached.
     */
    suspend fun fetchData(): AssetData? {
        var listResult: AssetData? = null
        val allMessages = mutableListOf<Message>()
        for (i in 0..2) {
            val start = i * 50
            try {
                val result = retrofitService.getData(start = start)
                listResult = result
                val messages = result?.feedMessageResponse?.messages?.list ?: emptyList()
                allMessages.addAll(messages)
                dataPersistenceManager.saveDataToDisk(data = result)
                if (messages.size < 50) break
                return result
            } catch (e: Exception) {
                val cached = dataPersistenceManager.loadDataFromDisk()
                if (cached != null) {
                    return cached
                } else {
                    return null
                }
            }
            listResult?.feedMessageResponse?.messages?.list = allMessages
        }
        return listResult
    }

}