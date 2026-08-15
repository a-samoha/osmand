package com.samos.osmand.presentation.screen.download.viewmodel

import androidx.lifecycle.viewModelScope
import com.samos.osmand.domain.manager.MapDownloadManager
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
                val mappedItems = statesMap.map { (fileName, status) ->
                    MapDownloadItemModel(
                        id = fileName,
                        fileName = fileName,
                        status = status
                    )
                }

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
            startMapDownload(intent.fileName, true)
        }
        is DownloadIntent.OnDeleteMapClick -> {
            deleteMap(intent.fileName)
        }
    }

    fun startMapDownload(fileName: String, forceOverwrite: Boolean = false) {
        downloadManager.enqueueDownload(fileName, forceOverwrite)
    }

    fun deleteMap(fileName: String) {
        downloadManager.deleteMapFile(fileName)
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
