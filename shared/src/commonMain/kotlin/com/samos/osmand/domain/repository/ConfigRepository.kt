package com.samos.osmand.domain.repository

import com.samos.osmand.presentation.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {

    fun getAppThemeMode(): Flow<AppThemeMode>
    suspend fun setAppThemeMode(mode: AppThemeMode)
}
