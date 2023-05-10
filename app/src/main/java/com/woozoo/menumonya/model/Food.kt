package com.woozoo.menumonya.model

data class Food(
    val main: String = "",
    val side: String = "",
    val dessert: String = "",
    val imageUrl: String = "",
    val provider: String = "" // 메뉴 제공자
)
