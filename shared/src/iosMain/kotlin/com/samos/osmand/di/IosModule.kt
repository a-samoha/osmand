package com.samos.osmand.di

import com.samos.osmand.data.repository.IosMemoryRepository
import com.samos.osmand.domain.network.IosNetworkMonitor
import com.samos.osmand.domain.network.NetworkMonitor
import com.samos.osmand.domain.repository.MemoryRepository
import com.samos.osmand.presentation.base.ToastManager
import com.samos.osmand.presentation.base.ToastManagerImplIos
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val iosModule = module {
    single<MemoryRepository> { IosMemoryRepository() }

    factoryOf(::ToastManagerImplIos) bind ToastManager::class
    factoryOf(::IosNetworkMonitor) bind NetworkMonitor::class
}
