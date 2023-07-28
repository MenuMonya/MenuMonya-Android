package com.woozoo.menumonya.ui.screen

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import com.woozoo.menumonya.R
import com.woozoo.menumonya.ui.adapter.RegionAdapter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@LargeTest
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun `region_button_click_test`() {
        onView(ViewMatchers.isRoot()).perform(waitFor(3000))

        // First, scroll to the position that needs to be matched and click on it.
        onView(withId(R.id.region_rv))
            .perform(
                RecyclerViewActions.actionOnItem<RegionAdapter.RegionButtonViewHolder>(
                    hasDescendant(withText("역삼")),
                    click()
                )
            )

        onView(ViewMatchers.isRoot()).perform(waitFor(2000))

        onView(withId(R.id.region_rv))
            .perform(
                RecyclerViewActions.actionOnItem<RegionAdapter.RegionButtonViewHolder>(
                    hasDescendant(withText("문정")),
                    click()
                )
            )

        onView(ViewMatchers.isRoot()).perform(waitFor(2000))
    }

    /**
     * 테스트 코드 TODO
     * - 클릭된 마커의 식당과 RecyclerView에 표시된 식당이 일치하는지 테스트
     * - 메뉴가 많았을 때 제대로 표시되는지 테스트
     */

    private fun waitFor(delay: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = ViewMatchers.isRoot()

            override fun getDescription(): String = "wait for $delay milliseconds"

            override fun perform(uiController: UiController, view: View?) {
                uiController.loopMainThreadForAtLeast(delay)
            }
        }
    }
}