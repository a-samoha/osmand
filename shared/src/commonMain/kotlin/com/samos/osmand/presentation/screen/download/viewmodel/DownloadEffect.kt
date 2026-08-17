package com.samos.osmand.presentation.screen.download.viewmodel

import com.samos.osmand.presentation.mvi.MviEffect

sealed interface DownloadEffect : MviEffect {

    data class ShowToast(
        val type: ToastType
    ) : DownloadEffect
}

enum class ToastType {
    NoInternetConnection,
    ConnectionLost,
    OnMapDownloaded,
    OnMapDeleted;
}
