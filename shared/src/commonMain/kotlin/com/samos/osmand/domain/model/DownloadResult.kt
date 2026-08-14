package com.samos.osmand.domain.model

sealed interface DownloadResult {
    data object FileAlreadyExists : DownloadResult
    data class Progress(val percent: Int) : DownloadResult
    data object Success : DownloadResult
    data class Error(val message: String) : DownloadResult
}
