package com.siri.travelguidemultiswipedbrealtime

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith
import androidx.test.rule.ActivityTestRule
import kotlinx.android.synthetic.main.activity_main.fab
import org.hamcrest.Matchers
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun mainActivityTest() {

        val fabBtn = Espresso.onView(
            Matchers.allOf(
                withId(R.id.fab),
                ViewMatchers.isDisplayed()
            )
        )

        fabBtn.perform(ViewActions.click())


    }
}