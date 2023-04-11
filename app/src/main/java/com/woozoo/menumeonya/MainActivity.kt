package com.woozoo.menumeonya

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import net.daum.mf.map.api.MapView.CurrentLocationEventListener


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var currentLocationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentLocationButton = findViewById(R.id.my_location_button)
        currentLocationButton.setOnClickListener {
            mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading  //이 부분

            val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                val userNowLocation: Location? =
                    lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (userNowLocation != null) {
                    val uLatitude = userNowLocation.latitude
                    val uLongitude = userNowLocation.longitude
                    val uNowPosition = MapPoint.mapPointWithGeoCoord(uLatitude, uLongitude)
                    mapView.setMapCenterPoint(uNowPosition, true)
                }

            }
        }

        // Kakao 지도 SDK 초기화
        mapView = MapView(this)
        val mapViewContainer = findViewById<View>(R.id.map_view_layout) as ViewGroup
        mapViewContainer.addView(mapView)

        setMapViewListener()
    }

    private fun setMapViewListener() {
        mapView.setCurrentLocationEventListener(object: CurrentLocationEventListener {
            override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
                Log.d("zzanzu", "onCurrentLocationUpdate: $p1")
                mapView.setMapCenterPoint(p1, true)
            }

            override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
                Log.d("zzanzu", "onCurrentLocationDeviceHeadingUpdate: ")
            }

            override fun onCurrentLocationUpdateFailed(p0: MapView?) {
                Log.d("zzanzu", "onCurrentLocationUpdateFailed: ")
            }

            override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
                Log.d("zzanzu", "onCurrentLocationUpdateCancelled: ")
            }

        })
    }
}