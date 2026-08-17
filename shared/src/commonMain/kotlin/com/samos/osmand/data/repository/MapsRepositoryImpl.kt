package com.samos.osmand.data.repository

import com.samos.osmand.data.source.OsmandApi
import com.samos.osmand.domain.model.MapDownloadResult
import com.samos.osmand.domain.repository.MapRepository
import com.samos.osmand.logger.LOGGER_TAG
import io.github.aakira.napier.Napier
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class MapRepositoryImpl(
    private val api: OsmandApi
) : MapRepository {

    private val downloadsFolder = Path(SystemTemporaryDirectory, "Downloads")

    override fun downloadMapFile(
        fileName: String,
        forceOverwrite: Boolean,
    ): Flow<MapDownloadResult> = flow {

        // 1. Check and create the Downloads folder if it doesn't exist yet
        if (!SystemFileSystem.exists(downloadsFolder)) {
            SystemFileSystem.createDirectories(downloadsFolder)
        }

        // The full path to our future file (e.g., Downloads/Denmark.zip)
        val targetFilePath = Path(downloadsFolder, fileName)

        // 2. CHECK: If the file exists and the user has NOT yet approved to overwrite.
        if (SystemFileSystem.exists(targetFilePath) && !forceOverwrite) {
            emit(MapDownloadResult.FileAlreadyExists)
            return@flow // Pause execution, wait for user reaction
        }

        // 3. If the file does not exist OR the user clicked "Overwrite" — start the download.
        try {
            val httpStatement = api.downloadMap(standard = "yes", fileName = fileName)

            httpStatement.execute { response ->
                val totalBytes = response.contentLength() ?: -1L
                val channel = response.bodyAsChannel()
                val fileSink = SystemFileSystem.sink(targetFilePath).buffered()

                val bufferSize = 8192L
                var totalBytesDownloaded = 0L
                var lastSentPercent = -1

                // 💡 FIX: Wrap the actual file streaming and writing loop into an inner try-catch
                try {
                    fileSink.use { sink ->
                        // Read the stream channel until it is completely exhausted
                        while (!channel.exhausted()) {
                            val chunk = channel.readRemaining(bufferSize)
                            totalBytesDownloaded += chunk.remaining
                            chunk.transferTo(sink)

                            if (totalBytes > 0) {
                                val progressPercent =
                                    ((totalBytesDownloaded.toDouble() / totalBytes) * 100).roundToInt()
                                if (progressPercent != lastSentPercent) {
                                    lastSentPercent = progressPercent
                                    emit(MapDownloadResult.Progress(progressPercent))
                                    Napier.d(tag = LOGGER_TAG) {"Download progress: $progressPercent%"}
                                }
                            } else {
                                val mbDownloaded = totalBytesDownloaded / (1024 * 1024)
                                Napier.d(tag = LOGGER_TAG) {"Downloaded: $mbDownloaded MB (Total size unknown)"}
                            }
                        }
                    }

                    emit(MapDownloadResult.Progress(100))
                    delay(50.milliseconds)
                    Napier.d(tag = LOGGER_TAG) {"Download status: Guaranteed 100% progress emitted after completion."}

                } catch (streamException: Exception) {
                    Napier.d(tag = LOGGER_TAG) {"Log Network ERROR: Connection interrupted mid-download: ${streamException.message}"}
                    throw streamException
                }
            }

            emit(MapDownloadResult.Success)

        } catch (e: Exception) {
            val isNetworkDrop =
                e.message?.contains("Connection", ignoreCase = true) == true
                        || e.message?.contains("host", ignoreCase = true) == true
            val resolvedMessage =
                if (isNetworkDrop) "Connection lost"
                else (e.message ?: "Unknown error")

            emit(MapDownloadResult.Error(resolvedMessage))
        }
    }
}
