package com.woozoo.menumonya.data.model


data class Restaurant(
    var type: String = "",
    var imgUrl: String = "",
    var name: String = "",
    var location: Location = Location(),
    var locationCategory: ArrayList<String> = ArrayList<String>(),
    var locationCategoryOrder: ArrayList<String> = ArrayList<String>(),
    var price: Price = Price(),
    var time: Time = Time(),
    var phoneNumber: String = "",
    var todayMenu: Food = Food(),
    var updatedTime: String = "",
    var menuAvailableOnline: Boolean = false,
)

data class Location(
    var coord: Coord = Coord(),
    var description: String = "",
    var name: String = ""
)

data class Coord(
    var latitude: String = "",
    var longitude: String = ""
)

data class Price(
    var cardPrice: String = "",
    var cashPrice: String = "",
    var takeoutPrice: String = ""
)

data class Time(
    var openTime: String = "00:00",
    var closeTime: String = "00:00",
    var breakTime: String = "00:00"
)