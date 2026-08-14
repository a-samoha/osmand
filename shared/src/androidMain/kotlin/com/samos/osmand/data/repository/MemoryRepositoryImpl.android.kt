package com.samos.osmand.data.repository

import android.os.Environment
import android.os.StatFs
import com.samos.osmand.domain.repository.MemoryRepository

class AndroidMemoryRepository : MemoryRepository {

    override fun getFreeSpaceBytes(): Pair<Long, Float> {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)

            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            val totalBlocks = stat.blockCountLong

            val freeBytes = availableBlocks * blockSize
            val totalBytes = totalBlocks * blockSize

            val usedPercentage = if (totalBytes > 0) {
                1f - (freeBytes.toDouble() / totalBytes.toDouble()).toFloat()
            } else {
                0f
            }

            Pair(freeBytes, usedPercentage)
        } catch (e: Exception) {
            Pair(0L, 0f)
        }
    }
}

actual fun createMemoryRepository(): MemoryRepository = AndroidMemoryRepository()
