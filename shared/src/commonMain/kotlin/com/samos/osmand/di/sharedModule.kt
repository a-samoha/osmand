package com.samos.osmand.di

import com.samos.osmand.presentation.navigation.router.ComposeRouter
import com.samos.osmand.presentation.navigation.router.ComposeRouterImpl
import com.samos.osmand.presentation.screen.download.viewmodel.DownloadViewModel
import com.samos.osmand.presentation.screen.splash.SplashViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {

    singleOf(::ComposeRouterImpl) bind ComposeRouter::class

    viewModelOf(::SplashViewModel)
    viewModelOf(::DownloadViewModel)
}
