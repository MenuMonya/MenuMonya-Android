package com.woozoo.menumeonya.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtils {

    companion object {
        private val ACCESS_FINE_LOCATION = 1000

        fun checkLocationPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED
        }

        fun requestLocationPermission(activity: Activity) {
            ActivityCompat.requestPermissions(activity,
                listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray(),
                ACCESS_FINE_LOCATION
            )
        }
    }

}