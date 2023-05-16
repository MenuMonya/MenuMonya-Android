package com.woozoo.menumonya.repository

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.woozoo.menumonya.BuildConfig
import com.woozoo.menumonya.Constants.Companion.REMOTE_CONFIG_FETCH_INTERVAL
import com.woozoo.menumonya.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigRepositoryImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
    ) : RemoteConfigRepository {

    /**
     * Firebase Remote Config 관련 초기화 작업
     * - Application.kt의 onCreate()에서 호출함.
     * - 해당 로직이 실행되지 않은 상태에서 getString()을 호출할 경우 오류가 발생함.
     *   - setDefaultAsnyc()가 호출되지 않았기 때문.
     * - TODO : 추후 스플래시 화면이 생긴다면 호출 타이밍을 변경해야 함.
     */
    override fun initializeRemoteConfig() {
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

    override suspend fun getRestaurantsCollectionNameConfig() = withContext(Dispatchers.IO) {
        if (BuildConfig.DEBUG) {
            remoteConfig.getString("RESTAURANT_COLLECTION_DEV")
        } else {
            remoteConfig.getString("RESTAURANT_COLLECTION_PROD")
        }
    }

    override suspend fun getMenuCollectionNameConfig() = withContext(Dispatchers.IO) {
        if (BuildConfig.DEBUG) {
            remoteConfig.getString("MENU_COLLECTION_DEV")
        } else {
            remoteConfig.getString("MENU_COLLECTION_PROD")
        }
    }

    override fun getFeedbackUrlConfig(): String {
        return if (BuildConfig.DEBUG) {
            remoteConfig.getString("FEEDBACK_URL_DEV")
        } else {
            remoteConfig.getString("FEEDBACK_URL_PROD")
        }
    }

    override fun getReportMenuUrlConfig(): String {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        return remoteConfig.getString("REPORT_MENU_URL")
    }

    override fun getLatestAppVersionConfig(): Long {
        return if (BuildConfig.DEBUG) {
            remoteConfig.getLong("LATEST_APP_VERSION_AOS_DEV")
        } else {
            remoteConfig.getLong("LATEST_APP_VERSION_AOS_PROD")
        }
    }
}