package com.woozoo.menumonya.data.repository

import com.woozoo.menumonya.data.model.Region
import com.woozoo.menumonya.data.model.Restaurant

interface FireStoreRepository {

    suspend fun getRestaurantInRegion(region: String): ArrayList<Restaurant>

    suspend fun getRegionList(): ArrayList<Region>

    suspend fun getReportButtonText(): ArrayList<String>
}