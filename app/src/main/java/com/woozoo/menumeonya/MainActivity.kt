package com.woozoo.menumeonya

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.woozoo.menumeonya.MainViewModel.Event
import com.woozoo.menumeonya.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repeatOnStarted {
            viewModel.eventFlow.collect { event -> handleEvent(event) }
        }

        binding.locationGnBtn.setOnClickListener {
            viewModel.showLocationInfo("강남")
            viewModel.getRestaurantInfo("강남")
        }
        binding.locationYsBtn.setOnClickListener {
            viewModel.showLocationInfo("역삼")
            viewModel.getRestaurantInfo("역삼")
        }

        // 좌우로 item이 보이도록 설정
        binding.restaurantViewPager.apply {
            clipChildren = false
            clipToPadding = false
            offscreenPageLimit = 3 // 한 화면에 3개의 item이 렌더링됨
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER // 스크롤뷰 효과 없앰

            val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
            val offsetPx = resources.getDimensionPixelOffset(R.dimen.offset)
            binding.restaurantViewPager.setPageTransformer { page, position ->
                val offset = position * -(2 * offsetPx + pageMarginPx)
                page.translationX = offset // offset 만큼 왼쪽으로 이동시킴
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

    private fun handleEvent(event: Event) = when (event) {
        is Event.ShowToast -> Toast.makeText(this, event.text, Toast.LENGTH_SHORT).show()
        is Event.ShowRestaurantView -> {
            binding.restaurantViewPager.adapter = RestaurantAdapter(event.data)
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
}