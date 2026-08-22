package com.samos.osmand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samos.osmand.domain.repository.ConfigRepository
import com.samos.osmand.presentation.base.component.ApplySystemBarColors
import com.samos.osmand.presentation.theme.AppThemeMode
import org.koin.android.ext.android.inject
import kotlin.getValue

class MainActivity : ComponentActivity() {

    private val configRepository: ConfigRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val activity = LocalActivity.current
            val systemDark = isSystemInDarkTheme()
            val themeMode by configRepository.getAppThemeMode()
                .collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)

            val darkTheme = when (themeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> systemDark
            }
            activity?.let {
                ApplySystemBarColors(
                    activity = it,
                    darkTheme = darkTheme,
                )
            }
            SharedCompose()
        }
    }
}
