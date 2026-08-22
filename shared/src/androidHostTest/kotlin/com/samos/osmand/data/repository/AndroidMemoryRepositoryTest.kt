package com.samos.osmand.data.repository

import android.os.StatFs
import android.os.Environment
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidMemoryRepositoryTest {

    private lateinit var repository: AndroidMemoryRepository
    private val mockStatFs = mockk<StatFs>()

    @BeforeTest
    fun setUp() {
        repository = AndroidMemoryRepository()
        mockkStatic(Environment::class)
        mockkConstructor(StatFs::class)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    // ==========================================
    // SUCCESS SCENARIOS
    // ==========================================

    @Test
    fun `getFreeSpaceBytes returns correct bytes and usage percentage when system stats are valid`() {
        // Given
        val fakeFile = File("/fake/data/dir")
        every { Environment.getDataDirectory() } returns fakeFile
        every { anyConstructed<StatFs>().blockSizeLong } returns 1024L
        every { anyConstructed<StatFs>().availableBlocksLong } returns 4L
        every { anyConstructed<StatFs>().blockCountLong } returns 10L

        // When
        val (freeBytes, usedPercentage) = repository.getFreeSpaceBytes()

        // Then
        assertEquals(4096L, freeBytes)
        assertEquals(0.6f, usedPercentage, 0.001f)
    }

    @Test
    fun `getFreeSpaceBytes handles division by zero when total blocks are zero`() {
        // Given
        val fakeFile = File("/fake/data/dir")
        every { Environment.getDataDirectory() } returns fakeFile
        every { mockStatFs.blockSizeLong } returns 1024L
        every { mockStatFs.availableBlocksLong } returns 0L
        every { mockStatFs.blockCountLong } returns 0L

        // When
        val (freeBytes, usedPercentage) = repository.getFreeSpaceBytes()

        // Then
        assertEquals(0L, freeBytes)
        assertEquals(0f, usedPercentage)
    }

    // ==========================================
    // ERROR SCENARIOS
    // ==========================================

    @Test
    fun `getFreeSpaceBytes returns fallback pair when platform environment throws exception`() {
        // Given
        // Simulate a system failure where Android OS cannot resolve the data directory
        every { Environment.getDataDirectory() } throws RuntimeException("System storage partition unavailable")

        // When
        val (freeBytes, usedPercentage) = repository.getFreeSpaceBytes()

        // Then
        assertEquals(0L, freeBytes)
        assertEquals(0f, usedPercentage)
    }

    @Test
    fun `getFreeSpaceBytes returns fallback pair when stat calculations throw exception`() {
        // Given
        val fakeFile = File("/fake/data/dir")
        every { Environment.getDataDirectory() } returns fakeFile

        // Simulate internal JNI block calculation failure inside Android Framework
        every { mockStatFs.blockSizeLong } throws IllegalArgumentException("Invalid internal stat directory block path")

        // When
        val (freeBytes, usedPercentage) = repository.getFreeSpaceBytes()

        // Then
        assertEquals(0L, freeBytes)
        assertEquals(0f, usedPercentage)
    }
}
