package com.woozoo.menumonya.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager

class LocationUtils {

    companion object {

        /**
         * 기기의 위치를 파악하는 방법에는 두가지가 있음
         * (1) GPS : GPS 기반으로 위치를 파악함. 실내에서는 작동되지 않음.
         * (2) Network : 연결된 네트워크 기반으로 위치를 파악함.
         */
        fun checkLocationServiceStatus(context: Context): Boolean {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            return isGpsEnabled || networkEnabled
        }

        /**
         * 내 위치를 한 번만 조회함.
         * @param listener : 위치 조회시 수행되는 로직
         */
        @SuppressLint("MissingPermission")
        fun requestLocationUpdateOnce(locationManager: LocationManager, listener: LocationListener) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                10f,
                listener.apply {
                    locationManager.removeUpdates(this) // 위치 업데이트를 반복하지 않도록 리스너 제거
                }
            )
        }
    }

}