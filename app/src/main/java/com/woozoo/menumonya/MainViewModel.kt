package com.woozoo.menumonya

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context.LOCATION_SERVICE
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.woozoo.menumonya.Application.Companion.context
import com.woozoo.menumonya.Constants.Companion.LATLNG_GN
import com.woozoo.menumonya.Constants.Companion.LATLNG_YS
import com.woozoo.menumonya.Constants.Companion.MAP_DEFAULT_ZOOM
import com.woozoo.menumonya.Constants.Companion.MAP_MIN_ZOOM
import com.woozoo.menumonya.model.Menu
import com.woozoo.menumonya.model.Restaurant
import com.woozoo.menumonya.util.DateUtils.Companion.getTodayDate
import com.woozoo.menumonya.util.LocationUtils.Companion.requestLocationUpdateOnce
import com.woozoo.menumonya.util.PermissionUtils.Companion.isGpsPermissionAllowed

import com.woozoo.menumonya.util.PermissionUtils.Companion.isLocationPermissionAllowed
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

    private var mRestaurantInfoArray: ArrayList<Restaurant> = ArrayList()
    private var markerList: ArrayList<Marker> = ArrayList()
    private var selectedLocation: String = ""

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
//                locationSource = FusedLocationSource(
//                    activity,
//                    LOCATION_PERMISSION_REQUEST_CODE
//                )
                locationTrackingMode = LocationTrackingMode.NoFollow
                uiSettings.apply {
                    isLocationButtonEnabled = false
                    isZoomControlEnabled = false
                }
                minZoom = MAP_MIN_ZOOM
            }

            moveCameraCoord(LATLNG_GN.latitude, LATLNG_GN.longitude)
            showLocationInfo("강남")
        }
    }

    fun moveCameraToMarker(markerIndex: Int) {
        if (mRestaurantInfoArray.size > 0) {
            val latitude = parseDouble(mRestaurantInfoArray[markerIndex].location.coord.latitude)
            val longitude = parseDouble(mRestaurantInfoArray[markerIndex].location.coord.longitude)

            val coord = LatLng(latitude, longitude)

            naverMap.locationOverlay.apply {
                isVisible = false
                position = coord
            }

            // 마커 설정 초기화
            for (marker in markerList) {
                marker.apply {
                    width = Marker.SIZE_AUTO
                    height = Marker.SIZE_AUTO
                    zIndex = Marker.DEFAULT_GLOBAL_Z_INDEX
                    icon = OverlayImage.fromResource(R.drawable.restaurant_marker)
                }
            }
            // 선택된 마커 아이콘 변경
            markerList[markerIndex].apply {
                icon = OverlayImage.fromResource(R.drawable.restaurant_marker_selected)
                zIndex = Marker.DEFAULT_GLOBAL_Z_INDEX + 1
            }

            naverMap.setContentPadding(0, 0, 0, context().resources.getDimensionPixelOffset(R.dimen.restaurant_item_height))
            naverMap.moveCamera(CameraUpdate.scrollTo(coord).animate(CameraAnimation.None))
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

    suspend fun getTodayRestaurantInfoAsync(location: String): Deferred<ArrayList<Restaurant>> {
        return viewModelScope.async {
            val restaurantInfo = ArrayList<Restaurant>()
            val db = Firebase.firestore
            val restaurantRef = db.collection("restaurants")
            val query = restaurantRef.whereArrayContainsAny("locationCategory", listOf(location))

            val result = query.get().await()
            val documents = result.documents

            for (document in documents) {
                val restaurant = document.toObject<Restaurant>()
                
                if (restaurant != null) {
                    // 메뉴 정보 조회
                    val menu = getMenuAsync(document.id)?.await()

                    val todayMenu = menu?.date?.get(getTodayDate())
                    if (todayMenu != null) restaurant.todayMenu =  todayMenu

                    restaurantInfo.add(restaurant)
                }
            }

            // locationCategoryOrder값으로 순서 재정렬(가까운 블록에 위치한 순서대로)
            for (restaurant in restaurantInfo) {
                restaurant.locationCategoryOrder.removeAll { !it.contains(location) }
            }
            restaurantInfo.sortBy { it.locationCategoryOrder[0] }

            restaurantInfo
        }
    }

    suspend fun getMenuAsync(restaurantId: String): Deferred<Menu>? {
        return viewModelScope.async {
            var menu = Menu()

            val db = Firebase.firestore
            val menuRef = db.collection("menus")
            val query = menuRef.whereEqualTo("restaurantId", restaurantId)

            val result = query.get().await()
            val documents = result.documents

            if (documents.size > 0) {
                menu = documents[0].toObject<Menu>()!!
            }

            menu
        }
    }

    /**
     * 하단의 식당 정보 가로 스크롤 뷰를 표시함.
     * - (중요) 지도에 마커를 표시하기 위한 식당 정보를 이미 fetch하였다는 전제 하에 작동함.
     */
    fun showLocationViewPager(location: String, markerIndex: Int = -1) {
        if (mRestaurantInfoArray.size > 0) {
            showRestaurantView(mRestaurantInfoArray, markerIndex)
        }
    }

    fun showLocationInfo(location: String) {
        viewModelScope.launch {
            selectedLocation = location

            when (selectedLocation) {
                "강남" -> moveCameraCoord(LATLNG_GN.latitude, LATLNG_GN.longitude)
                "역삼" -> moveCameraCoord(LATLNG_YS.latitude, LATLNG_YS.longitude)
            }

            mRestaurantInfoArray = getTodayRestaurantInfoAsync(selectedLocation).await() // TODO: 정렬 안돼있음

            setMarkers(mRestaurantInfoArray)
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
            restaurantInfo.forEachIndexed { index, restaurant ->
                val latitude = parseDouble(restaurant.location.coord.latitude)
                val longitude = parseDouble(restaurant.location.coord.longitude)
                val latLng = LatLng(latitude, longitude)

                val marker = Marker().apply {
                    position = latLng
                    captionText = restaurant.name
                    isHideCollidedSymbols = true
                    icon = OverlayImage.fromResource(R.drawable.restaurant_marker)
                    setOnClickListener {
                        onMarkerClicked(index, selectedLocation)
                        true
                    }
                }

                markerList.add(marker)
            }

            markerList.forEach { marker ->
                marker.map = naverMap
            }
        }
    }

    /**
     * - 내 위치를 획득하기 위해서는 두가지 상태여야 함.
     *   (1) '기기'의 GPS 기능이 켜져있는 상태
     *   (2) '앱'의 위치 권한이 허용되어있는 상태
     *
     * - 두 상태를 체크하고, 상태에 따라 사용자에게 허용/켜짐을 요청함.
     */
    fun getCurrentLocation(activity: Activity) {
        if (!isGpsPermissionAllowed()) {
            showGpsPermissionAlert()
        } else {
            if (!isLocationPermissionAllowed()) {
                requestLocationPermission()
            } else {
                requestLocationUpdateOnce(
                    locationManager,
                    LocationListener { location ->
                        // 내 위치로 카메라 이동, 내 위치 표시
                        moveCameraCoord(location.latitude, location.longitude)
                        naverMap.apply {
                            locationSource = FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE)
                            locationTrackingMode = LocationTrackingMode.Follow
                        }
                    })
            }
        }
    }

    private fun showToast(text: String) {
        event(Event.ShowToast(text))
    }

    private fun showRestaurantView(data: ArrayList<Restaurant>, markerIndex: Int) {
        event(Event.ShowRestaurantView(data, markerIndex))
    }

    private fun onMarkerClicked(markerIndex: Int, location: String) {
        event(Event.OnMarkerClicked(markerIndex, location))
    }

    private fun requestLocationPermission() {
        event(Event.RequestLocationPermission(""))
    }

    private fun showGpsPermissionAlert() {
        event(Event.ShowGpsPermissionAlert(""))
    }

    sealed class Event {
        /**
         * MainActivity에 전달할 이벤트를 이곳에 정
         *
         * (ex) data class ShowToast(val text: String) : Event()
         */
        data class ShowToast(val text: String): Event()
        data class ShowRestaurantView(val data: ArrayList<Restaurant>, val markerIndex: Int): Event()
        data class OnMarkerClicked(val markerIndex: Int, val location: String): Event()
        data class RequestLocationPermission(val data: String): Event()
        data class ShowGpsPermissionAlert(val data: String): Event()
    }
}