package com.samos.osmand.data.repository

import com.samos.osmand.data.source.OsmandApi
import com.samos.osmand.domain.model.MapDownloadResult
import io.ktor.client.statement.HttpStatement
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapRepositoryImplTest {

    private val api = mockk<OsmandApi>()
    private lateinit var fakeFileSystem: FakeFileSystem
    private lateinit var repository: MapRepositoryImpl

    private val tempDir = "tmp".toPath()
    private val downloadsFolder = tempDir.div("Downloads")

    @BeforeTest
    fun setUp() {
        fakeFileSystem = FakeFileSystem()
        repository = MapRepositoryImpl(api)
    }

    @AfterTest
    fun tearDown() {
        fakeFileSystem.checkNoOpenFiles()
    }

    // ==========================================
    // ERROR SCENARIOS
    // ==========================================

    @Test
    fun `downloadMapFile emits connection lost error when network drops during setup`() = runTest {
        // Given
        val fileName = "Germany.zip"
        coEvery { api.downloadMap(standard = "yes", fileName = fileName) } throws IllegalStateException("Connection timeout drop host")

        // When
        val results = repository.downloadMapFile(fileName, forceOverwrite = false).toList()

        // Then
        assertEquals(1, results.size)
        val errorResult = results.first() as MapDownloadResult.Error
        assertEquals("Connection lost", errorResult.message)
    }

    @Test
    fun `downloadMapFile emits custom error when streaming is interrupted mid-download`() = runTest {
        // Given
        val fileName = "France.zip"
        val httpStatement = mockk<HttpStatement>()

        coEvery { httpStatement.execute<Unit>(any()) } throws IllegalStateException("Mid-download stream failure")
        coEvery { api.downloadMap(standard = "yes", fileName = fileName) } returns httpStatement

        // When
        val results = repository.downloadMapFile(fileName, forceOverwrite = false).toList()

        // Then
        assertTrue(results.last() is MapDownloadResult.Error)
        val errorResult = results.last() as MapDownloadResult.Error
        assertEquals("Mid-download stream failure", errorResult.message)
    }
}
