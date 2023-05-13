package com.woozoo.menumonya.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.woozoo.menumonya.model.Food
import com.woozoo.menumonya.model.Menu
import com.woozoo.menumonya.model.Restaurant
import com.woozoo.menumonya.util.DateUtils.Companion.getTodayDate
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
    private lateinit var menuCollectionName: String

    override suspend fun getRestaurantInLocation(location: String) = withContext(Dispatchers.IO) {
        restaurantCollectionName = remoteConfigRepository.getRestaurantsCollectionNameConfig()

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

                val todayMenu: Food? = menu.date.get(getTodayDate())
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

    override suspend fun getMenu(restaurantId: String) = withContext(Dispatchers.IO) {
        menuCollectionName = remoteConfigRepository.getMenuCollectionNameConfig()

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