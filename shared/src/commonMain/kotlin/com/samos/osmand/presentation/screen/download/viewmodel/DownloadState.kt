package com.samos.osmand.presentation.screen.download.viewmodel

import com.samos.osmand.presentation.mvi.MviState

data class DownloadState(
    val isLoading: Boolean = false,
    val usableMemory: String? = null,
    val usedMemoryProgress: Float = 0f,
    val items: List<DownloadItemModel> = getMockItems(),
) : MviState

data class DownloadItemModel(
    val id: String,
    val title: String,
)

private fun getMockItems(): List<DownloadItemModel> {

    val countries = listOf(
        "Albania", "Croatia", "Estonia", "Germany", "Ukraine", "Poland", "Italy",
        "France", "Spain", "Austria", "Belgium", "Denmark", "Finland", "Greece"
    )

    val items: List<DownloadItemModel> = List(30) { index ->
        DownloadItemModel(
            id = "id_${index}_${(1000..9999).random()}",
            title = "${countries[index % countries.size]} #${index + 1}"
        )
    }

    return items
}
