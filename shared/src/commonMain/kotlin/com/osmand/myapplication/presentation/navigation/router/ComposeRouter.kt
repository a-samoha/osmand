package com.osmand.myapplication.presentation.navigation.router

import kotlinx.coroutines.flow.SharedFlow

interface ComposeRouter {

    fun observe(): SharedFlow<NavigationEffect>

    fun navigateBack()

    fun replaceScreen(route: NavigationRoute)

    fun navigateTo(route: NavigationRoute)
}
