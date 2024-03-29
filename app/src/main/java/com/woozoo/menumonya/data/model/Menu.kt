package com.woozoo.menumonya.data.model

data class Menu (
    val restaurantId: String = "",
    val restaurantLocationCategory: ArrayList<String> = ArrayList(),
    val restaurantName: String = "",
    val date: Map<String, Food> = mapOf(),
    val updatedTime: String = ""
)