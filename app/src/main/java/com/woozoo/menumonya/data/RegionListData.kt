package com.woozoo.menumonya.data

import com.woozoo.menumonya.data.model.Region

class RegionListData {

    companion object {
        val regionList = arrayListOf(
            Region(name="강남", latitude=37.49794137321272, longitude=127.02758180118178, regionId=0, viewType=0),
            Region(name="역삼", latitude=37.5008310629869, longitude=127.0368902426878, regionId=1, viewType=0),
            Region(name="선릉", latitude=37.5045028, longitude=127.0489425, regionId=2, viewType=0),
            Region(name="가산디지털단지", latitude=37.4755197, longitude=126.8845077, regionId=3, viewType=0),
            Region(name="성수", latitude=37.5460092, longitude=127.0521253, regionId=4, viewType=0),
            Region(name="문정", latitude=37.4833447, longitude=127.119679, regionId=5, viewType=0)
        )

        fun getLocalRegionList(): ArrayList<Region> {
            return regionList
        }
    }
}