package com.woozoo.menumonya.ui.screen

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.woozoo.menumonya.R
import com.woozoo.menumonya.data.model.Region
import com.woozoo.menumonya.data.repository.RemoteConfigRepository
import com.woozoo.menumonya.databinding.ActivityMainBinding
import com.woozoo.menumonya.repeatOnStarted
import com.woozoo.menumonya.ui.adapter.RegionAdapter
import com.woozoo.menumonya.ui.dialog.LocationPermissionDialog
import com.woozoo.menumonya.ui.dialog.NoticeDialog
import com.woozoo.menumonya.ui.screen.MainViewModel.Event
import com.woozoo.menumonya.util.AnalyticsUtils
import com.woozoo.menumonya.util.PermissionUtils.Companion.ACCESS_FINE_LOCATION_REQUEST_CODE
import com.woozoo.menumonya.util.PermissionUtils.Companion.requestLocationPermission
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var remoteConfigRepository: RemoteConfigRepository

    @Inject
    lateinit var analyticsUtils: AnalyticsUtils

    private val GPS_ENABLE_REQUEST_CODE = 2000

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private var regionAdapter: RegionAdapter? = null
    private lateinit var locationPermissionDialog: LocationPermissionDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repeatOnStarted {
            viewModel.eventFlow.collect { event -> handleEvent(event) }
        }

        viewModel.locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        viewModel.checkFirstOpen()
        viewModel.getRegionList()
    }

    private fun handleEvent(event: Event) = when (event) {
        is Event.ShowToast -> Toast.makeText(this, event.text, Toast.LENGTH_SHORT).show()
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

        else -> {}
    }

    override fun onResume() {
        super.onResume()

        viewModel.checkLatestAppVersion()
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
    // FIXME: 너무 많은 일들을 하고있음. initvializeMapView()는 왜 여기서 하는거야? -_-
    private fun initRegionRecyclerView(data: ArrayList<Region>) {
        val recyclerView = binding.regionRv

        regionAdapter = RegionAdapter(data, this, remoteConfigRepository, analyticsUtils)
        regionAdapter!!.setOnItemClickListener(object: RegionAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val selectedRegion = data[position]

                viewModel.invalidateViewPager()

                viewModel.apply {
                    showLocationInfo(selectedRegion.name)
                    moveCameraToCoord(selectedRegion.latitude, selectedRegion.longitude)
                    setLastRegionData(selectedRegion.name)
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.adapter = regionAdapter
    }
}