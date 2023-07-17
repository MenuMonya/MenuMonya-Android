package com.woozoo.menumonya.ui.screen

import com.google.common.truth.Truth.assertThat
import com.woozoo.menumonya.data.model.Region
import com.woozoo.menumonya.data.repository.DataStoreRepository
import com.woozoo.menumonya.data.repository.FireStoreRepository
import com.woozoo.menumonya.data.repository.RemoteConfigRepository
import com.woozoo.menumonya.util.AnalyticsUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @Mock
    lateinit var fireStoreRepository: FireStoreRepository
    @Mock
    lateinit var remoteConfigRepository: RemoteConfigRepository
    @Mock
    lateinit var dataStoreRepository: DataStoreRepository
    @Mock
    lateinit var analyticsUtils: AnalyticsUtils

    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setUp() {
        // ViewModel 생성
        mainViewModel = MainViewModel(
            fireStoreRepository,
            remoteConfigRepository,
            dataStoreRepository,
            analyticsUtils
        )
    }

    @Test
    fun `마지막 선택 지역이 강남이었을 때 지역 버튼 데이터 제대로 생성하는지 테스트`() = runTest {
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
    fun `마지막 선택 지역이 역삼이었을 때 지역 버튼 데이터 제대로 생성하는지 테스트`() = runTest {
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
    fun `마지막 선택 지역이 역삼이었을 때 지역 버튼 데이터 잘못 생성된 경우의 테스트`() = runTest {
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

/**
 * when()에 들어갈 객체는 Mock 객체여야만 한다!
 * verify()에 들어갈 객체는 Mock 객체여야만 한다!
 * 위와 같은 경우 mainViewModel은 Mock 객체가 아니기 때문에 when(), verify()가 불가능함.
 */