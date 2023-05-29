package com.woozoo.menumonya.repository

import com.woozoo.menumonya.model.Region
import com.woozoo.menumonya.model.Restaurant

interface FireStoreRepository {

    suspend fun getRestaurantInRegion(region: String): ArrayList<Restaurant>

    suspend fun getRegionList(): ArrayList<Region>
}