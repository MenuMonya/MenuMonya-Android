package com.woozoo.menumonya

import android.Manifest
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
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.woozoo.menumonya.MainViewModel.Event
import com.woozoo.menumonya.databinding.ActivityMainBinding
import com.woozoo.menumonya.util.PermissionUtils.Companion.ACCESS_FINE_LOCATION_REQUEST_CODE
import com.woozoo.menumonya.util.PermissionUtils.Companion.requestLocationPermission

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val GPS_ENABLE_REQUEST_CODE = 2000

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var locationPermissionDialog: LocationPermissionDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repeatOnStarted {
            viewModel.eventFlow.collect { event -> handleEvent(event) }
        }

        binding.locationGnBtn.background = applicationContext.getDrawable(R.drawable.color_button_background)
        binding.locationGnBtn.setTextColor(applicationContext.getColor(R.color.white))
        binding.locationGnBtn.setOnClickListener(this)
        binding.locationYsBtn.setOnClickListener(this)
        binding.feedbackIv.setOnClickListener(this)
        binding.currentLocationBtn.setOnClickListener(this)
        binding.loadingView.setOnClickListener { } // 로딩 화면 아래의 뷰에 대한 터치를 막기 위함

        recyclerView = binding.restaurantRv
        recyclerView.setHasFixedSize(false)
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
//        recyclerView.layoutManager = StaggeredGridLayoutManager(2, 0)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        binding.naverMap.onCreate(savedInstanceState)

        viewModel.initializeMapView(binding.naverMap)
    }

    private fun handleEvent(event: Event) = when (event) {
        is Event.ShowToast -> Toast.makeText(this, event.text, Toast.LENGTH_SHORT).show()
        is Event.OnMarkerClicked -> {
            if (recyclerView.adapter != null) {
                recyclerView.layoutManager?.scrollToPosition(event.markerIndex)
            } else {
                viewModel.showLocationViewPager(event.markerIndex)
            }
        }
        is Event.ShowRestaurantView -> {
            if (recyclerView.adapter == null) {
                recyclerView.adapter = RestaurantAdapter(event.data, this)
                if (event.markerIndex != -1) {
                    recyclerView.layoutManager?.scrollToPosition(event.markerIndex)
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
    }

    override fun onStart() {
        super.onStart()
        binding.naverMap.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.naverMap.onResume()
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
            R.id.location_gn_btn -> {
                recyclerView.invalidate()
                recyclerView.adapter = null
                viewModel.showLocationInfo("강남")
                
                binding.locationGnBtn.background = applicationContext.getDrawable(R.drawable.color_button_background)
                binding.locationYsBtn.background = applicationContext.getDrawable(R.drawable.white_button_background)
                binding.locationGnBtn.setTextColor(applicationContext.getColor(R.color.white))
                binding.locationYsBtn.setTextColor(applicationContext.getColor(R.color.gray600))

                binding.currentLocationBtn.background = resources.getDrawable(R.drawable.current_location_button)
                binding.currentLocationTv.setTextColor(resources.getColor(R.color.colorPrimary))
                binding.currentLocationIv.setColorFilter(resources.getColor(R.color.colorPrimary))
            }
            R.id.location_ys_btn -> {
                recyclerView.invalidate()
                recyclerView.adapter = null
                viewModel.showLocationInfo("역삼")
                
                binding.locationYsBtn.background = applicationContext.getDrawable(R.drawable.color_button_background)
                binding.locationGnBtn.background = applicationContext.getDrawable(R.drawable.white_button_background)
                binding.locationYsBtn.setTextColor(applicationContext.getColor(R.color.white))
                binding.locationGnBtn.setTextColor(applicationContext.getColor(R.color.gray600))

                binding.currentLocationBtn.background = resources.getDrawable(R.drawable.current_location_button)
                binding.currentLocationTv.setTextColor(resources.getColor(R.color.colorPrimary))
                binding.currentLocationIv.setColorFilter(resources.getColor(R.color.colorPrimary))
            }
            R.id.feedback_iv -> {
                val feedbackUrl = viewModel.getFeedbackUrl()
                val intent = Intent(ACTION_VIEW, Uri.parse(feedbackUrl))
                startActivity(intent)
            }
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
}