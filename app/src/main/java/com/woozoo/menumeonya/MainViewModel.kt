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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.woozoo.menumeonya.Constants.Companion.LATLNG_GN
import com.woozoo.menumeonya.Constants.Companion.LATLNG_YS
import com.woozoo.menumeonya.model.Restaurant
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Double.parseDouble

class MainViewModel(application: Application): AndroidViewModel(Application()) {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    lateinit var naverMap: NaverMap
    private var locationManager: LocationManager

    private var markerList: ArrayList<Marker> = ArrayList()

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

    private fun moveCameraCoord(latitude: Double, longitude: Double) {
        val coord = LatLng(latitude, longitude)
        naverMap.moveCamera(CameraUpdate.scrollTo(coord))
    }

    private suspend fun getRestaurantInfoAsync(location: String): Deferred<ArrayList<Restaurant>> {
        return viewModelScope.async {
            val restaurantInfoArray = ArrayList<Restaurant>()
            val db = Firebase.firestore
            val restaurantRef = db.collection("restaurants")
            val query = restaurantRef.whereArrayContainsAny("locationCategory", listOf(location))

            val result = query.get().await()
            val documents = result.documents

            for (document in documents) {
                val restaurant = document.toObject<Restaurant>()
                if (restaurant != null) restaurantInfoArray.add(restaurant)
            }

            restaurantInfoArray
        }
    }

    fun showLocationInfo(location: String) {
        viewModelScope.launch {
            when (location) {
                "강남" -> moveCameraCoord(LATLNG_GN.latitude, LATLNG_GN.longitude)
                "역삼" -> moveCameraCoord(LATLNG_YS.latitude, LATLNG_YS.longitude)
            }

            val restaurantInfo = getRestaurantInfoAsync(location).await()

            setMarkers(restaurantInfo)
        }
    }

    private fun setMarkers(restaurantInfo: ArrayList<Restaurant>) {
        if (restaurantInfo.size > 0) {
            // 마커 표시 초기화
            for (marker in markerList) {
                marker.map = null
            }
            markerList = ArrayList()

            // 마커 표시
            restaurantInfo.forEach { restaurant ->
                val latitude = parseDouble(restaurant.location.coord.latitude)
                val longitude = parseDouble(restaurant.location.coord.longitude)
                val latLng = LatLng(latitude, longitude)

                val marker = Marker()
                marker.position = latLng
                marker.captionText = restaurant.name

                markerList.add(marker)
            }

            markerList.forEach { marker ->
                marker.map = naverMap
            }
        }
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