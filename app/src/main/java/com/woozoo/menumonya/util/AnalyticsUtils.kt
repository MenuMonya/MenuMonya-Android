package com.woozoo.menumonya.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsUtils @Inject constructor(
    @ApplicationContext val context: Context,
    private val firebaseAnalytics: FirebaseAnalytics
) {

    companion object {
        const val CONTENT_TYPE_MARKER = "marker"
        const val CONTENT_TYPE_LIST = "list"
        const val CONTENT_TYPE_REGION = "region"
        const val CONTENT_TYPE_REPORT_BUTTON = "report_button"
        const val CONTENT_TYPE_REPORT_REGION_BUTTON = "report_region_button"
    }

    fun saveContentSelectionLog(contentType: String, content: String) {
        val param = Bundle().apply {
            this.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            this.putString(FirebaseAnalytics.Param.CONTENT, content)
        }

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, param)
    }
}