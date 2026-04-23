/**
 * @author Ethan Brooks
 * Builds a Retrofit client, calls SPOT Server, gets XML Feed, parses data using simpleXML.
 */
package com.github.ebrooks2002.fisherfinder.network

import com.github.ebrooks2002.fisherfinder.model.AssetData
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Defines how Retrofit talks to the SPOT web server.
 */
interface SPOTApiService {
    /**
     * Returns an asset data object containing XML feed data.
     */
    @GET("message.json")
    suspend fun getData(
        @Query("count") count: Int = 50,
        @Query("start") start: Int = 0
    ): AssetData
}







