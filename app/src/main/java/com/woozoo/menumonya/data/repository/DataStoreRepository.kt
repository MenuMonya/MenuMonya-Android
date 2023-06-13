package com.woozoo.menumonya.data.repository

import androidx.datastore.preferences.core.Preferences

interface DataStoreRepository {
    suspend fun getLastSelectedRegion(): String

    suspend fun setLastSelectedRegion(region: String): Preferences

    suspend fun getIsFirstOpen(): Boolean

    suspend fun setIsFirstOpen(isFirstOpen: Boolean): Preferences
}