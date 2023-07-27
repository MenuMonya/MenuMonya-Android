package com.woozoo.menumonya.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woozoo.menumonya.BuildConfig
import com.woozoo.menumonya.Constants.Companion.REGION_REPORT
import com.woozoo.menumonya.Constants.Companion.REGION_REPORT_TYPE
import com.woozoo.menumonya.data.model.Region
import com.woozoo.menumonya.data.repository.DataStoreRepository
import com.woozoo.menumonya.data.repository.FireStoreRepository
import com.woozoo.menumonya.data.repository.RemoteConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fireStoreRepository: FireStoreRepository,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val dataStoreRepository: DataStoreRepository
): ViewModel() {

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    private fun event(event: Event) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    /**
     * 지역 정보를 조회하고 화면에 표시하기 위한 형태로 리스트를 수정함.
     */
    fun showRegionList() {
        viewModelScope.launch {
            val regionList = fireStoreRepository.getRegionList()
            val modifiedRegionList = modifyRegionData(regionList)

            initRegionRecyclerView(modifiedRegionList)
            initRegionMapView(modifiedRegionList[0])
        }
    }

    fun checkLatestAppVersion() {
        val latestAppVersion = remoteConfigRepository.getLatestAppVersionConfig()
        val currentAppVersion = BuildConfig.VERSION_CODE

        if (latestAppVersion.toInt() > currentAppVersion) {
            showUpdateDialog()
        }
    }

    /**
     * 최초 실행 여부 체크 및 다이얼로그 표시
     */
    fun checkFirstOpen() {
        viewModelScope.launch {
            val isFirstOpen = dataStoreRepository.getIsFirstOpen()

            if (isFirstOpen) {
                showNoticeDialog()
                dataStoreRepository.setIsFirstOpen(false)
            }
        }
    }

    /**
     * (0) regionId 값으로 데이터 정렬(보여주고싶은 순서로 활용함)
     * (1) 마지막으로 클릭한 지역을 가장 첫번째로 오도록 순서 변경
     * (2) '지역건의' 버튼 추가
     */
    suspend fun modifyRegionData(data: ArrayList<Region>) = withContext(Dispatchers.IO) {
        data.sortBy { it.regionId } // (0)

        // (1)
        val lastSelectedRegion = dataStoreRepository.getLastSelectedRegion()
        val lastSelectedRegionIndex = data.indexOfFirst { it.name == lastSelectedRegion }
        Collections.swap(data, 0, lastSelectedRegionIndex)

        data.add(Region(REGION_REPORT, 0.0, 0.0, 999, REGION_REPORT_TYPE)) // (2)

        data
    }

    fun setLastRegionData(region: String) {
        viewModelScope.launch {
            dataStoreRepository.setLastSelectedRegion(region)
        }
    }

    private fun showUpdateDialog() {
        event(Event.ShowUpdateDialog(""))
    }

    private fun initRegionMapView(data: Region) {
        event(Event.InitRegionMapView(data))
    }

    private fun initRegionRecyclerView(data: ArrayList<Region>) {
        event(Event.InitRegionRecyclerView(data))
    }

    private fun showNoticeDialog() {
        event(Event.ShowNoticeDialog(""))
    }


    sealed class Event {
        /**
         * MainActivity에 전달할 이벤트를 이 곳에 정의함.
         *
         * (ex) data class ShowToast(val text: String) : Event()
         */
        data class InitRegionRecyclerView(val data: ArrayList<Region>) : Event()
        data class InitRegionMapView(val data: Region) : Event()
        data class ShowToast(val text: String) : Event()
        data class ShowUpdateDialog(val data: String) : Event()
        data class ShowNoticeDialog(val data: String) : Event()
    }
}