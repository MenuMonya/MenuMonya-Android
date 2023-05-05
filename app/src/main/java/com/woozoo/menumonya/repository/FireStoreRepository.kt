package com.woozoo.menumonya.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.woozoo.menumonya.model.Menu
import com.woozoo.menumonya.model.Restaurant
import com.woozoo.menumonya.repository.RemoteConfigRepository.getMenuCollectionNameConfig
import com.woozoo.menumonya.repository.RemoteConfigRepository.getRestaurantsCollectionNameConfig
import com.woozoo.menumonya.util.DateUtils.Companion.getTodayDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FireStoreRepository {

    private lateinit var restaurantCollectionName: String
    private lateinit var menuCollectionName: String

    suspend fun getRestaurantInLocation(location: String) = withContext(Dispatchers.IO) {
        val db = Firebase.firestore

        restaurantCollectionName = getRestaurantsCollectionNameConfig()

        val restaurantInfo = ArrayList<Restaurant>()
        val restaurantRef = db.collection(restaurantCollectionName)
        val query = restaurantRef.whereArrayContainsAny("locationCategory", listOf(location))

        val result = query.get().await()
        val documents = result.documents

        for (document in documents) {
            val restaurant = document.toObject<Restaurant>()

            if (restaurant != null) {
                // 메뉴 정보 조회
                val menu = getMenu(document.id)

                val todayMenu = menu.date.get(getTodayDate())
                if (todayMenu != null) restaurant.todayMenu =  todayMenu

                restaurantInfo.add(restaurant)
            }
        }

        // locationCategoryOrder값으로 순서 재정렬(가까운 블록에 위치한 순서대로)
        for (restaurant in restaurantInfo) {
            restaurant.locationCategoryOrder.removeAll { !it.contains(location) }
        }
        restaurantInfo.sortBy { it.locationCategoryOrder[0] }

        restaurantInfo
    }

    suspend fun getMenu(restaurantId: String) = withContext(Dispatchers.IO) {
        val db = Firebase.firestore

        menuCollectionName = getMenuCollectionNameConfig()

        var menu = Menu()

        val menuRef = db.collection(menuCollectionName)
        val query = menuRef.whereEqualTo("restaurantId", restaurantId)

        val result = query.get().await()
        val documents = result.documents

        if (documents.size > 0) {
            menu = documents[0].toObject<Menu>()!!
        }

        menu
    }
}