package com.samos.osmand.data.source

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.Streaming
import io.ktor.client.statement.HttpStatement

interface OsmandApi {

    @Streaming // 💡 Important: it says NOT to load the entire file into RAM at once.
    @GET("download")
    suspend fun downloadMap(
        @Query("standard") standard: String,
        @Query("file") fileName: String,     // e.g.: "Denmark_capital-region_europe_2.obf.zip"
    ): HttpStatement // 💡 Return an HttpStatement for streaming reads.
}
