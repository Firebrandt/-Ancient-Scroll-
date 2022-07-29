package com.example.volumen

import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.data.Article
import com.example.volumen.databinding.ActivityMainBinding
import com.example.volumen.uicontrollers.MainActivity
import okhttp3.internal.wait
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern.matches

// Use this to set up idle resource registration so Espresso will synchronize with Async portions
// of the app.
val idleRegistry : IdlingRegistry = IdlingRegistry.getInstance()

// Specify a test runner that acc runs the tests.
@RunWith(AndroidJUnit4::class)
class InstrumentationTests {
    /** A class handling instrumentation tests for this app.
     *
     * While a lot of the time, individual functions don't have their own unit tests,
     * most of the UI related function(s) will be covered.
     */

    // Start the main activity before every test; we're testing it after all.
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    // Before every test, make sure to wait for the mainActivity to load some data...
    @Before
    fun wait_for_data() {
        val resource = MainActivity.getIdlingResourceInTest()
        idleRegistry.register(resource)
    }

    @Test
    fun details_back_swapped() {
        /** Test that pressing the back button correctly makes the list pane visible when
         * the details pane is visible (either by navigating back or from .close() doing nothing
         * due to a screen that displays both).
         */

        // Navigate to the details screen by clicking a recycler view item.
        onView(withId(R.id.item_list)).perform(RecyclerViewActions
            .actionOnItemAtPosition<ItemListAdapter.ItemViewHolder>(0, click()))

        // Then navigate back.
        pressBack()

        // Check that we're able to properly see the recycler view again.
        onView(withId(R.id.item_list)).check(matches(isDisplayed()))
    }

    @Test
    fun clicks_to_details_pane(){
        /** Test that when the recycler view's list items are clicked, the details pane becomes visible.
         * Either through navigation on smaller devices, default behaviour on larger ones.*/

        // Click a recycler view item.
        onView(withId(R.id.item_list)).perform(RecyclerViewActions
            .actionOnItemAtPosition<ItemListAdapter.ItemViewHolder>(0, click()))

        // Check that we can see the pane.
        onView(withId(R.id.details_pane)).check(matches(isDisplayed()))

    }

    @Test
    fun back_exits_properly(){
        /** Test that pressing back on the home screen correctly closes the app. **/
        pressBackUnconditionally()
        assertEquals("", Lifecycle.State.DESTROYED, activityRule.scenario.state)
    }

    @Test
    fun no_swipe_to_details_pane(){
        /** Test that upon swiping left, the list pane does not go invisible.
         * Encompasses locked pane on smaller devices, default on larger. */
        swipeLeft()
        onView(withId(R.id.item_list)).check(matches(isDisplayed()))
    }
}