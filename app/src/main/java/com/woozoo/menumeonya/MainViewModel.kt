package com.woozoo.menumeonya

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.woozoo.menumeonya.Application.Companion.context
import com.woozoo.menumeonya.Constants.Companion.LATLNG_GN
import com.woozoo.menumeonya.Constants.Companion.LATLNG_YS
import com.woozoo.menumeonya.Constants.Companion.MAP_DEFAULT_ZOOM
import com.woozoo.menumeonya.Constants.Companion.MAP_MIN_ZOOM
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

    private var restaurantInfoArray: ArrayList<Restaurant> = ArrayList()
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
    fun initializeMapView(mapView: MapView, activity: Activity) {
        mapView.getMapAsync {
            naverMap = it.apply {
                locationSource = FusedLocationSource(
                    activity,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                locationTrackingMode = LocationTrackingMode.NoFollow
                uiSettings.isLocationButtonEnabled = true
                minZoom = MAP_MIN_ZOOM
            }

            moveCameraCoord(LATLNG_GN.latitude, LATLNG_GN.longitude)
            showLocationInfo("강남")
        }
    }

    fun moveCameraToMarker(markerIndex: Int) {
        if (restaurantInfoArray.size > 0) {
            val latitude = parseDouble(restaurantInfoArray[markerIndex].location.coord.latitude)
            val longitude = parseDouble(restaurantInfoArray[markerIndex].location.coord.longitude)

            val coord = LatLng(latitude, longitude)

            naverMap.locationOverlay.apply {
                isVisible = false
                position = coord
            }

            // 마커 설정 초기화
            for (marker in markerList) {
                marker.width = Marker.SIZE_AUTO
                marker.height = Marker.SIZE_AUTO
                marker.zIndex = Marker.DEFAULT_GLOBAL_Z_INDEX
            }
            // 선택된 마커 확대
            markerList[markerIndex].apply {
                width = 100
                height = 130
                zIndex = Marker.DEFAULT_GLOBAL_Z_INDEX + 1
            }

            naverMap.setContentPadding(0, 0, 0, context().resources.getDimensionPixelOffset(R.dimen.restaurant_item_height))
            naverMap.moveCamera(CameraUpdate.scrollTo(coord).animate(CameraAnimation.Easing))
        }
    }

    private fun moveCameraCoord(latitude: Double, longitude: Double) {
        val coord = LatLng(latitude, longitude)
        val cameraUpdateParams = CameraUpdateParams().apply {
            scrollTo(coord)
            zoomTo(MAP_DEFAULT_ZOOM)
        }

        naverMap.moveCamera(CameraUpdate.withParams(cameraUpdateParams))
    }

    suspend fun getRestaurantInfoAsync(location: String): Deferred<ArrayList<Restaurant>> {
        return viewModelScope.async {
            val restaurantInfo = ArrayList<Restaurant>()
            val db = Firebase.firestore
            val restaurantRef = db.collection("restaurants")
            val query = restaurantRef.whereArrayContainsAny("locationCategory", listOf(location))

            val result = query.get().await()
            val documents = result.documents

            for (document in documents) {
                val restaurant = document.toObject<Restaurant>()
                if (restaurant != null) restaurantInfo.add(restaurant)
            }

            restaurantInfo
        }
    }

    fun getRestaurantInfo(location: String) {
        viewModelScope.launch {
            restaurantInfoArray = getRestaurantInfoAsync(location).await()

            showRestaurantView(restaurantInfoArray)
        }
    }

    fun showLocationInfo(location: String) {
        viewModelScope.launch {
            when (location) {
                "강남" -> moveCameraCoord(LATLNG_GN.latitude, LATLNG_GN.longitude)
                "역삼" -> moveCameraCoord(LATLNG_YS.latitude, LATLNG_YS.longitude)
            }

            restaurantInfoArray = getRestaurantInfoAsync(location).await()

            setMarkers(restaurantInfoArray)
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
                marker.isHideCollidedSymbols = true

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

    private fun showRestaurantView(data: ArrayList<Restaurant>) {
        event(Event.ShowRestaurantView(data))
    }

    sealed class Event {
        /**
         * MainActivity에 전달할 이벤트를 이곳에 정
         *
         * (ex) data class ShowToast(val text: String) : Event()
         */
        data class ShowToast(val text: String): Event()
        data class ShowRestaurantView(val data: ArrayList<Restaurant>): Event()
    }
}