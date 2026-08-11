package com.samos.osmand.presentation.screen.download.viewmodel

import com.samos.osmand.presentation.mvi.MviIntent

sealed interface DownloadIntent : MviIntent {

    object NavigateBack : DownloadIntent
    object OnCategoryClick : DownloadIntent
}
