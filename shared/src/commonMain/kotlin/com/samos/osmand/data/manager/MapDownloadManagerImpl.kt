package com.samos.osmand.data.manager

import com.samos.osmand.domain.manager.MapDownloadManager
import com.samos.osmand.domain.model.DownloadStatus
import com.samos.osmand.domain.model.MapDownloadResult
import com.samos.osmand.domain.model.xml.RegionNode
import com.samos.osmand.domain.model.xml.RegionsListXml
import com.samos.osmand.domain.repository.MapRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import nl.adaptivity.xmlutil.serialization.XML
import osmand.shared.generated.resources.Res

class MapDownloadManagerImpl(
    private val repository: MapRepository,
) : MapDownloadManager {

    private val downloadScope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
    private val downloadsFolder = Path(SystemTemporaryDirectory, "Downloads")
    private val queueMutex = Mutex() // A mutex guarantees sequential execution

    private val _downloadStates = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    override val downloadStates = _downloadStates.asStateFlow()

    init {
        scanLocalDownloads()
    }

    /**
     * 2. ADDING TO QUEUE: Sequential loading via Mutex
     */
    override fun enqueueDownload(fileName: String, forceOverwrite: Boolean) {
        // Update specific item state to 'InQueue' without changing other maps
        _downloadStates.update { it + (fileName to DownloadStatus.InQueue) }

        downloadScope.launch {
            // The lock is closing. The next file will wait here until this one finishes collecting.
            queueMutex.withLock {
                _downloadStates.update { it + (fileName to DownloadStatus.Downloading(0)) }

                repository.downloadMapFile(fileName, forceOverwrite).collect { result ->
                    when (result) {
                        is MapDownloadResult.Progress -> {
                            _downloadStates.update {
                                it + (fileName to DownloadStatus.Downloading(result.percent))
                            }
                        }
                        is MapDownloadResult.Success -> {
                            _downloadStates.update { it + (fileName to DownloadStatus.Downloaded) }
                        }
                        is MapDownloadResult.Error -> {
                            _downloadStates.update { it + (fileName to DownloadStatus.Error(result.message)) }
                        }
                        is MapDownloadResult.FileAlreadyExists -> {
                            // UI overwrite dialog handling logic triggers here
                        }
                    }
                }
            }
        }
    }

    /**
     * 3. DELETE FROM DISK: Deletes the file and clears the status in the UI.
     */
    override fun deleteMapFile(fileName: String) {
        downloadScope.launch {
            try {
                val filePath = Path(downloadsFolder, fileName)

                if (SystemFileSystem.exists(filePath)) {
                    // Delete the file using kotlinx-io
                    SystemFileSystem.delete(filePath)
                    println("File successfully deleted from storage: $fileName")
                }
                // 💡 Reset state back to 'NotDownloaded' instead of removing from the map completely
                _downloadStates.update { it + (fileName to DownloadStatus.NotDownloaded) }
            } catch (e: Exception) {
                println("Failed to delete file $fileName: ${e.message}")
                _downloadStates.update { it + (fileName to DownloadStatus.Error("Deletion failed: ${e.message}")) }
            }
        }
    }

    /**
     * 1 .DISK SCAN: Checks the folder when the application restarts.
     */
    private fun scanLocalDownloads() {
        downloadScope.launch {
            try {
                // 1. Parse map names from the XML asset file
                val parsedMaps = parseMapNamesFromXml()
                println("Test allAppMaps: $parsedMaps")

                val initialStates =
                    parsedMaps.associateTo(mutableMapOf<String, DownloadStatus>()) { fileName ->
                        fileName to DownloadStatus.NotDownloaded
                    }

                // 2. Check physical disk storage for existing files
                if (SystemFileSystem.exists(downloadsFolder)) {
                    for (fileName in parsedMaps) {
                        val filePath = Path(downloadsFolder, fileName)
                        if (SystemFileSystem.exists(filePath)) {
                            // Mark file as fully downloaded and ready offline
                            initialStates[fileName] = DownloadStatus.Downloaded
                        }
                    }
                }

                // Update StateFlow with initial offline map data
                _downloadStates.value = initialStates
                println("Log XML: Successfully published ${initialStates.size} maps to StateFlow")
            } catch (e: Exception) {
                println("Critical error during local downloads scanning: ${e.message}")
            }
        }
    }

    private suspend fun parseMapNamesFromXml(): List<String> {
        return try {
            // Read XML file from Compose Multiplatform shared resources
            val xmlBytes = Res.readBytes("files/regions.xml")
            val rawXml = xmlBytes.decodeToString()

            // No configuration needed anymore because all fields are explicitly declared in the data class
            val xmlParser = XML {}

            // Deserialize XML into Kotlin tree structures
            val container = xmlParser.decodeFromString(RegionsListXml.serializer(), rawXml)
            println("Log XML: Successfully deserialized root regions")

            // Flatten the recursive tree into a clean list of file names (defaulting to 'europe' suffix)
            val generatedNames = extractMapFileNames(container.regions, parentSuffix = "europe")
            println("Log XML: Extraction finished. Generated ${generatedNames.size} total map names")

            generatedNames
        } catch (e: Exception) {
            println("Log XML CRITICAL ERROR: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    private fun extractMapFileNames(
        nodes: List<RegionNode>,
        parentPrefix: String? = null,
        parentSuffix: String? = null
    ): List<String> {
        // Internal helper function to ensure the very first letter of a string is capitalized
        fun String.capitalizeFirstLetter(): String {
            if (this.isEmpty()) return this
            return this.substring(0, 1).lowercase()
                .replaceFirstChar { it.uppercase() } + this.substring(1)
        }

        val fileNames = mutableListOf<String>()

        for (node in nodes) {
            // Resolve prefix rule from parent hierarchy
            val currentPrefix = when {
                node.inner_download_prefix == "\$name" -> node.name
                node.inner_download_prefix != null -> node.inner_download_prefix
                else -> parentPrefix
            }

            // Suffix falls back to parent if not defined locally
            val currentSuffix = node.inner_download_suffix ?: parentSuffix

            // If the node represents an actual map asset, construct its name
            val isMap =
                node.map == "yes" || (node.type == "map" || (node.type == null && node.map != "no"))

            if (isMap && node.name != null) {
                val baseName = StringBuilder()

                // 1. Build the core name string using prefix and suffix
                if (!currentPrefix.isNullOrEmpty()) {
                    baseName.append("${currentPrefix}_")
                    baseName.append(node.name)
                } else {
                    baseName.append(node.name)
                }

                if (!currentSuffix.isNullOrEmpty()) {
                    baseName.append("_$currentSuffix")
                }

                val finalFileName = "${baseName.toString().capitalizeFirstLetter()}_2.obf.zip"

                fileNames.add(finalFileName)
            }

            // Recursively traverse deeper into subregions
            if (node.subRegions.isNotEmpty()) {
                fileNames.addAll(
                    extractMapFileNames(node.subRegions, currentPrefix, currentSuffix)
                )
            }
        }
        return fileNames
    }
}
