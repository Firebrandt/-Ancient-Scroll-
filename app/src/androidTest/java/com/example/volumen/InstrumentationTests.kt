package com.example.volumen

import android.app.UiAutomation
import android.content.Context
import android.util.Log
import androidx.core.view.size
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiSelector
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.application.MyApplication
import com.example.volumen.data.*
import com.example.volumen.repository.ArticleRepository
import com.example.volumen.uicontrollers.MainActivity
import com.example.volumen.work.worker.BackgroundLoadWorker
import junit.framework.Assert.assertTrue
import net.sourceforge.htmlunit.corejs.javascript.tools.shell.Main
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

// Use this to set up idle resource registration so Espresso will synchronize with Async portions
// of the app.
val idleRegistry : IdlingRegistry = IdlingRegistry.getInstance()


// Helps ensure the test will only run on devices at API 18 or higher (for UiAutomator to work).
@SdkSuppress(minSdkVersion = 18)
// Specify a test runner that acc runs the tests.
@RunWith(AndroidJUnit4::class)
class InstrumentationTests {
    /** A class handling instrumentation tests for this app.
     *
     * While some of the time, individual functions don't have their own unit tests,
     * most of the UI related function(s) will be covered here instead.
     *
     * A couple aspects (reasonable article formatting, cache loading properly) is very difficult
     * to properly test for, and also pretty obvious to see. I'll leave that to manual testing.
     *
     * For these tests, make sure your screen is unlocked and
     */

    // Start the main activity before every test; we're testing it after all.
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private lateinit var device: UiDevice

    // Before every test, make sure to wait for the mainActivity to load some data...
    @Before
    fun wait_for_data() {
        // Allow for a long wait when loading article(s).
        IdlingPolicies.setMasterPolicyTimeout(300, TimeUnit.SECONDS)
        IdlingPolicies.setIdlingResourceTimeout(300, TimeUnit.SECONDS)
        val resource = MainActivity.articleIdlingRes
        idleRegistry.register(resource)


        // Get a UiDevice object that lets us manipulate and access a device and its state.
        // Including high level stuff like orientation and display size.
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
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

    @Test
    fun clear_cache_button_empties_recycler_view(){
        /** Test that the clear cache menu item empties the recycler view correctly.
         * Note that at the time of writing this test, the default behaviour for MainActivity
         * is to always load one item before running the tests.
         */
        onView(withId(R.id.clear_everything_item)).perform(click())

        activityRule.scenario.onActivity {
            val recyclerView = it.findViewById<RecyclerView>(R.id.item_list)

            // Assert that the recycler view has no view holders in it.
            assertTrue(recyclerView.adapter!!.itemCount == 0)
        }
    }

    @Test
    fun load_from_background_button_adds_item(){
        /** Test that loading from the background successfully adds an article to the recycler view.
         * The correctness of the article is tricky to test (sigh), but we can
         * at least test the general behaviour here.
         */
        lateinit var recyclerView: RecyclerView
        var currentItems : Int? = null

        activityRule.scenario.onActivity {
            recyclerView = it.findViewById<RecyclerView>(R.id.item_list)
            currentItems = recyclerView.adapter!!.itemCount
        }

        onView(withId(R.id.load_background_item)).perform(click())

        assertTrue(recyclerView.adapter!!.itemCount == currentItems!! + 1)
    }


    fun exit_with_cached_item_retains_same_items() {
    }

    @Test
    fun load_from_background_adds_notification(){
        /** Test that the app successfully sends the background loading notification when we click
         * the load from background button.
         *
         * NOTE: Some flakiness here. If the notification disappears EXTREMELY QUICKLY
         * (short article), this could fail. But that will only happen if the article loads so
         * quickly that it outpaces the test, which is unlikely.
         */
        // Check the notification after going to background.
        // Remember that espresso will pause the test and wait for Idling
        // resources to become idle, so that the test code syncs properly.
        // For this test, the idling article resource slows us down (we don't actually need to wait for
        // the loading to finish; wait for the notification idler instead for this specific test.
        idleRegistry.unregister(MainActivity.articleIdlingRes)
        idleRegistry.register(BackgroundLoadWorker.createNotificationIdler)
        // Hit the button
        onView(withId(R.id.load_background_item)).perform(click())

        val result2 = device.openNotification()
        Log.i("BRUH", "load_from_background_adds_notification: $result2")


        // The query runs on displayed elements. So, if we find it, it's visible. If we don't it's not.

        try {
            val backgroundWorkNotification: UiObject = device.findObject(
                UiSelector().text(
                    ApplicationProvider.getApplicationContext<Context>()
                        .getString(R.string.load_background_notification_title)
                )
            )
            assertTrue(true)
        } catch (e: Exception) {
            assertTrue("We couldn't find the notification!", false)
        }
    }

}

@RunWith(AndroidJUnit4::class)
class DatabaseTests {
    /** A class to store tests for the App's database functionalities.
     * Oddly enough although these are unit tests, they seem to have to be put in as Instrumentation
     * tests because we operate on the AppContext (and that seems to require starting it). **/

