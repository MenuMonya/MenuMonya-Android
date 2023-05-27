package com.woozoo.menumonya.model

import com.woozoo.menumonya.Constants.Companion.REGION_BUTTON_TYPE

data class Region(
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val regionId: Int = -1, // 데이터 정렬 순서(보여주고싶은 순서 변경 용도로 사용, DB 수정을 통해 제어하기 위함)
    val viewType: Int = REGION_BUTTON_TYPE
)
