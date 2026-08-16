package com.samos.osmand.presentation.screen.download.viewmodel

import com.samos.osmand.presentation.mvi.MviIntent

sealed interface DownloadIntent : MviIntent {

    object NavigateBack : DownloadIntent

    data class OnMapDownloadItemClick(
        val mapId: String,
    ) : DownloadIntent

    data class OnDownloadMapClick(
        val mapId: String,
    ) : DownloadIntent

    data class OnCancelDownloadClick(
        val mapId: String,
    ) : DownloadIntent

    data class OnDeleteMapClick(
        val mapId: String,
    ) : DownloadIntent

    data class OnConfirmDeletiuon(
        val mapId: String,
    ) : DownloadIntent

    data object OnCancelMapDeletion : DownloadIntent
}
