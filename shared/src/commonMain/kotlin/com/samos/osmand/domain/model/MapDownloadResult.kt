package com.samos.osmand.domain.model

sealed interface MapDownloadResult {
    data object FileAlreadyExists : MapDownloadResult
    data class Progress(val percent: Int) : MapDownloadResult
    data object Success : MapDownloadResult
    data class Error(val message: String) : MapDownloadResult
}
