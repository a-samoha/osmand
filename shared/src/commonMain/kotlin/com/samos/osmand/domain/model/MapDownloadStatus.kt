package com.samos.osmand.domain.model

sealed interface DownloadStatus {
    data object InQueue : DownloadStatus
    data class Downloading(val progress: Int) : DownloadStatus
    data object Downloaded : DownloadStatus // File on disk (both at app startup and after downloading)
    data class Error(val message: String) : DownloadStatus
}
