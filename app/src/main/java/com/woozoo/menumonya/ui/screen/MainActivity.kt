package com.woozoo.menumonya.ui.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
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
import com.woozoo.menumonya.ui.dialog.NoticeDialog
import com.woozoo.menumonya.ui.screen.MainViewModel.Event
import com.woozoo.menumonya.util.AnalyticsUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var remoteConfigRepository: RemoteConfigRepository

    @Inject
    lateinit var analyticsUtils: AnalyticsUtils

    private val viewModel: MainViewModel by viewModels()
    private val mapViewModel: MapViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private var regionAdapter: RegionAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repeatOnStarted {
            viewModel.eventFlow.collect { event -> handleEvent(event) }
        }

        viewModel.checkFirstOpen()
        viewModel.showRegionList()
    }

    private fun handleEvent(event: Event) = when (event) {
        is Event.ShowToast -> Toast.makeText(this, event.text, Toast.LENGTH_SHORT).show()

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

    override fun onResume() {
        super.onResume()

        viewModel.checkLatestAppVersion()
    }


    /**
     * (1) 지역 리스트 버튼 표시(RecyclerView)
     * (2) 클릭 로직 적용(RecyclerView-selection)
     */
    private fun initRegionRecyclerView(data: ArrayList<Region>) {
        val recyclerView = binding.regionRv

        regionAdapter = RegionAdapter(data, this, remoteConfigRepository, analyticsUtils)
        regionAdapter!!.setOnItemClickListener(object: RegionAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val selectedRegion = data[position]

                viewModel.setLastRegionData(selectedRegion.name)

                mapViewModel.selectedLocation = selectedRegion.name
                mapViewModel.invalidateViewPager()
                mapViewModel.showLocationInfo(selectedRegion.name)
                mapViewModel.moveCameraToCoord(selectedRegion.latitude, selectedRegion.longitude)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView.adapter = regionAdapter
    }
}