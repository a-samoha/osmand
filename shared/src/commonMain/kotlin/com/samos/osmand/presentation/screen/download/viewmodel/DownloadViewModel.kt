package com.samos.osmand.presentation.screen.download.viewmodel

import androidx.lifecycle.viewModelScope
import com.samos.osmand.domain.repository.MapRepository
import com.samos.osmand.domain.repository.MemoryRepository
import com.samos.osmand.presentation.mvi.MviEffect
import com.samos.osmand.presentation.mvi.MviViewModel
import com.samos.osmand.presentation.navigation.router.ComposeRouter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DownloadViewModel(
    memoryRepository: MemoryRepository,
    private val mapRepository: MapRepository,
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

    }

    override fun handleIntent(intent: DownloadIntent) = when (intent) {
        DownloadIntent.OnCategoryClick -> {
            val job = mapRepository.downloadMapFile("Denmark_capital-region_europe_2.obf.zip")
                .onEach { result ->
                    println("Test result $result")
                }.launchIn(viewModelScope)
        }
        DownloadIntent.NavigateBack -> {}
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
