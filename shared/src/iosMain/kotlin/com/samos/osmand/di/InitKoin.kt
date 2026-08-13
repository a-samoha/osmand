package com.samos.osmand.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(sharedModule, iosModule)
    }
}
