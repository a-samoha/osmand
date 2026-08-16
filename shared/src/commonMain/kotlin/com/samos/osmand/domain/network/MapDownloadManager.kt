package com.samos.osmand.domain.network

import com.samos.osmand.domain.model.DownloadStatus
import com.samos.osmand.domain.model.xml.RegionNode
import kotlinx.coroutines.flow.StateFlow

interface MapDownloadManager {
    val downloadStates: StateFlow<Map<RegionNode, DownloadStatus>>

    fun enqueueDownload(node: RegionNode, forceOverwrite: Boolean = false)

    fun deleteMapFile(node: RegionNode)

    fun cancelDownload(node: RegionNode)
}
