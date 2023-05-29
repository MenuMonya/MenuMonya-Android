package com.woozoo.menumonya

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context.LOCATION_SERVICE
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.woozoo.menumonya.model.Restaurant
import com.woozoo.menumonya.repository.FireStoreRepository
import com.woozoo.menumonya.repository.RemoteConfigRepository
import com.woozoo.menumonya.util.AnalyticsUtils
import com.woozoo.menumonya.util.AnalyticsUtils.Companion.CONTENT_TYPE_LOCATION
import com.woozoo.menumonya.util.AnalyticsUtils.Companion.CONTENT_TYPE_MARKER
import com.woozoo.menumonya.util.AnalyticsUtils.Companion.CONTENT_TYPE_VIEW_PAGER
import com.woozoo.menumonya.util.DateUtils
import com.woozoo.menumonya.util.LocationUtils.Companion.requestLocationUpdateOnce
import com.woozoo.menumonya.util.PermissionUtils.Companion.isGpsPermissionAllowed
import com.woozoo.menumonya.util.PermissionUtils.Companion.isLocationPermissionAllowed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.lang.Double.parseDouble
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val fireStoreRepository: FireStoreRepository,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val analyticsUtils: AnalyticsUtils
): AndroidViewModel(Application()) {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    lateinit var naverMap: NaverMap
    private var locationManager: LocationManager

    private var mRestaurantInfoArray: ArrayList<Restaurant> = ArrayList()
    private var markerList: ArrayList<Marker> = ArrayList()
    private var selectedLocation: String = ""
    private var isInitialized: Boolean = false

    init {
        locationManager = application.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private fun event(event: Event) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    @SuppressLint("MissingPermission")
    fun initializeMapView(mapView: MapView) {
        mapView.getMapAsync {
            naverMap = it.apply {
                locationTrackingMode = LocationTrackingMode.NoFollow
                uiSettings.apply {
                    isLocationButtonEnabled = false
                    isZoomControlEnabled = false
                }
                minZoom = MAP_MIN_ZOOM
            }

            moveCameraToCoord(LATLNG_GN.latitude, LATLNG_GN.longitude)
            showLocationInfo("강남")

            isInitialized = true
        }
    }

    fun moveCameraToMarker(markerIndex: Int) {
        if (mRestaurantInfoArray.size > 0) {
            val selectedRestaurant = mRestaurantInfoArray[markerIndex]

            val latitude = parseDouble(selectedRestaurant.location.coord.latitude)
            val longitude = parseDouble(selectedRestaurant.location.coord.longitude)

            val coord = LatLng(latitude, longitude)

            naverMap.locationOverlay.apply {
                isVisible = false
                position = coord
            }

            // 마커 설정 초기화
            markerList.forEachIndexed { index, marker ->
                marker.apply {
                    width = Marker.SIZE_AUTO
                    height = Marker.SIZE_AUTO
                    zIndex = if (mRestaurantInfoArray[index].todayMenu.date == DateUtils.getTodayDate()) {
                        Marker.DEFAULT_GLOBAL_Z_INDEX + 1
                    } else {
                        Marker.DEFAULT_GLOBAL_Z_INDEX
                    }
                    icon = if (mRestaurantInfoArray[index].todayMenu.date == DateUtils.getTodayDate()) {
                        OverlayImage.fromResource(R.drawable.restaurant_marker)
                    } else {
                        OverlayImage.fromResource(R.drawable.restaurant_marker_menu_not_added)
                    }
                }
            }
            // 선택된 마커 아이콘 변경
            markerList[markerIndex].apply {
                icon = if (selectedRestaurant.todayMenu.date == DateUtils.getTodayDate()) {
                    OverlayImage.fromResource(R.drawable.restaurant_marker_selected)
                } else {
                    OverlayImage.fromResource(R.drawable.restaurant_marker_selected_menu_not_added)
                }
                zIndex = Marker.DEFAULT_GLOBAL_Z_INDEX + 1
            }

            naverMap.setContentPadding(0, 0, 0, context().resources.getDimensionPixelOffset(R.dimen.restaurant_item_height))
            naverMap.moveCamera(CameraUpdate.scrollTo(coord).animate(CameraAnimation.None))

            analyticsUtils.saveContentSelectionLog(CONTENT_TYPE_VIEW_PAGER, selectedRestaurant.name)
        }
    }

    private fun moveCameraToCoord(latitude: Double, longitude: Double) {
        val coord = LatLng(latitude, longitude)
        val cameraUpdateParams = CameraUpdateParams().apply {
            scrollTo(coord)
            zoomTo(MAP_DEFAULT_ZOOM)
        }

        naverMap.moveCamera(CameraUpdate.withParams(cameraUpdateParams))
    }

    /**
     * 하단의 식당 정보 가로 스크롤 뷰를 표시함.
     * - (중요) 지도에 마커를 표시하기 위한 식당 정보를 이미 fetch하였다는 전제 하에 작동함.
     */
    fun showLocationViewPager(markerIndex: Int = -1) {
        if (mRestaurantInfoArray.size > 0) {
            showRestaurantView(mRestaurantInfoArray, markerIndex)
        }
    }

    fun showLocationInfo(location: String) {
        viewModelScope.launch {
            selectedLocation = location

            when (selectedLocation) {
                "강남" -> moveCameraToCoord(LATLNG_GN.latitude, LATLNG_GN.longitude)
                "역삼" -> moveCameraToCoord(LATLNG_YS.latitude, LATLNG_YS.longitude)
            }

            mRestaurantInfoArray = fireStoreRepository.getRestaurantInLocation(location)
            setMarkers(mRestaurantInfoArray)

            analyticsUtils.saveContentSelectionLog(CONTENT_TYPE_LOCATION, location)
        }
    }

    fun updateLocationInfo(currentViewPagerIndex: Int) {
        if (isInitialized) {
            showLoading(true)
            viewModelScope.launch {
                mRestaurantInfoArray = fireStoreRepository.getRestaurantInLocation(selectedLocation)
                setMarkers(mRestaurantInfoArray, currentViewPagerIndex)

                fetchRestaurantInfo(mRestaurantInfoArray)
                showLoading(false)
            }
        }
    }

    /**
     * 지도에 식당 마커들을 표시함.
     * selectedIndex 값을 지정할 경우, 해당 인덱스의 마커를 클릭된 아이콘(@drawable/restaurant_marker_selected)으로 표시함.
     * 마커를 표시하려는 식당의 메뉴가 등록되지 않은 경우와 등록된 경우의 마커가 다름.
     * @param selectedIndex  선택된 아이콘으로 변경할 마커의 인덱스.
     */
    private fun setMarkers(restaurantInfo: ArrayList<Restaurant>, selectedIndex: Int = -1) {
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
                    zIndex = if (restaurant.todayMenu.date == DateUtils.getTodayDate()) {
                        Marker.DEFAULT_GLOBAL_Z_INDEX + 1
                    } else {
                        Marker.DEFAULT_GLOBAL_Z_INDEX
                    }
                    icon = if (restaurant.todayMenu.date == DateUtils.getTodayDate()) {
                        OverlayImage.fromResource(R.drawable.restaurant_marker)
                    } else {
                        OverlayImage.fromResource(R.drawable.restaurant_marker_menu_not_added)
                    }
                    setOnClickListener {
                        onMarkerClicked(index, selectedLocation)
                        analyticsUtils.saveContentSelectionLog(CONTENT_TYPE_MARKER, restaurant.name)
                        true
                    }
                }

                markerList.add(marker)
            }

            // 클릭된 아이콘으로 변경
            if (selectedIndex != -1) {
                markerList[selectedIndex].apply {
                    icon = if (restaurantInfo[selectedIndex].todayMenu.date == DateUtils.getTodayDate()) {
                        OverlayImage.fromResource(R.drawable.restaurant_marker_selected)
                    } else {
                        OverlayImage.fromResource(R.drawable.restaurant_marker_selected_menu_not_added)
                    }
                    zIndex = Marker.DEFAULT_GLOBAL_Z_INDEX + 1
                }
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
                showLoading(true)
                requestLocationUpdateOnce(
                    locationManager,
                    LocationListener { location ->
                        // 내 위치로 카메라 이동, 내 위치 표시
                        moveCameraToCoord(location.latitude, location.longitude)
                        naverMap.apply {
                            locationSource = FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE)
                            locationTrackingMode = LocationTrackingMode.Follow
                        }
                        moveToCurrentLocation()
                        showLoading(false)
                    })
            }
        }
    }

    fun checkLatestAppVersion() {
        val latestAppVersion = remoteConfigRepository.getLatestAppVersionConfig()
        val currentAppVersion = BuildConfig.VERSION_CODE

        if (latestAppVersion.toInt() > currentAppVersion) {
            showUpdateDialog()
        }
    }

    fun getFeedbackUrl(): String {
        return remoteConfigRepository.getFeedbackUrlConfig()
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
    private fun moveToCurrentLocation() {
        event(Event.MoveToCurrentLocation(""))
    }

    private fun showLoading(visibility: Boolean) {
        event(Event.ShowLoading(visibility))
    }

    private fun showUpdateDialog() {
        event(Event.ShowUpdateDialog(""))
    }

    private fun fetchRestaurantInfo(data: ArrayList<Restaurant>) {
        event(Event.FetchRestaurantInfo(data))
    }

    sealed class Event {
        /**
         * MainActivity에 전달할 이벤트를 이 곳에 정의함.
         *
         * (ex) data class ShowToast(val text: String) : Event()
         */
        data class ShowToast(val text: String): Event()
        data class ShowRestaurantView(val data: ArrayList<Restaurant>, val markerIndex: Int): Event()
        data class OnMarkerClicked(val markerIndex: Int, val location: String): Event()
        data class RequestLocationPermission(val data: String): Event()
        data class ShowGpsPermissionAlert(val data: String): Event()
        data class MoveToCurrentLocation(val data: String): Event()
        data class ShowLoading(val visibility: Boolean): Event()
        data class ShowUpdateDialog(val data: String): Event()

        data class FetchRestaurantInfo(val data: ArrayList<Restaurant>): Event()
    }
}