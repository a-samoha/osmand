package com.samos.osmand.presentation.screen.download.viewmodel

import androidx.lifecycle.viewModelScope
import com.samos.osmand.domain.manager.MapDownloadManager
import com.samos.osmand.domain.model.xml.RegionNode
import com.samos.osmand.domain.repository.MemoryRepository
import com.samos.osmand.presentation.mvi.MviEffect
import com.samos.osmand.presentation.mvi.MviViewModel
import com.samos.osmand.presentation.navigation.router.ComposeRouter
import kotlinx.coroutines.launch

class DownloadViewModel(
    memoryRepository: MemoryRepository,
    private val downloadManager: MapDownloadManager,
    private val router: ComposeRouter,
) : MviViewModel<DownloadState, DownloadIntent, MviEffect>(DownloadState()) {

    init {
        val freeSpace = memoryRepository.getFreeSpaceBytes()
        updateState {
            it.copy(
                usableMemory = formatBytes(freeSpace.first),
                usedMemoryProgress = freeSpace.second,
            )
        }

        // 💡 2. Subscribe to the central DownloadManager states flow to populate items reactively
        viewModelScope.launch {
            downloadManager.downloadStates.collect { statesMap ->
                // Map the Map<String, DownloadStatus> into your List<MapDownloadItemModel>
                val mappedItems = statesMap.map { (node, status) ->
                    MapDownloadItemModel(
                        id = node.name ?: "",
                        displayName = node.name?.replace("-", " ")?.replaceFirstChar { it.uppercase() } ?: "",
                        status = status,
                        // 💡 If it has sub-regions, it's a container (like Germany), not a downloadable file
                        isContainer = node.subRegions.isNotEmpty(),
                        childRegions = node.subRegions
                    )
                }.sortedBy { it.displayName }

                updateState { currentState ->
                    currentState.copy(
                        items = mappedItems,
                        isLoading = mappedItems.isEmpty() // Optional: true until XML finishes parsing
                    )
                }
            }
        }
    }

    override fun handleIntent(intent: DownloadIntent) = when (intent) {
        DownloadIntent.NavigateBack -> {
            router.navigateBack()
        }
        is DownloadIntent.OnDownloadMapClick -> {
            val node = findNodeById(intent.mapId)
            if (node != null) {
                startMapDownload(node, true)
            } else {
                println("Log MVI ERROR: Could not find RegionNode for download with ID: ${intent.mapId}")
            }
        }
        is DownloadIntent.OnDeleteMapClick -> {
            val node = findNodeById(intent.mapId)
            if (node != null) {
                deleteMap(node)
            } else {
                println("Log MVI ERROR: Could not find RegionNode for deletion with ID: ${intent.mapId}")
            }
        }
    }

    private fun findNodeById(mapId: String): RegionNode? {
        // Access the synchronous snapshot of the current states map from the manager
        val currentStatesMap = downloadManager.downloadStates.value

        // Search the keys (RegionNodes) to find the first one that matches the requested mapId
        return currentStatesMap.keys.firstOrNull { node ->
            node.name == mapId
        }
    }

    fun startMapDownload(node: RegionNode, forceOverwrite: Boolean = false) {
        downloadManager.enqueueDownload(node, forceOverwrite)
    }

    fun deleteMap(node: RegionNode,) {
        downloadManager.deleteMapFile(node)
    }

    private fun formatBytes(bytes: Long): String {
        val kilobyte = 1024.0
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024

        fun Double.roundToTwoDecimals(): Double {
            // Round, e.g.: 14.5467 to 14.55
            return kotlin.math.round(this * 100) / 100.0
        }

        return when {
            bytes >= gigabyte -> "${(bytes / gigabyte).roundToTwoDecimals()} Gb"
            bytes >= megabyte -> "${(bytes / megabyte).roundToTwoDecimals()} Mb"
            bytes >= kilobyte -> "${(bytes / kilobyte).roundToTwoDecimals()} Kb"
            else -> "$bytes Bytes"
        }
    }
}
