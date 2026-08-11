package com.samos.osmand.presentation.screen.download.viewmodel

import com.samos.osmand.presentation.mvi.MviState

data class DownloadState(
    val isLoading: Boolean = false,
) : MviState
