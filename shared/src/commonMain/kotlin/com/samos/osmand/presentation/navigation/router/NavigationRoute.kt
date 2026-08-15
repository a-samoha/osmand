package com.samos.osmand.presentation.navigation.router

import kotlinx.serialization.Serializable

sealed interface NavigationRoute {

    @Serializable
    data object Splash : NavigationRoute

    @Serializable
    data class Download(val parentId: String? = null) : NavigationRoute
}
