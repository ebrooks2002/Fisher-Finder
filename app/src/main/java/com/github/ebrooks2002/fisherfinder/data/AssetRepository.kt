package com.github.ebrooks2002.fisherfinder.data

import android.content.Context
import com.github.ebrooks2002.fisherfinder.model.AssetData
import com.github.ebrooks2002.fisherfinder.model.Message
import com.github.ebrooks2002.fisherfinder.network.SPOTApi

class AssetRepository(dataPersistenceManager: DataPersistenceManager) {

    /**
     * Tries to fetch call .getData(), saves result to disk, the loads the data to
     * the disk for offline use. If the .getDat() request fails,
     * we load data from disk and return cached.
     */
    suspend fun fetchData(dataPersistenceManager: DataPersistenceManager,context: Context): AssetData? {
        var listResult: AssetData? = null
        val allMessages = mutableListOf<Message>()
        for (i in 0..2) {
            val start = i * 50
            try {
                val result = SPOTApi.retrofitService.getData(start = start)
                listResult = result
                val messages = result?.feedMessageResponse?.messages?.list ?: emptyList()
                allMessages.addAll(messages)
                dataPersistenceManager.saveDataToDisk(context = context, data = result)
                if (messages.size < 50) break
                return result
            } catch (e: Exception) {
                val cached = dataPersistenceManager.loadDataFromDisk(context = context)
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