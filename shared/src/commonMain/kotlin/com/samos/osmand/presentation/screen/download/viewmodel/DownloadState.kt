package com.samos.osmand.presentation.screen.download.viewmodel

import com.samos.osmand.domain.model.DownloadStatus
import com.samos.osmand.presentation.mvi.MviState

data class DownloadState(
    val isLoading: Boolean = false,
    val usableMemory: String? = null,
    val usedMemoryProgress: Float = 0f,
    val items: List<MapDownloadItemModel> = emptyList(),
) : MviState

data class MapDownloadItemModel(
    val id: String,
    val fileName: String,
    val status: DownloadStatus
)
