package com.samos.osmand

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samos.osmand.domain.repository.ConfigRepository
import com.samos.osmand.presentation.navigation.MainNavHost
import com.samos.osmand.presentation.theme.AppThemeMode
import com.samos.osmand.presentation.theme.OsmandAppTheme
import org.koin.compose.koinInject

@Composable
fun SharedCompose() {
    val configRepository: ConfigRepository = koinInject()

    val themeMode by configRepository.getAppThemeMode()
        .collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)
    val systemDark = isSystemInDarkTheme()

    val darkTheme = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> systemDark
    }

    OsmandAppTheme(darkTheme) {
        MainNavHost()
    }
}
