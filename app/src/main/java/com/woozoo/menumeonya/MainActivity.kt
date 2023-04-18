package com.woozoo.menumeonya

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.map.MapView
import com.woozoo.menumeonya.MainViewModel.Event

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var mapView: MapView
    private lateinit var locationGnButton: Button
    private lateinit var locationYsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repeatOnStarted {
            viewModel.eventFlow.collect { event -> handleEvent(event) }
        }

        locationGnButton = findViewById(R.id.location_gn_btn)
        locationYsButton = findViewById(R.id.location_ys_btn)
        locationGnButton.setOnClickListener {

        }
        locationYsButton.setOnClickListener {

        }

        mapView = findViewById(R.id.naver_map)
        mapView.onCreate(savedInstanceState)

        viewModel.initializeMapView(mapView, this) {
            viewModel.moveCamera(it)
        }
    }

    private fun handleEvent(event: Event) = when (event) {
        is Event.ShowToast -> Toast.makeText(this, event.text, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}