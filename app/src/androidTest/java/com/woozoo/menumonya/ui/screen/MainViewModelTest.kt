package com.woozoo.menumonya.ui.screen

import com.google.common.truth.Truth.assertThat
import com.woozoo.menumonya.data.model.Region
import com.woozoo.menumonya.data.repository.DataStoreRepository
import com.woozoo.menumonya.data.repository.FireStoreRepository
import com.woozoo.menumonya.data.repository.RemoteConfigRepository
import com.woozoo.menumonya.util.AnalyticsUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fireStoreRepository: FireStoreRepository
    @Inject
    lateinit var remoteConfigRepository: RemoteConfigRepository
    @Mock
    lateinit var dataStoreRepository: DataStoreRepository
    @Inject
    lateinit var analyticsUtils: AnalyticsUtils

    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setUp() {
        // Injection
        hiltRule.inject()
        // ViewModel 생성
        mainViewModel = MainViewModel(
            fireStoreRepository,
            remoteConfigRepository,
            dataStoreRepository,
        )
    }

    @Test
    fun test1() = runTest {
        // (1) Given
        val mockRegionList = arrayListOf(
            Region(name="문정", latitude=37.4833447, longitude=127.119679, regionId=5, viewType=0),
            Region(name="성수", latitude=37.5460092, longitude=127.0521253, regionId=4, viewType=0),
            Region(name="가산디지털단지", latitude=37.4755197, longitude=126.8845077, regionId=3, viewType=0),
            Region(name="강남", latitude=37.49794137321272, longitude=127.02758180118178, regionId=0, viewType=0),
            Region(name="역삼", latitude=37.5008310629869, longitude=127.0368902426878, regionId=1, viewType=0),
            Region(name="선릉", latitude=37.5045028, longitude=127.0489425, regionId=2, viewType=0)
        )
        val mockModifiedRegionData = arrayListOf(
            Region(name="강남", latitude=37.49794137321272, longitude=127.02758180118178, regionId=0, viewType=0),
            Region(name="역삼", latitude=37.5008310629869, longitude=127.0368902426878, regionId=1, viewType=0),
            Region(name="선릉", latitude=37.5045028, longitude=127.0489425, regionId=2, viewType=0),
            Region(name="가산디지털단지", latitude=37.4755197, longitude=126.8845077, regionId=3, viewType=0),
            Region(name="성수", latitude=37.5460092, longitude=127.0521253, regionId=4, viewType=0),
            Region(name="문정", latitude=37.4833447, longitude=127.119679, regionId=5, viewType=0),
            Region(name="지역건의", latitude=0.0, longitude=0.0, regionId=999, viewType=1)
        )
        // Stubbing
        `when`(dataStoreRepository.getLastSelectedRegion()).thenReturn("강남")

        // (2) When
        val modifiedRegionList = mainViewModel.modifyRegionData(mockRegionList)

        // (3) Then
        verify(dataStoreRepository).getLastSelectedRegion()
        assertThat(modifiedRegionList).isEqualTo(mockModifiedRegionData)
    }

    @Test
    fun test2() = runTest {
        // (1) Given
        val mockRegionList = arrayListOf(
            Region(name="문정", latitude=37.4833447, longitude=127.119679, regionId=5, viewType=0),
            Region(name="성수", latitude=37.5460092, longitude=127.0521253, regionId=4, viewType=0),
            Region(name="가산디지털단지", latitude=37.4755197, longitude=126.8845077, regionId=3, viewType=0),
            Region(name="강남", latitude=37.49794137321272, longitude=127.02758180118178, regionId=0, viewType=0),
            Region(name="역삼", latitude=37.5008310629869, longitude=127.0368902426878, regionId=1, viewType=0),
            Region(name="선릉", latitude=37.5045028, longitude=127.0489425, regionId=2, viewType=0)
        )
        val mockModifiedRegionData = arrayListOf(
            Region(name="역삼", latitude=37.5008310629869, longitude=127.0368902426878, regionId=1, viewType=0),
            Region(name="강남", latitude=37.49794137321272, longitude=127.02758180118178, regionId=0, viewType=0),
            Region(name="선릉", latitude=37.5045028, longitude=127.0489425, regionId=2, viewType=0),
            Region(name="가산디지털단지", latitude=37.4755197, longitude=126.8845077, regionId=3, viewType=0),
            Region(name="성수", latitude=37.5460092, longitude=127.0521253, regionId=4, viewType=0),
            Region(name="문정", latitude=37.4833447, longitude=127.119679, regionId=5, viewType=0),
            Region(name="지역건의", latitude=0.0, longitude=0.0, regionId=999, viewType=1)
        )
        // Stubbing
        `when`(dataStoreRepository.getLastSelectedRegion()).thenReturn("역삼")

        // (2) When
        val modifiedRegionList = mainViewModel.modifyRegionData(mockRegionList)

        // (3) Then
        verify(dataStoreRepository).getLastSelectedRegion()
        assertThat(modifiedRegionList).isEqualTo(mockModifiedRegionData)
    }

    @Test
    fun test3() = runTest {
        // (1) Given
        val mockRegionList = arrayListOf(
            Region(name="문정", latitude=37.4833447, longitude=127.119679, regionId=5, viewType=0),
            Region(name="성수", latitude=37.5460092, longitude=127.0521253, regionId=4, viewType=0),
            Region(name="가산디지털단지", latitude=37.4755197, longitude=126.8845077, regionId=3, viewType=0),
            Region(name="강남", latitude=37.49794137321272, longitude=127.02758180118178, regionId=0, viewType=0),
            Region(name="역삼", latitude=37.5008310629869, longitude=127.0368902426878, regionId=1, viewType=0),
            Region(name="선릉", latitude=37.5045028, longitude=127.0489425, regionId=2, viewType=0)
        )
        val mockModifiedRegionData = arrayListOf(
            Region(name="강남", latitude=37.49794137321272, longitude=127.02758180118178, regionId=0, viewType=0),
            Region(name="역삼", latitude=37.5008310629869, longitude=127.0368902426878, regionId=1, viewType=0),
            Region(name="선릉", latitude=37.5045028, longitude=127.0489425, regionId=2, viewType=0),
            Region(name="가산디지털단지", latitude=37.4755197, longitude=126.8845077, regionId=3, viewType=0),
            Region(name="성수", latitude=37.5460092, longitude=127.0521253, regionId=4, viewType=0),
            Region(name="문정", latitude=37.4833447, longitude=127.119679, regionId=5, viewType=0),
            Region(name="지역건의", latitude=0.0, longitude=0.0, regionId=999, viewType=1)
        )
        // Stubbing
        `when`(dataStoreRepository.getLastSelectedRegion()).thenReturn("역삼")

        // (2) When
        val modifiedRegionList = mainViewModel.modifyRegionData(mockRegionList)

        // (3) Then
        verify(dataStoreRepository).getLastSelectedRegion()
        assertThat(modifiedRegionList).isNotEqualTo(mockModifiedRegionData)
    }

}