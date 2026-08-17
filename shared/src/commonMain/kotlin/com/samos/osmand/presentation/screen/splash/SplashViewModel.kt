package com.samos.osmand.presentation.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samos.osmand.presentation.navigation.router.ComposeRouter
import com.samos.osmand.presentation.navigation.router.NavigationRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SplashViewModel(
    private val router: ComposeRouter,
) : ViewModel() {

    init {
        viewModelScope.launch {
            delay(1100.milliseconds)
            router.replaceScreen(NavigationRoute.Download())
        }
    }
}
