package com.github.ebrooks2002.fisherfinder.data

import android.content.Context
import android.util.Log
import com.github.ebrooks2002.fisherfinder.model.AssetData
import com.github.ebrooks2002.fisherfinder.model.Message
import com.github.ebrooks2002.fisherfinder.network.SPOTApi
import com.github.ebrooks2002.fisherfinder.viewModel.FisherFinderUiState
import retrofit2.HttpException

class AssetDataFetcher {
    private val dataPersistenceManager = DataPersistenceManager()

    /**
     * Tries to fetch call .getDat(), saves result to disk, the loads the data to
     * the disk for offline use. If the .getDat() request fails,
     * we load data from disk and return cached.
     */
    suspend fun fetchData(context: Context, start: Int): AssetData? {
        try {
            val result = SPOTApi.retrofitService.getData(start=start)
            dataPersistenceManager.saveDataToDisk(context = context, data = result)
            return result
        } catch(e: Exception) {
            val cached = dataPersistenceManager.loadDataFromDisk(context = context)
            if (cached != null) {
                return cached
            } else {
                return null
            }
        }
    }
}