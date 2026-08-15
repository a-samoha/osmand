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
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML
import osmand.shared.generated.resources.Res

class MapDownloadManagerImpl(
    private val repository: MapRepository
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
        // Tell the UI that the file has been queued.
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
                            // Логіка показу діалогу (якщо обробляється через окрему подію/колбек)
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
                    println("Файл успішно видалено з диска: $fileName")
                }

                _downloadStates.update { currentMap ->
                    currentMap - fileName
                }
            } catch (e: Exception) {
                println("Помилка при видаленні файлу $fileName: ${e.message}")
                _downloadStates.update {
                    it + (fileName to DownloadStatus.Error("Не вдалося видалити: ${e.message}"))
                }
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
                val allAppMaps = parseMapNamesFromXml()
                println("Test allAppMaps: $allAppMaps")

                // 2. Check physical disk storage for existing files
                if (SystemFileSystem.exists(downloadsFolder)) {
                    val localMapStates = mutableMapOf<String, DownloadStatus>()

                    for (fileName in allAppMaps) {
                        val filePath = Path(downloadsFolder, fileName)
                        if (SystemFileSystem.exists(filePath)) {
                            // Mark file as fully downloaded and ready offline
                            localMapStates[fileName] = DownloadStatus.Downloaded
                        }
                    }
                    // Update StateFlow with initial offline map data
                    _downloadStates.value = localMapStates
                }
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

            // 💡 FIXED: Correct DSL builder constructor for XMLUtil 0.90+
            val xmlParser = XML {
                // Configure XML declaration mode inside the builder scope
                xmlDeclMode = XmlDeclMode.None

                // Basic configuration parameters for parsing stability
                indent = 0
            }

            // Deserialize XML into Kotlin tree structures
            val container = xmlParser.decodeFromString(RegionsListXml.serializer(), rawXml)

            // Flatten the recursive tree into a clean list of file names (defaulting to 'europe' suffix)
            extractMapFileNames(container.regions, parentSuffix = "europe")
        } catch (e: Exception) {
            println("XML serialization parsing failed: ${e.message}")
            emptyList()
        }
    }

    private fun extractMapFileNames(
        nodes: List<RegionNode>,
        parentPrefix: String? = null,
        parentSuffix: String? = null
    ): List<String> {
        val fileNames = mutableListOf<String>()

        for (node in nodes) {
            // Resolve prefix rule: if prefix is "$name", use current node name
            val currentPrefix = when {
                node.inner_download_prefix == "\$name" -> node.name
                node.inner_download_prefix != null -> node.inner_download_prefix
                else -> parentPrefix
            }

            // Suffix falls back to parent if not defined locally
            val currentSuffix = node.inner_download_suffix ?: parentSuffix

            // If the node represents an actual map asset, construct its name
            if (node.type == "map" && node.name != null) {
                val baseName = StringBuilder()

                if (!currentPrefix.isNullOrEmpty()) {
                    baseName.append("${currentPrefix}_")
                }
                baseName.append(node.name)
                if (!currentSuffix.isNullOrEmpty()) {
                    baseName.append("_$currentSuffix")
                }
                baseName.append(".obf.zip")

                fileNames.add(baseName.toString())
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
