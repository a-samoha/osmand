package com.samos.osmand.presentation.base.component

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
@Suppress("DEPRECATION")
fun ApplySystemBarColors(
    activity: Activity,
    darkTheme: Boolean,
    resetToTransparentOnDispose: Boolean = false,
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent,
) {
    val view = LocalView.current
    val window = activity.window
    val statusBarArgb = statusBarColor.toArgb()
    val navigationBarArgb = navigationBarColor.toArgb()
    val transparentArgb = Color.Transparent.toArgb()
    val useDarkSystemBarIcons = !darkTheme

    DisposableEffect(window, view, resetToTransparentOnDispose) {
        val insetsController = WindowInsetsControllerCompat(window, view)
        val previousLightStatusBars = insetsController.isAppearanceLightStatusBars
        val previousLightNavigationBars = insetsController.isAppearanceLightNavigationBars

        onDispose {
            if (resetToTransparentOnDispose) {
                window.statusBarColor = transparentArgb
                window.navigationBarColor = transparentArgb
                insetsController.isAppearanceLightStatusBars = previousLightStatusBars
                insetsController.isAppearanceLightNavigationBars = previousLightNavigationBars
            }
        }
    }

    SideEffect {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = statusBarArgb
        window.navigationBarColor = navigationBarArgb
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        WindowInsetsControllerCompat(window, view).run {
            isAppearanceLightStatusBars = useDarkSystemBarIcons
            isAppearanceLightNavigationBars = useDarkSystemBarIcons
        }
    }
}

@Composable
// Required to keep the bottom sheet dialog navigation bar stable on older Android versions.
@Suppress("DEPRECATION")
fun ApplyBottomSheetNavigationBarColor(
    darkTheme: Boolean,
    navigationBarColor: Color = Color.Transparent,
) {
    val view = LocalView.current
    val context = LocalContext.current
    val navigationBarArgb = navigationBarColor.toArgb()
    val useDarkNavigationBarIcons = !darkTheme

    DisposableEffect(
        view,
        navigationBarArgb,
        useDarkNavigationBarIcons,
    ) {
        val window = when {
            view is DialogWindowProvider -> view.window
            view.parent is DialogWindowProvider -> (view.parent as DialogWindowProvider).window
            context is Activity -> context.window
            else -> null
        }

        if (window == null) {
            return@DisposableEffect onDispose {}
        }

        val insetsController = WindowInsetsControllerCompat(window, view)

        val previousNavigationBarColor = window.navigationBarColor
        val previousLightNavigationBars = insetsController.isAppearanceLightNavigationBars
        val previousContrastEnforced =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced
            } else {
                null
            }

        window.navigationBarColor = navigationBarArgb

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        insetsController.isAppearanceLightNavigationBars =
            useDarkNavigationBarIcons

        onDispose {
            window.navigationBarColor = previousNavigationBarColor
            insetsController.isAppearanceLightNavigationBars = previousLightNavigationBars
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && previousContrastEnforced != null) {
                window.isNavigationBarContrastEnforced = previousContrastEnforced
            }
        }
    }
}