package com.samos.osmand.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.Preferences
import com.samos.osmand.domain.repository.ConfigRepository
import com.samos.osmand.presentation.theme.AppThemeMode

class ConfigRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : ConfigRepository {

    private val KEY_APP_THEME_MODE = stringPreferencesKey("key_app_theme_mode")

    override fun getAppThemeMode(): Flow<AppThemeMode> = dataStore.data
        .map { preferences ->
            val name = preferences[KEY_APP_THEME_MODE] ?: AppThemeMode.SYSTEM.name
            runCatching { AppThemeMode.valueOf(name) }.getOrDefault(AppThemeMode.SYSTEM)
        }

    override suspend fun setAppThemeMode(mode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_THEME_MODE] = mode.name
        }
    }
}