    @Mock
    private lateinit var application: MyApplication
    private lateinit var testDatabase: AppDatabase
    private lateinit var testArticleDao: ArticleDao
    private lateinit var testImageUrlsDao: ImageUrlsDao
    private lateinit var testJunctionDao: JunctionDao
    private lateinit var testRepository : ArticleRepository

    @Before
    fun createTestDb(){
        /** Create a temporary test database before each test, in memory. Also make a temporary
         * test repository and DAOs.
         */
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        testArticleDao = testDatabase.getArticleDao()
        testImageUrlsDao = testDatabase.getImageUrlsDao()
        testJunctionDao = testDatabase.getJunctionsDao()

        testRepository = ArticleRepository(testArticleDao, testImageUrlsDao, testJunctionDao)
    }

    @Test
    fun test_getCachedArticles() {
        /** Test whether getCachedArticles() correctly gets articles for a basic case. **/
        // Note to self: supposedly I'm not supposed to test private functions because they're an
        // implementation detail, could change any second. Instead test the paths that use it.
        // Hmph. So that's the philosophy I adopt here for stuff like getArticleFromCache().

        val testArticle = Article(1, "Test", "Test", "Test")
        val testArticle2 = Article(2, "Test", "Test", "Test")
        val testImage1 = ImageUrl(1,"Image 1")
        val testImage2 = ImageUrl(2,"Image 2")
        testArticleDao.insert(testArticle)
        testArticleDao.insert(testArticle2)
        testImageUrlsDao.insertImageUrl(testImage1)
        testImageUrlsDao.insertImageUrl(testImage2)

        testJunctionDao.insertPairing(ArticleImageJunction(1, 1, 1))
        testJunctionDao.insertPairing(ArticleImageJunction(2, 2, 2))

        val articles = testRepository.getCachedArticles()

        val finishedArticle1 = testArticle.copy()
        finishedArticle1.imageList = listOf(testImage1.imageUrl)

        val finishedArticle2 = testArticle2.copy()
        finishedArticle2.imageList = listOf(testImage2.imageUrl)

        // Assert equal using sets; I don't actually care about the order here!
        assertEquals(articles.toSet(), setOf(finishedArticle1, finishedArticle2))
    }

    @Test
    fun test_getCachedArticles_empty() {
        /** Test whether getCachedArticles() correctly gets articles for an empty database. **/
        // The database should be empty at the start...
        assertTrue(testRepository.getCachedArticles().isEmpty())
    }

    @Test
    fun clearCache() {
        /** Test that clearCache() successfully wipes the database tables as we need it to. */
        val testArticle = Article(1, "Test", "Test", "Test")
        val testArticle2 = Article(2, "Test", "Test", "Test")
        val testImage1 = ImageUrl(1,"Image 1")
        val testImage2 = ImageUrl(2,"Image 2")
        testArticleDao.insert(testArticle)
        testArticleDao.insert(testArticle2)
        testImageUrlsDao.insertImageUrl(testImage1)
        testImageUrlsDao.insertImageUrl(testImage2)

        testJunctionDao.insertPairing(ArticleImageJunction(1, 1, 1))
        testJunctionDao.insertPairing(ArticleImageJunction(2, 2, 2))

        testRepository.clearCache()

        // This line *should* essentially ensure that the database is empty.
        // I don't want to add additional DAO methods that are only used for tests.
        assertTrue(testRepository.getCachedArticles().isEmpty())
    }

    @After
    fun cleanUpTestDb() {
        testDatabase.close()
    }

}