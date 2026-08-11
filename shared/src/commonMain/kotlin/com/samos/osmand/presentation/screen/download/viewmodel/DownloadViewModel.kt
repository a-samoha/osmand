package com.samos.osmand.presentation.screen.download.viewmodel

import com.samos.osmand.presentation.mvi.MviEffect
import com.samos.osmand.presentation.mvi.MviViewModel
import com.samos.osmand.presentation.navigation.router.ComposeRouter

class DownloadViewModel(
    private val router: ComposeRouter,
) : MviViewModel<DownloadState, DownloadIntent, MviEffect>(DownloadState()) {

    override fun handleIntent(intent: DownloadIntent) = when (intent) {
        DownloadIntent.OnCategoryClick -> {}
        DownloadIntent.NavigateBack -> {}
    }
}
