package com.woozoo.menumonya.repository

import androidx.datastore.preferences.core.Preferences

interface DataStoreRepository {
    suspend fun getLastSelectedRegion(): String

    suspend fun setLastSelectedRegion(region: String): Preferences

    suspend fun isFirstOpen(): Boolean
}