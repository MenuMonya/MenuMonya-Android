package com.woozoo.menumonya

import com.naver.maps.geometry.LatLng

object Constants {
    const val REGION_REPORT = "지역건의"
    const val REGION_BUTTON_TYPE = 0
    const val REGION_REPORT_TYPE = 1

    val LATLNG_GN = LatLng(37.4981647, 127.0283079)
    val LATLNG_YS = LatLng(37.5007163, 127.0366182)
    const val MAP_MIN_ZOOM = 13.0
    const val MAP_DEFAULT_ZOOM = 14.5

    const val GLIDE_IMAGE_SIZE_WIDTH = 100
    const val GLIDE_IMAGE_SIZE_HEIGHT = 100

    const val REMOTE_CONFIG_FETCH_INTERVAL: Long = 60 * 1  // 1분마다 업데이트 함
}