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

    /**
     * Firebase Remote Config 관련 초기화 작업
     * - Application.kt의 onCreate()에서 호출함.
     * - 해당 로직이 실행되지 않은 상태에서 getString()을 호출할 경우 오류가 발생함.
     *   - setDefaultAsnyc()가 호출되지 않았기 때문.
     * - TODO : 추후 스플래시 화면이 생긴다면 호출 타이밍을 변경해야 함.
     */
    fun initializeRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = REMOTE_CONFIG_FETCH_INTERVAL
        }
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
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        if (BuildConfig.DEBUG) {
            remoteConfig.getString("RESTAURANT_COLLECTION_DEV")
        } else {
            remoteConfig.getString("RESTAURANT_COLLECTION_PROD")
        }
    }

    suspend fun getMenuCollectionNameConfig() = withContext(Dispatchers.IO) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        if (BuildConfig.DEBUG) {
            remoteConfig.getString("MENU_COLLECTION_DEV")
        } else {
            remoteConfig.getString("MENU_COLLECTION_PROD")
        }
    }

    fun getFeedbackUrlConfig(): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        return if (BuildConfig.DEBUG) {
            remoteConfig.getString("FEEDBACK_URL_DEV")
        } else {
            remoteConfig.getString("FEEDBACK_URL_PROD")
        }
    }

    fun getReportMenuUrlConfig(): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        return remoteConfig.getString("REPORT_MENU_URL")
    }
}