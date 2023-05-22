package com.woozoo.menumonya.model

import com.woozoo.menumonya.Constants.Companion.REGION_BUTTON_TYPE

data class Region(
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val viewType: Int = REGION_BUTTON_TYPE
)
