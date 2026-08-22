package com.samos.osmand.di

import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.samos.osmand.data.manager.MapDownloadManagerImpl
import com.samos.osmand.data.repository.ConfigRepositoryImpl
import com.samos.osmand.data.repository.createMemoryRepository
import com.samos.osmand.domain.network.MapDownloadManager
import com.samos.osmand.domain.repository.ConfigRepository
import com.samos.osmand.domain.repository.MemoryRepository
import com.samos.osmand.presentation.navigation.router.ComposeRouter
import com.samos.osmand.presentation.navigation.router.ComposeRouterImpl
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadViewModel
import com.samos.osmand.presentation.screen.splash.SplashViewModel
import okio.Path.Companion.toPath
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

const val DATASTORE_FILE_NAME = "app_settings.preferences_pb"

fun createDataStore(producePath: () -> String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
}

val sharedModule = module {
    includes(networkModule)

    /**
     * Use to pass named dependencies (get(named("...")))
     * or static parameters (e.g., ConfigRepositoryImpl(get(), isDebug = true)).
     */
    single<ConfigRepository> { ConfigRepositoryImpl(get()) }

    /**
     * Automatically injects dependencies via reflection or compile-time code generation
     *  - substitutes `get()` for all its parameters.
     */
    singleOf(::ComposeRouterImpl) bind ComposeRouter::class
    singleOf(::MapDownloadManagerImpl) bind MapDownloadManager::class

    single<MemoryRepository> { createMemoryRepository() }

    viewModelOf(::SplashViewModel)
    viewModelOf(::DownloadViewModel)
}
