package com.woozoo.menumeonya

import android.app.Application
import android.content.Context

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
}