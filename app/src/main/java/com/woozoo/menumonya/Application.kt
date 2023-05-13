package com.woozoo.menumonya

import android.app.Application
import android.content.Context
import com.woozoo.menumonya.repository.RemoteConfigRepositoryImpl
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application: Application() {
    @Inject lateinit var remoteConfigRepository: RemoteConfigRepositoryImpl

    init {
        instance = this
    }

    companion object {
        private var instance: Application? = null

        fun context(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

        remoteConfigRepository.initializeRemoteConfig()
    }
}