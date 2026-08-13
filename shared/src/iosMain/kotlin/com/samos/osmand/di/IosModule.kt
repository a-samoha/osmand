package com.samos.osmand.di

import com.samos.osmand.domain.repository.IosMemoryRepository
import com.samos.osmand.domain.repository.MemoryRepository
import org.koin.dsl.module

val iosModule = module {
    single<MemoryRepository> { IosMemoryRepository() }
}
