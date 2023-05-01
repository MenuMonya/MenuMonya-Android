package com.woozoo.menumonya

import android.app.Application
import android.content.Context
import com.woozoo.menumonya.repository.FireStoreRepository
import com.woozoo.menumonya.repository.RemoteConfigRepository

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

        // 종속 관계 때문에 초기화 순서 중요함
        FireStoreRepository.initialize()
    }
}