package com.samos.osmand.domain.manager

import com.samos.osmand.domain.model.DownloadStatus
import kotlinx.coroutines.flow.StateFlow

interface MapDownloadManager {
    val downloadStates: StateFlow<Map<String, DownloadStatus>>

    fun enqueueDownload(fileName: String, forceOverwrite: Boolean = false)

    fun deleteMapFile(fileName: String)
}
