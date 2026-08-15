package com.samos.osmand.presentation.screen.download.viewmodel

import com.samos.osmand.presentation.mvi.MviIntent

sealed interface DownloadIntent : MviIntent {

    object NavigateBack : DownloadIntent

    data class OnDownloadMapClick(
        val fileName: String,
    ) : DownloadIntent

    data class OnDeleteMapClick(
        val fileName: String,
    ) : DownloadIntent
}
