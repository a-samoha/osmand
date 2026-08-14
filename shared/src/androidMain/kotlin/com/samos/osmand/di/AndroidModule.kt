package com.samos.osmand.di

import com.samos.osmand.data.repository.AndroidMemoryRepository
import com.samos.osmand.domain.repository.MemoryRepository
import org.koin.dsl.module

val androidModule = module {
    single<MemoryRepository> { AndroidMemoryRepository() }
}
