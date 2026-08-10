package com.osmand.myapplication.presentation.navigation.router

sealed interface NavigationEffect {

    data object NavigateBack : NavigationEffect

    data class ReplaceScreen(val route: NavigationRoute) : NavigationEffect

    data class NavigateTo(val route: NavigationRoute) : NavigationEffect
}
