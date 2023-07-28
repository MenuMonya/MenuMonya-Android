package com.woozoo.menumonya.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.naver.maps.map.LocationTrackingMode
import com.woozoo.menumonya.Constants
import com.woozoo.menumonya.R
import com.woozoo.menumonya.data.repository.RemoteConfigRepository
import com.woozoo.menumonya.databinding.FragmentMapBinding
import com.woozoo.menumonya.repeatOnStarted
import com.woozoo.menumonya.ui.adapter.RestaurantAdapter
import com.woozoo.menumonya.ui.dialog.LocationPermissionDialog
import com.woozoo.menumonya.ui.screen.MapViewModel.MapViewEvent.*
import com.woozoo.menumonya.util.AnalyticsUtils
import com.woozoo.menumonya.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment(), View.OnClickListener {

    @Inject
    lateinit var remoteConfigRepository: RemoteConfigRepository

    @Inject
    lateinit var analyticsUtils: AnalyticsUtils

    private val viewModel: MapViewModel by activityViewModels()
    private lateinit var binding: FragmentMapBinding

    private lateinit var restaurantRv: RecyclerView
    private var restaurantAdapter: RestaurantAdapter? = null
    private lateinit var locationPermissionDialog: LocationPermissionDialog
    private var currentRestaurantIndex = 0

    private val GPS_ENABLE_REQUEST_CODE = 2000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container, false)

        binding.naverMap.getMapAsync { map ->
            map.apply {
                locationTrackingMode = LocationTrackingMode.NoFollow
                uiSettings.apply {
                    isLocationButtonEnabled = false
                    isZoomControlEnabled = false
                }
                minZoom = Constants.MAP_MIN_ZOOM
            }

            viewModel.locationManager =
                requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            viewModel.naverMap = map
            viewModel.isInitialized = true
        }

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.naverMap.onCreate(savedInstanceState)

        repeatOnStarted {
            viewModel.eventFlow.collect { event -> handleEvent(event) }
        }

        setUpRecyclerView()

        binding.currentLocationBtn.setOnClickListener(this)
        binding.loadingView.setOnClickListener { } // 로딩 화면 아래의 뷰에 대한 터치를 막기 위함
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.ACCESS_FINE_LOCATION_REQUEST_CODE) {
            // (1) 권한 허용 여부 체크
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.getCurrentLocation(requireActivity())
            } else {
                // (2) '다시는 보지 않음' 클릭 여부 체크
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(
                        requireContext(), R.string.location_permission_denied_toast,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(), R.string.location_permission_denied_forever_toast,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPS_ENABLE_REQUEST_CODE) {
            viewModel.getCurrentLocation(requireActivity())
        }
    }

    override fun onStart() {
        super.onStart()
        binding.naverMap.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.naverMap.onResume()

        if (restaurantAdapter != null) {
            viewModel.updateRegionInfo(currentRestaurantIndex)
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.naverMap.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.naverMap.onLowMemory()
    }

    private fun handleEvent(event: MapViewModel.MapViewEvent) = when (event) {
        is RequestLocationPermission -> {
            locationPermissionDialog = LocationPermissionDialog(requireContext()) {
                PermissionUtils.requestLocationPermission(requireActivity())
                locationPermissionDialog.dismiss()
            }
            locationPermissionDialog.show()
        }

        is ShowGpsPermissionAlert -> {
            AlertDialog.Builder(requireContext()).apply {
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

        is ShowRestaurantView -> {
            if (restaurantRv.adapter == null) {
                restaurantAdapter = RestaurantAdapter(
                    event.data,
                    event.buttonTextList,
                    requireActivity(),
                    remoteConfigRepository,
                    analyticsUtils
                )
                restaurantRv.adapter = restaurantAdapter
                if (event.markerIndex != -1) {
                    restaurantRv.scrollToPosition(event.markerIndex)
                } else {
                }
            } else {
            }
        }

        is OnMarkerClicked -> {
            if (restaurantRv.adapter != null) {
                restaurantRv.layoutManager?.scrollToPosition(event.markerIndex)
            } else {
                viewModel.showRestaurantRecyclerView(event.markerIndex)
            }
        }

        is MoveToCurrentLocation -> {
            binding.currentLocationBtn.background =
                resources.getDrawable(R.drawable.current_location_button_selected)
            binding.currentLocationTv.setTextColor(resources.getColor(R.color.colorSecondary))
            binding.currentLocationIv.setColorFilter(resources.getColor(R.color.colorSecondary))
        }

        is FetchRestaurantInfo -> {
            if (restaurantAdapter != null) {
                restaurantAdapter?.setData(event.data)
                restaurantRv.adapter?.notifyDataSetChanged()
            } else {
            }
        }

        is ShowLoading -> {
            if (event.visibility) {
                binding.loadingView.visibility = View.VISIBLE
            } else {
                binding.loadingView.visibility = View.GONE
            }
        }

        is InvalidateRecyclerView -> {
            restaurantRv.invalidate()
            restaurantRv.adapter = null
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            // '내 주변' 버튼 클릭
            R.id.current_location_btn -> {
                viewModel.getCurrentLocation(requireActivity())
            }
        }
    }

    private fun setUpRecyclerView() {
        restaurantRv = binding.restaurantRv
        restaurantRv.apply {
            setHasFixedSize(false)
            itemAnimator = null
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        }

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(restaurantRv)

        restaurantRv.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val snapView = snapHelper.findSnapView(restaurantRv.layoutManager)
                if (snapView != null) {
                    val scrolledIndex =
                        (restaurantRv.layoutManager as LinearLayoutManager).getPosition(snapView)
                    if (scrolledIndex != currentRestaurantIndex) {
                        currentRestaurantIndex = scrolledIndex

                        viewModel.moveCameraToMarker(scrolledIndex)
                    }
                }
            }
        })
    }
}