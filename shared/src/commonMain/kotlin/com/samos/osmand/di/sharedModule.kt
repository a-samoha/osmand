package com.samos.osmand.di

import com.samos.osmand.data.manager.MapDownloadManagerImpl
import com.samos.osmand.data.repository.createMemoryRepository
import com.samos.osmand.domain.manager.MapDownloadManager
import com.samos.osmand.domain.repository.MemoryRepository
import com.samos.osmand.presentation.navigation.router.ComposeRouter
import com.samos.osmand.presentation.navigation.router.ComposeRouterImpl
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadViewModel
import com.samos.osmand.presentation.screen.splash.SplashViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    includes(networkModule)

    singleOf(::ComposeRouterImpl) bind ComposeRouter::class
    singleOf(::MapDownloadManagerImpl) bind MapDownloadManager::class

    single<MemoryRepository> { createMemoryRepository() }

    viewModelOf(::SplashViewModel)
    viewModelOf(::DownloadViewModel)
}
