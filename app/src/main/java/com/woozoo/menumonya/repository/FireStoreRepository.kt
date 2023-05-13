package com.woozoo.menumonya.repository

import com.woozoo.menumonya.model.Restaurant

interface FireStoreRepository {

    suspend fun getRestaurantInLocation(location: String): ArrayList<Restaurant>
}