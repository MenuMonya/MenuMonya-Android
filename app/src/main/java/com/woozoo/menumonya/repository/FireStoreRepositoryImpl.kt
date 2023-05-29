package com.woozoo.menumonya.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.woozoo.menumonya.model.Region
import com.woozoo.menumonya.model.ReportButtonText
import com.woozoo.menumonya.model.Restaurant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FireStoreRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val remoteConfigRepository: RemoteConfigRepository
): FireStoreRepository {

    private lateinit var restaurantCollectionName: String

    override suspend fun getRestaurantInRegion(region: String): ArrayList<Restaurant> {
        restaurantCollectionName = remoteConfigRepository.getRestaurantsCollectionNameConfig()

        val restaurantInfo = ArrayList<Restaurant>()
        val restaurantRef = db.collection(restaurantCollectionName)
        val query = restaurantRef.whereArrayContainsAny("locationCategory", listOf(region))

        val result = query.get().await()
        val documents = result.documents

        for (document in documents) {
            val restaurant = document.toObject<Restaurant>()

            if (restaurant != null) restaurantInfo.add(restaurant)
        }

        // locationCategoryOrder값으로 순서 재정렬(가까운 블록에 위치한 순서대로)
        for (restaurant in restaurantInfo) {
            restaurant.locationCategoryOrder.removeAll { !it.contains(region) }
        }
        restaurantInfo.sortBy { it.locationCategoryOrder[0] }

        return restaurantInfo
    }

    override suspend fun getRegionList(): ArrayList<Region> {
        val regionInfo = ArrayList<Region>()
        val regionRef = db.collection("regions")

        val result = regionRef.get().await()
        val documents = result.documents

        for (document in documents) {
            val region = document.toObject<Region>()

            if (region != null) regionInfo.add(region)
        }

        return regionInfo
    }

    override suspend fun getReportButtonText(): ArrayList<String> {
        val reportButtonTextList = ArrayList<String>()
        val reportButtonTextRef = db.collection("menu-report-text")

        val result = reportButtonTextRef.get().await()
        val documents = result.documents

        for (document in documents) {
            val text = document.toObject<ReportButtonText>()

            if (text != null && text.description != "") reportButtonTextList.add(text.description)
        }

        return reportButtonTextList
    }
}