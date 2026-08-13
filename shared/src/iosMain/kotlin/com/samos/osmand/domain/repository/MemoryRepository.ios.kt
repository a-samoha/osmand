package com.samos.osmand.domain.repository

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSFileSystemSize
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSNumber

class IosMemoryRepository : MemoryRepository {
    @OptIn(ExperimentalForeignApi::class)
    override fun getFreeSpaceBytes(): Pair<Long, Float> {
        return try {
            val fileManager = NSFileManager.defaultManager
            val documentDirectory = NSHomeDirectory()
            val attributes = fileManager.attributesOfFileSystemForPath(documentDirectory, null)

            val freeBytes = (attributes?.get(NSFileSystemFreeSize) as? NSNumber)?.longLongValue ?: 0L
            val totalBytes = (attributes?.get(NSFileSystemSize) as? NSNumber)?.longLongValue ?: 0L

            val freePercentage = if (totalBytes > 0L) {
                ((freeBytes.toDouble() / totalBytes.toDouble()) * 100).toFloat()
            } else {
                0f
            }

            Pair(freeBytes, freePercentage)
        } catch (e: Exception) {
            Pair(0L, 0f)
        }
    }
}


actual fun createMemoryRepository(): MemoryRepository = IosMemoryRepository()
