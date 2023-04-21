package com.woozoo.menumeonya.model


data class Restaurant(
    val type: String = "",
    val imgUrl: String = "",
    val name: String = "",
    val location: Location = Location(),
    val locationCategory: ArrayList<String> = ArrayList<String>(),
    val locationCategoryOrder: ArrayList<String> = ArrayList<String>(),
    val price: Price = Price(),
    val time: Time = Time(),
    val updatedTime: String = ""
)

data class Location(
    val coord: Coord = Coord(),
    val description: String = "",
    val name: String = ""
)

data class Coord(
    val latitude: String = "",
    val longitude: String = ""
)

data class Price(
    val cardPrice: String = "",
    val cashPrice: String = "",
    val takeoutPrice: String = ""
)

data class Time(
    val openTime: String = "00:00",
    val closeTime: String = "00:00",
    val breakTime: String = "00:00"
)