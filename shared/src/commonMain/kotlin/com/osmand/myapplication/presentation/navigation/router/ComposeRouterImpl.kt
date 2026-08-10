package com.osmand.myapplication.presentation.navigation.router

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class ComposeRouterImpl : ComposeRouter {

    private val routerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _navEffect = MutableSharedFlow<NavigationEffect>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun observe(): SharedFlow<NavigationEffect> = _navEffect.asSharedFlow()

    override fun navigateBack() {
        routerScope.launch {
            _navEffect.emit(NavigationEffect.NavigateBack)
        }
    }

    override fun replaceScreen(route: NavigationRoute) {
        routerScope.launch {
            _navEffect.emit(NavigationEffect.ReplaceScreen(route))
        }
    }

    override fun navigateTo(route: NavigationRoute) {
        routerScope.launch {
            _navEffect.emit(NavigationEffect.NavigateTo(route))
        }
    }
}
