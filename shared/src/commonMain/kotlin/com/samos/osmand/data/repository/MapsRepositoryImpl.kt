package com.samos.osmand.data.repository

import com.samos.osmand.data.source.OsmandApi
import com.samos.osmand.domain.model.MapDownloadResult
import com.samos.osmand.domain.repository.MapRepository
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
                if (totalBytes <= 0) {
                    emit(MapDownloadResult.Progress(100))
                    delay(50.milliseconds)
                    println("Download status: Force-emitted 100% progress for dynamic stream.")
                }
                val channel = response.bodyAsChannel()

                val fileSink = SystemFileSystem.sink(targetFilePath).buffered()

                val bufferSize = 8192L
                var totalBytesDownloaded = 0L
                var lastSentPercent = -1

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
                                println("Download progress: $progressPercent%")
                            }
                        } else {
                            // Log the download volume incrementally if Content-Length is missing
                            val mbDownloaded = totalBytesDownloaded / (1024 * 1024)
                            println("Downloaded: $mbDownloaded MB (Total size unknown)")
                        }
                    }
                }
            }
            emit(MapDownloadResult.Success)
        } catch (e: Exception) {
            emit(MapDownloadResult.Error(e.message ?: "Unknown error"))
        }
    }
}
