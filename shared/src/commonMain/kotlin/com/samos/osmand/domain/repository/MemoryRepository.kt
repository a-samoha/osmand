package com.samos.osmand.domain.repository

interface MemoryRepository {
    fun getFreeSpaceBytes(): Pair<Long, Float>
}
