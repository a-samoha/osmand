package com.samos.osmand.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samos.osmand.presentation.navigation.router.ComposeRouter
import com.samos.osmand.presentation.navigation.router.NavigationEffect
import com.samos.osmand.presentation.navigation.router.NavigationRoute
import com.samos.osmand.presentation.screen.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun MainNavHost(
    navController: NavHostController = rememberNavController(),
    router: ComposeRouter = koinInject(),
) {
    LaunchedEffect(router, navController) {
        router.observe().collect { effect ->
            when (effect) {
                is NavigationEffect.NavigateBack -> {
                    navController.popBackStack()
                }
                is NavigationEffect.NavigateTo -> {
                    navController.navigate(effect.route)
                }
                is NavigationEffect.ReplaceScreen -> {
                    navController.currentDestination?.route?.let { currentRouteStr ->
                        navController.navigate(effect.route) {
                            popUpTo(currentRouteStr) { inclusive = true }
                        }
                    } ?: navController.navigate(effect.route)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Splash
    ) {
        composable<NavigationRoute.Splash> {
            SplashScreen()
        }

        composable<NavigationRoute.Download> {

        }
    }
}
