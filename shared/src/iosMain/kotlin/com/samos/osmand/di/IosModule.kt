package com.samos.osmand.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.samos.osmand.data.repository.IosMemoryRepository
import com.samos.osmand.domain.network.IosNetworkMonitor
import com.samos.osmand.domain.network.NetworkMonitor
import com.samos.osmand.domain.repository.MemoryRepository
import com.samos.osmand.presentation.base.ToastManager
import com.samos.osmand.presentation.base.ToastManagerImplIos
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

val iosModule = module {
    single<MemoryRepository> { IosMemoryRepository() }

    single<DataStore<Preferences>> {
        createDataStore {
            @OptIn(ExperimentalForeignApi::class)
            val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null
            )
            val path = requireNotNull(documentDirectory?.path)
            "$path/$DATASTORE_FILE_NAME"
        }
    }

    factoryOf(::ToastManagerImplIos) bind ToastManager::class
    factoryOf(::IosNetworkMonitor) bind NetworkMonitor::class
}
