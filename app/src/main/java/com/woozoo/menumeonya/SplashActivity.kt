package com.woozoo.menumeonya

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

    private val ACCESS_FINE_LOCATION = 1000
    private val GPS_ENABLE_REQUEST_CODE = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (!checkLocationServiceStatus()) {
            showDialogForLocationServiceSetting()
        } else {
            if (!checkPermission()) {
                requestPermission()
            } else {
                startActivity(Intent(this, NaverActivity::class.java))
                finish()
            }
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
            listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray(),
            ACCESS_FINE_LOCATION
        )
    }

    private fun checkLocationServiceStatus(): Boolean {
        val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    private fun showDialogForLocationServiceSetting() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("앱을 사용하기 위해 위치 서비스가 필요합니다.")
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { dialog, which ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)

            if (!checkPermission()) {
                requestPermission()
            }
        }
        builder.create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "위치 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()

                if (!checkLocationServiceStatus()) {
                    showDialogForLocationServiceSetting()
                }

                startActivity(Intent(this, NaverActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "위치 권한이 거절되었습니다", Toast.LENGTH_SHORT).show()
                requestPermission()
            }
        }
    }
}