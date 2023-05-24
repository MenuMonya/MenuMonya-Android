package com.woozoo.menumonya

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.woozoo.menumonya.MainViewModel.Event
import com.woozoo.menumonya.databinding.ActivityMainBinding
import com.woozoo.menumonya.model.Region
import com.woozoo.menumonya.repository.RemoteConfigRepository
import com.woozoo.menumonya.util.AnalyticsUtils
import com.woozoo.menumonya.util.PermissionUtils.Companion.ACCESS_FINE_LOCATION_REQUEST_CODE
import com.woozoo.menumonya.util.PermissionUtils.Companion.requestLocationPermission
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {
    @Inject lateinit var remoteConfigRepository: RemoteConfigRepository
    @Inject lateinit var analyticsUtils: AnalyticsUtils

    private val GPS_ENABLE_REQUEST_CODE = 2000

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewPager: ViewPager2
    private var restaurantAdapter: RestaurantAdapter? = null
    private var regionAdapter: RegionAdapter? = null
    private lateinit var locationPermissionDialog: LocationPermissionDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.restaurantViewPager

        repeatOnStarted {
            viewModel.eventFlow.collect { event -> handleEvent(event) }
        }

        binding.currentLocationBtn.setOnClickListener(this)
        binding.loadingView.setOnClickListener { } // 로딩 화면 아래의 뷰에 대한 터치를 막기 위함

        // 좌우로 item이 보이도록 설정
        viewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3 // 한 화면에 3개의 item이 렌더링됨
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER // 스크롤뷰 효과 없앰

            val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
            val offsetPx = resources.getDimensionPixelOffset(R.dimen.offset)
            viewPager.setPageTransformer { page, position ->
                val offset = position * -(2 * offsetPx + pageMarginPx)
                page.translationX = offset // offset 만큼 왼쪽으로 이동시킴
            }

            registerOnPageChangeCallback(object: OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    // height를 wrap_content가 되도록 설정
                    val view = (getChildAt(0) as RecyclerView).layoutManager?.findViewByPosition(position)
                    view?.post {
                        val wMeasureSpec =
                            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
                        val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        view.measure(wMeasureSpec, hMeasureSpec)
                        if (getChildAt(0).layoutParams.height != view.measuredHeight) {
                            getChildAt(0).layoutParams = (getChildAt(0).layoutParams).also { lp ->
                                lp.height = view.measuredHeight
                            }
                        }
                    }

                    // 마커로 카메라 이동
                    viewModel.moveCameraToMarker(position)
                }
            })
        }

        binding.naverMap.onCreate(savedInstanceState)
        viewModel.getRegionList()
    }

    private suspend fun handleEvent(event: Event) = when (event) {
        is Event.ShowToast -> Toast.makeText(this, event.text, Toast.LENGTH_SHORT).show()
        is Event.FetchRestaurantInfo -> {
            if (restaurantAdapter != null) {
                restaurantAdapter?.setData(event.data)
                viewPager.adapter?.notifyDataSetChanged()
            } else { }
        }
        is Event.OnMarkerClicked -> {
            if (viewPager.adapter != null) {
                viewPager.setCurrentItem(event.markerIndex, false)
            } else {
                viewModel.showLocationViewPager(event.markerIndex)
            }
        }
        is Event.ShowRestaurantView -> {
            if (viewPager.adapter == null) {
                restaurantAdapter = RestaurantAdapter(event.data, this, remoteConfigRepository, analyticsUtils)
                viewPager.adapter = restaurantAdapter
                if (event.markerIndex != -1) {
                    viewPager.setCurrentItem(event.markerIndex, false)
                } else { }
            } else { }
        }
        is Event.RequestLocationPermission -> {
            locationPermissionDialog = LocationPermissionDialog(this) {
                requestLocationPermission(this)
                locationPermissionDialog.dismiss()
            }
            locationPermissionDialog.show()
        }
        is Event.ShowGpsPermissionAlert -> {
            AlertDialog.Builder(this).apply {
                setMessage("현재 위치를 찾을 수 없습니다.\n위치 서비스를 켜주세요.")
                setCancelable(true)
                setNegativeButton("취소") { dialog, which ->
                    dialog.dismiss()
                }
                setPositiveButton("확인") { dialog, which ->
                    val gpsPermissionIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(gpsPermissionIntent, GPS_ENABLE_REQUEST_CODE)
                    dialog.dismiss()
                }
            }.create().show()
        }
        is Event.MoveToCurrentLocation -> {
            binding.currentLocationBtn.background = resources.getDrawable(R.drawable.current_location_button_selected)
            binding.currentLocationTv.setTextColor(resources.getColor(R.color.colorSecondary))
            binding.currentLocationIv.setColorFilter(resources.getColor(R.color.colorSecondary))
        }
        is Event.ShowLoading -> {
            if (event.visibility) {
                binding.loadingView.visibility = View.VISIBLE
            } else {
                binding.loadingView.visibility = View.GONE
            }
        }
        is Event.ShowUpdateDialog -> {
            AlertDialog.Builder(this).apply {
                setMessage(resources.getString(R.string.latest_app_version_update_message))
                setCancelable(true)
                setPositiveButton("확인") { dialog, _ ->
                    try {
                        startActivity(Intent(ACTION_VIEW,
                            Uri.parse(resources.getString(R.string.google_play_store_link))))
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(ACTION_VIEW,
                            Uri.parse(resources.getString(R.string.google_play_store_link_web))))
                    }
                    dialog.dismiss()
                }
            }.create().show()
        }
        is Event.ShowRegionList -> {
            initRegionRecyclerView(event.data)
        }
        is Event.ShowNoticeDialog -> {
            NoticeDialog(this).show()

        }
    }

    override fun onStart() {
        super.onStart()
        binding.naverMap.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.naverMap.onResume()

        viewModel.checkLatestAppVersion()
        if (viewPager != null && restaurantAdapter != null) {
            viewModel.updateLocationInfo(viewPager.currentItem)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.naverMap.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.naverMap.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.naverMap.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.naverMap.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.naverMap.onLowMemory()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            // '내 주변' 버튼 클릭
            R.id.current_location_btn -> {
                viewModel.getCurrentLocation(this)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) {
            // (1) 권한 허용 여부 체크
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.getCurrentLocation(this)
            } else {
                // (2) '다시는 보지 않음' 클릭 여부 체크
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, R.string.location_permission_denied_toast,
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.location_permission_denied_forever_toast,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPS_ENABLE_REQUEST_CODE) {
            viewModel.getCurrentLocation(this)
        }
    }

    /**
     * (1) 지역 리스트 버튼 표시(RecyclerView)
     * (2) 클릭 로직 적용(RecyclerView-selection)
     * (3) 네이버 맵 초기화, 카메라 이동
     */
    private suspend fun initRegionRecyclerView(data: ArrayList<Region>) {
        val recyclerView = binding.regionRv
        val modifiedData = viewModel.modifyRegionData(data)

        regionAdapter = RegionAdapter(modifiedData, this, remoteConfigRepository, analyticsUtils)
        regionAdapter!!.setOnItemClickListener(object: RegionAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val selectedRegion = modifiedData[position]

                viewPager.invalidate()
                viewPager.adapter = null

                viewModel.showLocationInfo(selectedRegion.name)
                viewModel.moveCameraToCoord(selectedRegion.latitude, selectedRegion.longitude)

                viewModel.setLastRegionData(selectedRegion.name)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.adapter = regionAdapter

        viewModel.initializeMapView(binding.naverMap, modifiedData[0])
    }
}