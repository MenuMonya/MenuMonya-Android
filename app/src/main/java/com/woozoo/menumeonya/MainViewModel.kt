package com.woozoo.menumeonya

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(Application()) {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    private lateinit var naverMap: NaverMap
    private var locationManager: LocationManager

    init {
        locationManager = application.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private fun event(event: Event) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    @SuppressLint("MissingPermission")
    fun initializeMapView(mapView: MapView, activity: Activity, listener: LocationListener) {
        mapView.getMapAsync {
            naverMap = it.apply {
                locationSource = FusedLocationSource(
                    activity,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                locationTrackingMode = LocationTrackingMode.NoFollow
                uiSettings.isLocationButtonEnabled = true
            }

            val currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (currentLocation != null) {
                moveCamera(currentLocation)
            } else {
                showToast("위치 정보를 불러오지 못했어요")
            }
        }
    }

    fun moveCamera(location: Location) {
        val coord = LatLng(location)

        naverMap.locationOverlay.apply {
            isVisible = true
            position = coord
            bearing = location.bearing
        }

        naverMap.moveCamera(CameraUpdate.scrollTo(coord))
    }

    private fun showToast(text: String) {
        event(Event.ShowToast(text))
    }

    sealed class Event {
        /**
         * MainActivity에 전달할 이벤트를 이곳에 정
         *
         * (ex) data class ShowToast(val text: String) : Event()
         */
        data class ShowToast(val text: String): Event()
    }
}