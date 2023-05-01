package com.woozoo.menumonya.repository

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.woozoo.menumonya.BuildConfig
import com.woozoo.menumonya.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteConfigRepository {
    private val instance = FirebaseRemoteConfig.getInstance()

    private val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 60 * 1 // 1분마다 업데이트 함
    }

    init {
        instance.setDefaultsAsync(R.xml.remote_config_defaults)
        instance.setConfigSettingsAsync(configSettings)
        instance.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("zzanzu", "remote config fetch successful")
            } else {
                Log.d("zzanzu", "remote config fetch failure")
            }
        }
    }

    suspend fun getRestaurantsCollectionName() = withContext(Dispatchers.IO) {
        if (BuildConfig.DEBUG) {
            instance.getString("RESTAURANT_COLLECTION_DEV")
        } else {
            instance.getString("RESTAURANT_COLLECTION_PROD")
        }
    }

    suspend fun getMenuCollectionName() = withContext(Dispatchers.IO) {
        if (BuildConfig.DEBUG) {
            instance.getString("MENU_COLLECTION_DEV")
        } else {
            instance.getString("MENU_COLLECTION_PROD")
        }
    }
}