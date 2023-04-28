package com.woozoo.menumonya.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.woozoo.menumonya.Application.Companion.context

class PermissionUtils {

    companion object {
        val ACCESS_FINE_LOCATION_REQUEST_CODE = 1000

        fun isLocationPermissionAllowed(): Boolean {
            return ContextCompat.checkSelfPermission(context(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED
        }

        fun requestLocationPermission(activity: Activity) {
            ActivityCompat.requestPermissions(activity,
                listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray(),
                ACCESS_FINE_LOCATION_REQUEST_CODE
            )
        }

        /**
         * GPS 또는 네트워크를 통한 위치 추적 권한 설정 여부 체크
         */
        fun isGpsPermissionAllowed(): Boolean {
            val lm: LocationManager = context().getSystemService(Context.LOCATION_SERVICE) as LocationManager

            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER)
        }
    }

}