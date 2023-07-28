package com.woozoo.menumonya.ui.screen

import android.app.Activity
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.CameraUpdateParams
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.woozoo.menumonya.Application
import com.woozoo.menumonya.Constants
import com.woozoo.menumonya.R
import com.woozoo.menumonya.data.model.Restaurant
import com.woozoo.menumonya.data.repository.FireStoreRepository
import com.woozoo.menumonya.util.AnalyticsUtils
import com.woozoo.menumonya.util.DateUtils
import com.woozoo.menumonya.util.LocationUtils
import com.woozoo.menumonya.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.lang.Double
import javax.inject.Inject
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.apply

@HiltViewModel
class MapViewModel @Inject constructor(
    private val fireStoreRepository: FireStoreRepository,
    private val analyticsUtils: AnalyticsUtils
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<MapViewEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    lateinit var naverMap: NaverMap

    private var mRestaurantInfoArray: ArrayList<Restaurant> = ArrayList()
    private var markerList: ArrayList<Marker> = ArrayList()

    var selectedRegion: String = ""
    var isInitialized: Boolean = false

    lateinit var locationManager: LocationManager

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private fun event(event: MapViewEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    /**
     * 하단의 식당 정보 가로 스크롤 뷰를 표시함.
     * - (중요) 지도에 마커를 표시하기 위한 식당 정보를 이미 fetch하였다는 전제 하에 작동함.
     */
    fun showRestaurantRecyclerView(markerIndex: Int = -1) {
        if (mRestaurantInfoArray.size > 0) {
            viewModelScope.launch {
                val buttonTextList = fireStoreRepository.getReportButtonText()
                showRestaurantView(mRestaurantInfoArray, buttonTextList, markerIndex)
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
        if (!PermissionUtils.isGpsPermissionAllowed()) {
            showGpsPermissionAlert()
        } else {
            if (!PermissionUtils.isLocationPermissionAllowed()) {
                requestLocationPermission()
            } else {
                showLoading(true)
                LocationUtils.requestLocationUpdateOnce(
                    locationManager
                ) { location ->
                    // 내 위치로 카메라 이동, 내 위치 표시
                    moveCameraToCoord(location.latitude, location.longitude)
                    naverMap.apply {
                        locationSource =
                            FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE)
                        locationTrackingMode = LocationTrackingMode.Follow
                    }
                    moveToCurrentLocation()
                    showLoading(false)
                }
            }
        }
    }

    fun moveCameraToCoord(latitude: kotlin.Double, longitude: kotlin.Double) {
        val coord = LatLng(latitude, longitude)
        val cameraUpdateParams = CameraUpdateParams().apply {
            scrollTo(coord)
            zoomTo(Constants.MAP_DEFAULT_ZOOM)
        }

        naverMap.moveCamera(CameraUpdate.withParams(cameraUpdateParams))
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
                val latitude = Double.parseDouble(restaurant.location.coord.latitude)
                val longitude = Double.parseDouble(restaurant.location.coord.longitude)
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
                        onMarkerClicked(index)
                        analyticsUtils.saveContentSelectionLog(
                            AnalyticsUtils.CONTENT_TYPE_MARKER,
                            restaurant.name
                        )
                        true
                    }
                }

                markerList.add(marker)
            }

            // 클릭된 아이콘으로 변경
            if (selectedIndex != -1) {
                markerList[selectedIndex].apply {
                    icon =
                        if (restaurantInfo[selectedIndex].todayMenu.date == DateUtils.getTodayDate()) {
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

    fun moveCameraToMarker(markerIndex: Int) {
        if (mRestaurantInfoArray.size > 0) {
            val selectedRestaurant = mRestaurantInfoArray[markerIndex]

            val latitude = Double.parseDouble(selectedRestaurant.location.coord.latitude)
            val longitude = Double.parseDouble(selectedRestaurant.location.coord.longitude)

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
                    zIndex =
                        if (mRestaurantInfoArray[index].todayMenu.date == DateUtils.getTodayDate()) {
                            Marker.DEFAULT_GLOBAL_Z_INDEX + 1
                        } else {
                            Marker.DEFAULT_GLOBAL_Z_INDEX
                        }
                    icon =
                        if (mRestaurantInfoArray[index].todayMenu.date == DateUtils.getTodayDate()) {
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

            naverMap.setContentPadding(
                0,
                0,
                0,
                Application.context().resources.getDimensionPixelOffset(R.dimen.restaurant_item_height)
            )
            naverMap.moveCamera(CameraUpdate.scrollTo(coord).animate(CameraAnimation.None))

            analyticsUtils.saveContentSelectionLog(
                AnalyticsUtils.CONTENT_TYPE_LIST,
                selectedRestaurant.name
            )
        }
    }

    /**
     * (1) 식당 마커 표시
     * (2) 로그 저장(선택된 지역)
     */
    fun showRegionMarkers(region: String) {
        viewModelScope.launch {
            selectedRegion = region

            mRestaurantInfoArray = fireStoreRepository.getRestaurantInRegion(selectedRegion)
            setMarkers(mRestaurantInfoArray)

            analyticsUtils.saveContentSelectionLog(
                AnalyticsUtils.CONTENT_TYPE_REGION,
                selectedRegion
            )
        }
    }

    fun updateRegionInfo(currentRestaurantIndex: Int) {
        if (isInitialized) {
            showLoading(true)
            viewModelScope.launch {
                mRestaurantInfoArray = fireStoreRepository.getRestaurantInRegion(selectedRegion)
                setMarkers(mRestaurantInfoArray, currentRestaurantIndex)

                fetchRestaurantInfo(mRestaurantInfoArray)
                showLoading(false)
            }
        }
    }

    private fun showRestaurantView(
        data: ArrayList<Restaurant>,
        buttonTextList: ArrayList<String>, markerIndex: Int
    ) {
        event(MapViewEvent.ShowRestaurantView(data, buttonTextList, markerIndex))
    }

    private fun onMarkerClicked(markerIndex: Int) {
        event(MapViewEvent.OnMarkerClicked(markerIndex))
    }

    private fun moveToCurrentLocation() {
        event(MapViewEvent.MoveToCurrentLocation(""))
    }

    private fun fetchRestaurantInfo(data: ArrayList<Restaurant>) {
        event(MapViewEvent.FetchRestaurantInfo(data))
    }

    private fun showLoading(visibility: Boolean) {
        event(MapViewEvent.ShowLoading(visibility))
    }

    fun invalidateRecyclerView() {
        event(MapViewEvent.InvalidateRecyclerView(""))
    }

    private fun requestLocationPermission() {
        event(MapViewEvent.RequestLocationPermission(""))
    }

    private fun showGpsPermissionAlert() {
        event(MapViewEvent.ShowGpsPermissionAlert(""))
    }

    sealed class MapViewEvent {
        data class RequestLocationPermission(val data: String) : MapViewEvent()
        data class ShowGpsPermissionAlert(val data: String) : MapViewEvent()
        data class OnMarkerClicked(val markerIndex: Int) : MapViewEvent()
        data class MoveToCurrentLocation(val data: String) : MapViewEvent()
        data class FetchRestaurantInfo(val data: ArrayList<Restaurant>) : MapViewEvent()
        data class ShowLoading(val visibility: Boolean) : MapViewEvent()
        data class InvalidateRecyclerView(val data: String) : MapViewEvent()
        data class ShowRestaurantView(
            val data: ArrayList<Restaurant>,
            val buttonTextList: ArrayList<String>,
            val markerIndex: Int
        ) : MapViewEvent()
    }
}