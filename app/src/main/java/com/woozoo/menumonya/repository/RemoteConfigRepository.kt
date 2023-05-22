package com.woozoo.menumonya.repository

interface RemoteConfigRepository {

    fun initializeRemoteConfig()

    suspend fun getRestaurantsCollectionNameConfig(): String

    suspend fun getMenuCollectionNameConfig(): String

    fun getRegionReportUrlConfig(): String

    fun getReportMenuUrlConfig(): String

    fun getLatestAppVersionConfig(): Long
}