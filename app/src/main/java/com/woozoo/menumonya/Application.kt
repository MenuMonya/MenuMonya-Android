package com.woozoo.menumonya

import android.app.Application
import android.content.Context
import com.woozoo.menumonya.repository.RemoteConfigRepository.initializeRemoteConfig

class Application: Application() {

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

        initializeRemoteConfig()
    }
}