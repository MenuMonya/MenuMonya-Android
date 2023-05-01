package com.woozoo.menumonya.repository

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.woozoo.menumonya.BuildConfig
import com.woozoo.menumonya.Constants.Companion.REMOTE_CONFIG_FETCH_INTERVAL
import com.woozoo.menumonya.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RemoteConfigRepository {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = REMOTE_CONFIG_FETCH_INTERVAL
    }

    init {
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("zzanzu", "remote config fetch successful")
            } else {
                Log.d("zzanzu", "remote config fetch failure")
            }
        }
    }

    suspend fun getRestaurantsCollectionNameConfig() = withContext(Dispatchers.IO) {
        if (BuildConfig.DEBUG) {
            remoteConfig.getString("RESTAURANT_COLLECTION_DEV")
        } else {
            remoteConfig.getString("RESTAURANT_COLLECTION_PROD")
        }
    }

    suspend fun getMenuCollectionNameConfig() = withContext(Dispatchers.IO) {
        if (BuildConfig.DEBUG) {
            remoteConfig.getString("MENU_COLLECTION_DEV")
        } else {
            remoteConfig.getString("MENU_COLLECTION_PROD")
        }
    }

    fun getFeedbackUrlConfig(): String {
        return if (BuildConfig.DEBUG) {
            remoteConfig.getString("FEEDBACK_URL_DEV")
        } else {
            remoteConfig.getString("FEEDBACK_URL_PROD")
        }
    }
}