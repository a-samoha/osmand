package com.samos.osmand.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider


private val lightScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    inversePrimary = InversePrimaryLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

private val darkScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    inversePrimary = InversePrimaryDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
)

@Composable
fun OsmandAppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) darkScheme else lightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography(), // Default Typo
    ) {
        CompositionLocalProvider(
            value = LocalCustomTypo provides getCustomTypo(),
            content = content
        )
    }
}
