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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.woozoo.menumonya.MainViewModel.Event
import com.woozoo.menumonya.databinding.ActivityMainBinding
import com.woozoo.menumonya.util.PermissionUtils.Companion.ACCESS_FINE_LOCATION_REQUEST_CODE
import com.woozoo.menumonya.util.PermissionUtils.Companion.requestLocationPermission

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val GPS_ENABLE_REQUEST_CODE = 2000

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewPager: ViewPager2
    private lateinit var locationPermissionDialog: LocationPermissionDialog

    private lateinit var amplitude: Amplitude

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.restaurantViewPager

        repeatOnStarted {
            viewModel.eventFlow.collect { event -> handleEvent(event) }
        }

        binding.locationGnBtn.background = applicationContext.getDrawable(R.drawable.color_button_background)
        binding.locationGnBtn.setTextColor(applicationContext.getColor(R.color.white))
        binding.locationGnBtn.setOnClickListener(this)
        binding.locationYsBtn.setOnClickListener(this)
        binding.feedbackIv.setOnClickListener(this)
        binding.currentLocationBtn.setOnClickListener(this)

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

        viewModel.initializeMapView(binding.naverMap)

        amplitude = Amplitude(
            Configuration(
                apiKey = resources.getString(R.string.AMPLITUDE_APP_ID),
                context = applicationContext
            )
        )
    }

    private fun handleEvent(event: Event) = when (event) {
        is Event.ShowToast -> Toast.makeText(this, event.text, Toast.LENGTH_SHORT).show()
        is Event.OnMarkerClicked -> {
            if (viewPager.adapter != null) {
                viewPager.currentItem = event.markerIndex
            } else {
                viewModel.showLocationViewPager(event.location, event.markerIndex)
            }
        }
        is Event.ShowRestaurantView -> {
            if (viewPager.adapter == null) {
                viewPager.adapter = RestaurantAdapter(event.data, this)
                if (event.markerIndex != -1) {
                    viewPager.currentItem = event.markerIndex
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
                viewPager.invalidate()
                viewPager.adapter = null
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
                viewPager.invalidate()
                viewPager.adapter = null
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
                amplitude.track("피드백 버튼 클릭")
                val feedbackUrl = viewModel.getFeedbackUrl()
                val intent = Intent(ACTION_VIEW, Uri.parse(feedbackUrl))
                startActivity(intent)
            }
            // '내 주변' 버튼 클릭
            R.id.current_location_btn -> {
                amplitude.track("내 주변 버튼 클릭")
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