package com.samos.osmand.data.manager

import com.samos.osmand.domain.model.DownloadStatus
import com.samos.osmand.domain.model.MapDownloadResult
import com.samos.osmand.domain.model.xml.RegionNode
import com.samos.osmand.domain.network.DownloadManagerEffect
import com.samos.osmand.domain.repository.MapRepository
import com.samos.osmand.domain.service.ServiceTracker
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.buffered
import kotlinx.io.writeString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val AWAIT_TIMEOUT_MS = 5_000L
private val downloadsFolder = Path(SystemTemporaryDirectory, "Downloads")

class MapDownloadManagerImplTest {

    private lateinit var repository: MapRepository
    private lateinit var serviceTracker: ServiceTracker
    private lateinit var manager: MapDownloadManagerImpl

    @BeforeTest
    fun setUp() {
        deleteRecursively(downloadsFolder)
        repository = mockk<MapRepository>()
        serviceTracker = mockk<ServiceTracker>()
        every { serviceTracker.startDownloadService() } just Runs
        every { serviceTracker.stopDownloadService() } just Runs
        manager = MapDownloadManagerImpl(repository, serviceTracker)
    }

    @AfterTest
    fun tearDown() {
        deleteRecursively(downloadsFolder)
    }

    // ---------- enqueueDownload ----------

    @Test
    fun enqueueDownload_whenDownloadSucceeds_updatesStateToDownloaded() = runBlocking {
        // Given
        val node = mapRegionNode("Ukraine")
        every { repository.downloadMapFile(any(), any()) } returns
            flowOf(MapDownloadResult.Progress(50), MapDownloadResult.Success)

        // When
        manager.enqueueDownload(node, forceOverwrite = false, startForeground = false)

        // Then
        val status = waitForStatus(node) { it is DownloadStatus.Downloaded }
        assertEquals(DownloadStatus.Downloaded, status)
    }

    @Test
    fun enqueueDownload_whenStartForegroundIsTrue_startsDownloadServiceImmediately() = runBlocking {
        // Given
        val node = mapRegionNode("Poland")
        every { repository.downloadMapFile(any(), any()) } returns flowOf(MapDownloadResult.Success)

        // When
        manager.enqueueDownload(node, forceOverwrite = false, startForeground = true)

        // Then
        verify(exactly = 1) { serviceTracker.startDownloadService() }
    }

    @Test
    fun enqueueDownload_whenErrorIsNetworkRelated_emitsConnectionLostEffectAndCleansUpState() = runBlocking {
        // Given
        val node = mapRegionNode("Germany")
        every { repository.downloadMapFile(any(), any()) } returns
            flowOf(MapDownloadResult.Error("UnknownHostException: unable to resolve host"))

        // When
        manager.enqueueDownload(node, forceOverwrite = false, startForeground = false)

        // Then
        val effect = withTimeout(AWAIT_TIMEOUT_MS) { manager.downloadEffects.first() }
        assertEquals(DownloadManagerEffect.ConnectionLost, effect)
        val finalStatus = waitForStatus(node) { it is DownloadStatus.NotDownloaded }
        assertEquals(DownloadStatus.NotDownloaded, finalStatus)
    }

    @Test
    fun enqueueDownload_whenErrorIsGeneric_doesNotEmitConnectionLostEffectAndCleansUpState() = runBlocking {
        // Given
        val node = mapRegionNode("France")
        every { repository.downloadMapFile(any(), any()) } returns
            flowOf(MapDownloadResult.Error("Server responded with 500"))

        // When
        manager.enqueueDownload(node, forceOverwrite = false, startForeground = false)

        // Then
        val finalStatus = waitForStatus(node) { it is DownloadStatus.NotDownloaded }
        assertEquals(DownloadStatus.NotDownloaded, finalStatus)
        val effect = withTimeoutOrNull(300L) { manager.downloadEffects.first() }
        assertNull(effect)
    }

    // ---------- deleteMapFile ----------

    @Test
    fun deleteMapFile_whenFileDoesNotExist_updatesStateToNotDownloaded() = runBlocking {
        // Given
        val node = mapRegionNode("Spain")

        // When
        manager.deleteMapFile(node)

        // Then
        val status = waitForStatus(node) { it is DownloadStatus.NotDownloaded }
        assertEquals(DownloadStatus.NotDownloaded, status)
    }

    @Test
    fun deleteMapFile_whenFileExists_deletesFileAndUpdatesStateToNotDownloaded() = runBlocking {
        // Given
        val node = mapRegionNode("Ukraine")
        val filePath = Path(downloadsFolder, "Ukraine_europe_2.obf.zip")
        createFile(filePath)
        assertTrue(SystemFileSystem.exists(filePath))

        // When
        manager.deleteMapFile(node)

        // Then
        val status = waitForStatus(node) { it is DownloadStatus.NotDownloaded }
        assertEquals(DownloadStatus.NotDownloaded, status)
        assertTrue(!SystemFileSystem.exists(filePath))
    }

    @Test
    fun deleteMapFile_whenTargetPathIsNonEmptyDirectory_setsErrorState() = runBlocking {
        // Given
        val node = mapRegionNode("Moldova")
        val dirPath = Path(downloadsFolder, "Moldova_europe_2.obf.zip")
        SystemFileSystem.createDirectories(dirPath)
        createFile(Path(dirPath, "leftover.tmp"))

        // When
        manager.deleteMapFile(node)

        // Then
        val status = waitForStatus(node) { it is DownloadStatus.Error }
        assertTrue(status is DownloadStatus.Error)
        assertTrue(status.message.contains("Deletion failed"))
    }

