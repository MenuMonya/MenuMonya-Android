package com.woozoo.menumonya.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
): DataStoreRepository {
    override suspend fun getLastSelectedRegion() = withContext(Dispatchers.IO) {
        val lastSelectedRegionKey = stringPreferencesKey("key_last_selected_region")
        val preferences = dataStore.data.first()
        preferences[lastSelectedRegionKey] ?: "강남"
    }

    override suspend fun setLastSelectedRegion(region: String) = withContext(Dispatchers.IO) {
        val lastSelectedRegionKey = stringPreferencesKey("key_last_selected_region")
        dataStore.edit {
            it[lastSelectedRegionKey] = region
        }
    }


    override suspend fun getIsFirstOpen() = withContext(Dispatchers.IO) {
        val isFirstOpenKey = booleanPreferencesKey("key_is_first_open")
        val preferences = dataStore.data.first()
        preferences[isFirstOpenKey] ?: true
    }

    override suspend fun setIsFirstOpen(isFirstOpen: Boolean) = withContext(Dispatchers.IO) {
        val isFirstOpenKey = booleanPreferencesKey("key_is_first_open")
        dataStore.edit {
            it[isFirstOpenKey] = isFirstOpen
        }
    }
}