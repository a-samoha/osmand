package com.samos.osmand.data.manager

import com.samos.osmand.domain.model.DownloadStatus
import com.samos.osmand.domain.model.MapDownloadResult
import com.samos.osmand.domain.model.xml.RegionNode
import com.samos.osmand.domain.model.xml.RegionsListXml
import com.samos.osmand.domain.network.DownloadManagerEffect
import com.samos.osmand.domain.network.MapDownloadManager
import com.samos.osmand.domain.repository.MapRepository
import com.samos.osmand.logger.LOGGER_TAG
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

private const val downloads = "Downloads"

class MapDownloadManagerImpl(
    private val repository: MapRepository,
) : MapDownloadManager {

    private val downloadScope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
    private val downloadsFolder = Path(SystemTemporaryDirectory, downloads)
    private val queueMutex = Mutex() // A mutex guarantees sequential execution

    private val _downloadStates = MutableStateFlow<Map<RegionNode, DownloadStatus>>(emptyMap())
    override val downloadStates = _downloadStates.asStateFlow()

    private val _downloadEffects = MutableSharedFlow<DownloadManagerEffect>(extraBufferCapacity = 1)
    override val downloadEffects = _downloadEffects.asSharedFlow()

    private val activeJobs = mutableMapOf<RegionNode, Job>()

    init {
        scanLocalDownloads()
    }

    /**
     * 2. ADDING TO QUEUE: Sequential loading via Mutex
     */
    override fun enqueueDownload(node: RegionNode, forceOverwrite: Boolean) {
        _downloadStates.update { it + (node to DownloadStatus.InQueue) }

        val downloadJob = downloadScope.launch {
            var isSuccess = false

            try {
                queueMutex.withLock {
                    _downloadStates.update { it + (node to DownloadStatus.Downloading(0)) }
                    val fileName = generateFileName(node)

                    repository.downloadMapFile(fileName, forceOverwrite).collect { result ->
                        when (result) {
                            is MapDownloadResult.Progress -> {
                                _downloadStates.update {
                                    it + (node to DownloadStatus.Downloading(result.percent))
                                }
                            }
                            is MapDownloadResult.Success -> {
                                isSuccess = true
                                _downloadStates.update { it + (node to DownloadStatus.Downloaded) }
                            }
                            is MapDownloadResult.Error -> {
                                val isNetworkIssue =
                                    result.message.contains("UnknownHostException") ||
                                            result.message.contains("ConnectException") ||
                                            result.message == "Connection lost"

                                val errorMessage = if (isNetworkIssue) {
                                    "No internet connection. Please check your network."
                                } else {
                                    result.message
                                }

                                if (isNetworkIssue) {
                                    _downloadEffects.tryEmit(DownloadManagerEffect.ConnectionLost)
                                }

                                _downloadStates.update {
                                    it + (node to DownloadStatus.Error(errorMessage))
                                }
                            }
                            is MapDownloadResult.FileAlreadyExists -> {}
                        }
                    }
                }
            } finally {
                if (!isSuccess) deleteMapFile(node)
                activeJobs.remove(node)
            }
        }

        activeJobs[node] = downloadJob
    }

    /**
     * 3. DELETE FROM DISK: Deletes the file and clears the status in the UI.
     */
    override fun deleteMapFile(node: RegionNode) {
        downloadScope.launch {
            try {
                val fileName = generateFileName(node)
                val filePath = Path(downloadsFolder, fileName)
                if (SystemFileSystem.exists(filePath)) {
                    SystemFileSystem.delete(filePath)
                }
                _downloadStates.update { it + (node to DownloadStatus.NotDownloaded) }
            } catch (e: Exception) {
                _downloadStates.update { it + (node to DownloadStatus.Error("Deletion failed: ${e.message}")) }
            }
        }
    }

    override fun cancelDownload(node: RegionNode) {
        val runningJob = activeJobs[node]

        if (runningJob != null && runningJob.isActive) {
            runningJob.cancel()
            deleteMapFile(node)
            Napier.d(tag = LOGGER_TAG) {"Log Network: Download cancelled by user for node: ${node.name}"}
        }

        _downloadStates.update { it + (node to DownloadStatus.NotDownloaded) }
    }

    /**
     * 1 .DISK SCAN: Checks the folder when the application restarts.
     */
    private fun scanLocalDownloads() {
        downloadScope.launch {
            try {
                // 1. Read and parse XML into raw objects tree
                val parsedNodes = parseXmlTree()

                // 2. Build flat list of all leaves/nodes from the tree
                val allNodes = flattenNodes(parsedNodes)

                // 3. Initialize everything as NotDownloaded
                val initialStates =
                    allNodes.associateTo(mutableMapOf<RegionNode, DownloadStatus>()) { node ->
                        node to DownloadStatus.NotDownloaded
                    }

                // 4. Check disk storage using file name generated from each node
                if (SystemFileSystem.exists(downloadsFolder)) {
                    for (node in allNodes) {
                        val fileName = generateFileName(node)
                        // Skip empty strings if the node is a pure container without an asset file
                        if (fileName.isNotEmpty()) {
                            val filePath = Path(downloadsFolder, fileName)
                            if (SystemFileSystem.exists(filePath)) {
                                // Now this assignment is perfectly legal
                                initialStates[node] = DownloadStatus.Downloaded
                            }
                        }
                    }
                }

                // Publish the complete map list with initial statuses
                _downloadStates.value = initialStates
                Napier.d(tag = LOGGER_TAG) {"Log XML: Successfully published ${initialStates.size} maps to StateFlow"}
            } catch (e: Exception) {
                Napier.d(tag = LOGGER_TAG) {"Critical error during local nodes scanning: ${e.message}"}
            }
        }
    }

    // Helper to flatten recursive XML nodes for scanning initialization
    private fun flattenNodes(nodes: List<RegionNode>): List<RegionNode> {
        val flatList = mutableListOf<RegionNode>()
        for (node in nodes) {
            flatList.add(node)
            if (node.subRegions.isNotEmpty()) {
                flatList.addAll(flattenNodes(node.subRegions))
            }
        }
        return flatList
    }

    // Wrapper to get string file name using your existing rule
    private fun generateFileName(node: RegionNode): String {
        // Reuse your extractMapFileNames rule here for a single node context
        return extractMapFileNames(listOf(node), parentSuffix = "europe").firstOrNull() ?: ""
    }

    private suspend fun parseXmlTree(): List<RegionNode> {
        return try {
            // Read XML file from Compose Multiplatform shared resources
            val xmlBytes = Res.readBytes("files/regions.xml")
            val rawXml = xmlBytes.decodeToString()

            // Configure the XML parser engine for XMLUtil 0.90+
            val xmlParser = XML {
                xmlDeclMode = XmlDeclMode.None
                indent = 0
            }

            // Deserialize XML into Kotlin tree structures
            val container = xmlParser.decodeFromString(RegionsListXml.serializer(), rawXml)
            Napier.d(tag = LOGGER_TAG) {"Log XML: Successfully deserialized ${container.regions.size} root regions from XML tree"}

            container.regions
        } catch (e: Exception) {
            Napier.d(tag = LOGGER_TAG) {"Log XML CRITICAL ERROR during tree parsing: ${e.message}"}
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
            val isMap = node.map == "yes"
                    || (node.type == "map"
                    || (node.type == null && node.map != "no"))

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