    @Test
    fun deleteMapFile_whenTargetPathIsNonEmptyDirectoryWithMultipleEntries_setsErrorState() = runBlocking {
        // Given
        val node = mapRegionNode("Belarus")
        val dirPath = Path(downloadsFolder, "Belarus_europe_2.obf.zip")
        SystemFileSystem.createDirectories(dirPath)
        createFile(Path(dirPath, "part1.tmp"))
        createFile(Path(dirPath, "part2.tmp"))

        // When
        manager.deleteMapFile(node)

        // Then
        val status = waitForStatus(node) { it is DownloadStatus.Error }
        assertTrue(status is DownloadStatus.Error)
        assertTrue((status as DownloadStatus.Error).message.contains("Deletion failed"))
    }

    // ---------- cancelDownload ----------

    @Test
    fun cancelDownload_whenNoActiveJobExists_setsStateToNotDownloadedImmediately() {
        // Given
        val node = mapRegionNode("Italy")

        // When
        manager.cancelDownload(node)

        // Then
        assertEquals(DownloadStatus.NotDownloaded, manager.downloadStates.value[node])
    }

    @Test
    fun cancelDownload_whenJobAlreadyCompleted_overwritesStateToNotDownloaded() = runBlocking {
        // Given
        val node = mapRegionNode("Austria")
        every { repository.downloadMapFile(any(), any()) } returns flowOf(MapDownloadResult.Success)
        manager.enqueueDownload(node, forceOverwrite = false, startForeground = false)
        waitForStatus(node) { it is DownloadStatus.Downloaded }

        // When
        manager.cancelDownload(node)

        // Then
        assertEquals(DownloadStatus.NotDownloaded, manager.downloadStates.value[node])
    }

    @Test
    fun cancelDownload_whenActiveJobCleanupFails_eventuallyReflectsErrorState() = runBlocking {
        // Given
        val node = mapRegionNode("Latvia")
        val dirPath = Path(downloadsFolder, "Latvia_europe_2.obf.zip")
        SystemFileSystem.createDirectories(dirPath)
        createFile(Path(dirPath, "leftover.tmp"))
        every { repository.downloadMapFile(any(), any()) } returns flow {
            emit(MapDownloadResult.Progress(10))
            delay(Long.MAX_VALUE)
        }
        manager.enqueueDownload(node, forceOverwrite = false, startForeground = false)
        waitForStatus(node) { it is DownloadStatus.Downloading }

        // When
        manager.cancelDownload(node)

        // Then
        assertEquals(DownloadStatus.NotDownloaded, manager.downloadStates.value[node])
        val status = waitForStatus(node) { it is DownloadStatus.Error }
        assertTrue(status is DownloadStatus.Error)
        assertTrue((status as DownloadStatus.Error).message.contains("Deletion failed"))
    }

    @Test
    fun cancelDownload_whenActiveJobCleanupFailsWithMultipleLeftovers_eventuallyReflectsErrorState() = runBlocking {
        // Given
        val node = mapRegionNode("Estonia")
        val dirPath = Path(downloadsFolder, "Estonia_europe_2.obf.zip")
        SystemFileSystem.createDirectories(dirPath)
        createFile(Path(dirPath, "part1.tmp"))
        createFile(Path(dirPath, "part2.tmp"))
        every { repository.downloadMapFile(any(), any()) } returns flow {
            emit(MapDownloadResult.Progress(5))
            delay(Long.MAX_VALUE)
        }
        manager.enqueueDownload(node, forceOverwrite = true, startForeground = false)
        waitForStatus(node) { it is DownloadStatus.Downloading }

        // When
        manager.cancelDownload(node)

        // Then
        assertEquals(DownloadStatus.NotDownloaded, manager.downloadStates.value[node])
        val status = waitForStatus(node) { it is DownloadStatus.Error }
        assertTrue(status is DownloadStatus.Error)
        assertTrue((status as DownloadStatus.Error).message.contains("Deletion failed"))
    }

    // ---------- helpers ----------

    private suspend fun waitForStatus(
        node: RegionNode,
        timeoutMillis: Long = AWAIT_TIMEOUT_MS,
        predicate: (DownloadStatus?) -> Boolean
    ): DownloadStatus? = withTimeout(timeoutMillis) {
        manager.downloadStates.first { predicate(it[node]) }[node]
    }

    private fun mapRegionNode(name: String): RegionNode = RegionNode(
        name = name,
        subRegions = emptyList(),
        inner_download_prefix = null,
        inner_download_suffix = null,
        map = "yes",
        type = null,
    )

    private fun createFile(path: Path) {
        path.parent?.let { SystemFileSystem.createDirectories(it) }
        SystemFileSystem.sink(path).use { sink ->
            sink.buffered().use { it.writeString("data") }
        }
    }

    private fun deleteRecursively(path: Path) {
        if (!SystemFileSystem.exists(path)) return
        val metadata = SystemFileSystem.metadataOrNull(path)
        if (metadata?.isDirectory == true) {
            SystemFileSystem.list(path).forEach { deleteRecursively(it) }
        }
        SystemFileSystem.delete(path, mustExist = false)
    }
}
