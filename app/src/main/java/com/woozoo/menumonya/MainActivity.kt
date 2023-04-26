package com.woozoo.menumonya

import android.content.Intent
import android.content.pm.PackageManager
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
import com.woozoo.menumonya.MainViewModel.Event
import com.woozoo.menumonya.databinding.ActivityMainBinding
import com.woozoo.menumonya.util.PermissionUtils.Companion.ACCESS_FINE_LOCATION_REQUEST_CODE
import com.woozoo.menumonya.util.PermissionUtils.Companion.requestLocationPermission

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val GPS_ENABLE_REQUEST_CODE = 2000

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.restaurantViewPager

        repeatOnStarted {
            viewModel.eventFlow.collect { event -> handleEvent(event) }
        }

        binding.locationGnBtn.background = applicationContext.getDrawable(R.drawable.selector_location_button_selected)
        binding.locationGnBtn.setTextColor(applicationContext.getColor(R.color.white))
        binding.locationGnBtn.setOnClickListener(this)
        binding.locationYsBtn.setOnClickListener(this)
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

                updatePagerHeightForChild(page, viewPager)
            }

            registerOnPageChangeCallback(object: OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    viewModel.moveCameraToMarker(position)
                }
            })
        }

        binding.naverMap.onCreate(savedInstanceState)

        viewModel.initializeMapView(binding.naverMap, this)
    }

    private fun updatePagerHeightForChild(view: View, pager: ViewPager2) {
        view.post {
            val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(wMeasureSpec, hMeasureSpec)
            pager.layoutParams = (pager.layoutParams).also { lp -> lp.height = view.measuredHeight }
            pager.invalidate()
        }
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
                viewPager.adapter = RestaurantAdapter(event.data)
                if (event.markerIndex != -1) {
                    viewPager.currentItem = event.markerIndex
                } else { }
            } else { }
        }
        is Event.RequestLocationPermission -> {
            requestLocationPermission(this)
        }
        is Event.ShowGpsPermissionAlert -> {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("현재 위치를 찾을 수 없습니다.\n위치 서비스를 켜주세요.")
            builder.setCancelable(true)
            builder.setNegativeButton("취소") { dialog, which ->
                dialog.dismiss()
            }
            builder.setPositiveButton("확인") { dialog, which ->
                val gpsPermissionIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(gpsPermissionIntent, GPS_ENABLE_REQUEST_CODE)
                dialog.dismiss()
            }
            builder.create().show()
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
                binding.locationGnBtn.background = applicationContext.getDrawable(R.drawable.selector_location_button_selected)
                binding.locationYsBtn.background = applicationContext.getDrawable(R.drawable.selector_location_button)
                binding.locationGnBtn.setTextColor(applicationContext.getColor(R.color.white))
                binding.locationYsBtn.setTextColor(applicationContext.getColor(R.color.gray600))
            }
            R.id.location_ys_btn -> {
                viewPager.invalidate()
                viewPager.adapter = null
                viewModel.showLocationInfo("역삼")
                binding.locationYsBtn.background = applicationContext.getDrawable(R.drawable.selector_location_button_selected)
                binding.locationGnBtn.background = applicationContext.getDrawable(R.drawable.selector_location_button)
                binding.locationYsBtn.setTextColor(applicationContext.getColor(R.color.white))
                binding.locationGnBtn.setTextColor(applicationContext.getColor(R.color.gray600))
            }
            R.id.current_location_btn -> {
                viewModel.getCurrentLocation(this)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.getCurrentLocation(this)
            } else {
                Toast.makeText(this, "위치 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
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