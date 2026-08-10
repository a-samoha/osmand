package com.osmand.myapplication.di

import com.osmand.myapplication.presentation.navigation.router.ComposeRouter
import com.osmand.myapplication.presentation.navigation.router.ComposeRouterImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {

    singleOf(::ComposeRouterImpl) bind ComposeRouter::class
}
