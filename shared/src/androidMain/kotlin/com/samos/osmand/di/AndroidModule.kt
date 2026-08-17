package com.samos.osmand.di

import com.samos.osmand.data.repository.AndroidMemoryRepository
import com.samos.osmand.domain.network.AndroidNetworkMonitor
import com.samos.osmand.domain.network.NetworkMonitor
import com.samos.osmand.domain.repository.MemoryRepository
import com.samos.osmand.domain.service.AndroidServiceTracker
import com.samos.osmand.domain.service.ServiceTracker
import com.samos.osmand.presentation.base.ToastManager
import com.samos.osmand.presentation.base.ToastManagerImplAndroid
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val androidModule = module {
    single<MemoryRepository> { AndroidMemoryRepository() }
    single<ServiceTracker> { AndroidServiceTracker(get()) }

    factoryOf(::ToastManagerImplAndroid) bind ToastManager::class
    factoryOf(::AndroidNetworkMonitor) bind NetworkMonitor::class
}
