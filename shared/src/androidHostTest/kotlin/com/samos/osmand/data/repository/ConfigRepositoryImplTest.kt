package com.samos.osmand.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.samos.osmand.presentation.theme.AppThemeMode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConfigRepositoryImplTest {

    private val KEY_APP_THEME_MODE = stringPreferencesKey("key_app_theme_mode")

    @Test
    fun `getAppThemeMode returns stored mode when value is valid`() = runTest {
        val preferences = mockk<Preferences>()
        every { preferences[KEY_APP_THEME_MODE] } returns AppThemeMode.DARK.name
        val dataStore = mockk<DataStore<Preferences>>()
        every { dataStore.data } returns flowOf(preferences)
        val repository = ConfigRepositoryImpl(dataStore)

        val result = repository.getAppThemeMode().first()

        assertEquals(AppThemeMode.DARK, result)
    }

    @Test
    fun `getAppThemeMode returns SYSTEM when key is missing`() = runTest {
        val preferences = mockk<Preferences>()
        every { preferences[KEY_APP_THEME_MODE] } returns null
        val dataStore = mockk<DataStore<Preferences>>()
        every { dataStore.data } returns flowOf(preferences)
        val repository = ConfigRepositoryImpl(dataStore)

        val result = repository.getAppThemeMode().first()

        assertEquals(AppThemeMode.SYSTEM, result)
    }

    @Test
    fun `getAppThemeMode returns SYSTEM when stored value is invalid`() = runTest {
        val preferences = mockk<Preferences>()
        every { preferences[KEY_APP_THEME_MODE] } returns "NOT_A_REAL_MODE"
        val dataStore = mockk<DataStore<Preferences>>()
        every { dataStore.data } returns flowOf(preferences)
        val repository = ConfigRepositoryImpl(dataStore)

        val result = repository.getAppThemeMode().first()

        assertEquals(AppThemeMode.SYSTEM, result)
    }

    @Test
    fun `getAppThemeMode propagates exception when underlying flow fails`() = runTest {
        val dataStore = mockk<DataStore<Preferences>>()
        every { dataStore.data } returns flow { throw IllegalStateException("read error") }
        val repository = ConfigRepositoryImpl(dataStore)

        assertFailsWith<IllegalStateException> {
            repository.getAppThemeMode().first()
        }
    }

    @Test
    fun `setAppThemeMode writes the given mode name`() = runTest {
        var storedPreferences: Preferences? = null
        val dataStore = mockk<DataStore<Preferences>>()
        coEvery { dataStore.updateData(any()) } coAnswers {
            val transform = firstArg<suspend (Preferences) -> Preferences>()
            transform(emptyPreferences()).also { storedPreferences = it }
        }

        val repository = ConfigRepositoryImpl(dataStore)
        repository.setAppThemeMode(AppThemeMode.LIGHT)

        assertEquals(AppThemeMode.LIGHT.name, storedPreferences?.get(KEY_APP_THEME_MODE))
    }

    @Test
    fun `setAppThemeMode writes DARK mode name`() = runTest {
        var storedPreferences: Preferences? = null
        val dataStore = mockk<DataStore<Preferences>>()
        coEvery { dataStore.updateData(any()) } coAnswers {
            val transform = firstArg<suspend (Preferences) -> Preferences>()
            transform(emptyPreferences()).also { storedPreferences = it }
        }

        val repository = ConfigRepositoryImpl(dataStore)
        repository.setAppThemeMode(AppThemeMode.DARK)

        assertEquals(AppThemeMode.DARK.name, storedPreferences?.get(KEY_APP_THEME_MODE))
    }

    @Test
    fun `setAppThemeMode propagates exception thrown by dataStore updateData`() = runTest {
        val dataStore = mockk<DataStore<Preferences>>()
        coEvery { dataStore.updateData(any()) } throws IllegalStateException("write error")
        val repository = ConfigRepositoryImpl(dataStore)

        assertFailsWith<IllegalStateException> {
            repository.setAppThemeMode(AppThemeMode.LIGHT)
        }
    }

    @Test
    fun `setAppThemeMode propagates exception for a different mode value`() = runTest {
        val dataStore = mockk<DataStore<Preferences>>()
        coEvery { dataStore.updateData(any()) } throws IllegalStateException("write error")
        val repository = ConfigRepositoryImpl(dataStore)

        assertFailsWith<IllegalStateException> {
            repository.setAppThemeMode(AppThemeMode.SYSTEM)
        }
    }
}
