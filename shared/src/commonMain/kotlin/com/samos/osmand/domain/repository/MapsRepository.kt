package com.samos.osmand.domain.repository

import com.samos.osmand.domain.model.MapDownloadResult
import kotlinx.coroutines.flow.Flow

interface MapRepository {

    fun downloadMapFile(
        fileName: String,
        forceOverwrite: Boolean = false
    ): Flow<MapDownloadResult>
}
