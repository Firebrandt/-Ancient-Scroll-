package com.example.volumen.uicontrollers

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import androidx.test.espresso.idling.CountingIdlingResource
import com.example.volumen.R
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.application.MyApplication
import com.example.volumen.data.Article
import com.example.volumen.databinding.ActivityMainBinding
import com.example.volumen.viewModels.ItemViewModel
import com.example.volumen.viewModels.ItemViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainActivity"

// Used for instrumentation tests.
val articleIdlingRes: CountingIdlingResource = CountingIdlingResource("Loading Articles")

class MainActivity : AppCompatActivity() {
    private val viewModel: ItemViewModel by viewModels(){
        val appDatabase = (application as MyApplication).appDatabase
        ItemViewModelFactory(appDatabase.getArticleDao(),
            appDatabase.getImageUrlsDao(),
            appDatabase.getJunctionsDao())
    }

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Set up the action bar title
        supportActionBar?.title = getString(R.string.app_name)

        // Set up cleaner swipe/back button behaviour.
        binding.slidingPane.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED_CLOSED
        this.onBackPressedDispatcher.addCallback(SlidingPaneOnBackPressedCallback(binding.slidingPane))

        // Now set up the recycler view and load in the data. Cached stuff first, then online stuff.
        lifecycleScope.launch(Dispatchers.IO) {
            // Tell espresso to wait for this task to finish before continuing:
            articleIdlingRes.increment()

            // TODO: Clear the cache for now.
            // viewModel.clearCache()

            val adapter = ItemListAdapter(::handleClickedListItem)
            binding.itemList.adapter = adapter
            binding.itemList.layoutManager = LinearLayoutManager(this@MainActivity)

            val dataset = viewModel.getCachedData() as MutableList<Article>


            Log.d(TAG, "onCreate: Dataset after cache is $dataset")
            withContext(Dispatchers.Main) {
                adapter.submitList(dataset)
            }

            val afterOnlineLoadData = dataset + viewModel.getOnlineData()


            Log.d(TAG, "onCreate: Dataset after online loading is ${dataset} ")

            withContext(Dispatchers.Main) {
                // We have to make a new list here, because submitList will IGNORE resubmissions of
                // the same list object. Even if the lists really are different. :(
                adapter.submitList(afterOnlineLoadData)
            }

            Log.i(TAG, "onCreate: Finished loading MainActivity recycler view :)")
            // Tell espresso the loading is done and we're good :)
            articleIdlingRes.decrement()
        }


    }

    private fun handleClickedListItem(clickedArticle: Article) {
        /** Updates the view model with, and the UI in reaction to, a clicked list item (Article).
         * by expanding the details pane and changing the app bar title.
         * Meant to be supplied as an onClickListener and not called directly.
         */
        Log.i(TAG, "handleClickedListItem: Handling the article recycler card click!")
        viewModel.updateCurrentArticle(clickedArticle)
        Log.i(TAG, "handleClickedListItem: By the way the article is ${viewModel.currentArticle.value}")

        // Format the article title to a proper title w/ capitalization
        val actualTitle = viewModel.currentArticle.value?.title?.split(" ")!!.toMutableList()
        for (i in 0 until actualTitle.size){
            val uppercasedTitle = actualTitle[i].toCharArray()
            uppercasedTitle[0] = uppercasedTitle[0].uppercaseChar()
            actualTitle[i] = uppercasedTitle.joinToString("")
        }
        supportActionBar?.title = actualTitle.joinToString(" ")
        binding.slidingPane.open()
    }

    // Sets up the menu and its items for the MainActivity by inflating a resource file.
    // Note: although I don't use it here, I can UPDATE the created instance of the options menu
    // via onPrepareOptionsMenu(), if I want to modify it after creation. Don't just re-call this.
    // Prompt onPrepareOptionsMenu() by calling invalidateOptionsMenu().
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // inflates INTO this activity's menu
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_cache_item -> {
                viewModel.clearCache()
                true
            }
            R.id.load_background_item -> {
                // TODO: Load stuff in the background here.
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class SlidingPaneOnBackPressedCallback(private val slidingPaneLayout: SlidingPaneLayout) :
        OnBackPressedCallback(slidingPaneLayout.isSlideable && slidingPaneLayout.isOpen),
            SlidingPaneLayout.PanelSlideListener {
        /** An OnBackPressedCallback that's used to ensure that the detailsPane can be closed with
         * the back button, when it's open. The listener (and constructor) ensures it is only
         * enabled in this case, hence why we combined the two. Also change the action bar title
         * appropriately.
         *
         * To use this, make sure to add it to the activity's onBackPressedDispatcher, which will
         * actually use this to handle back button presses.
         */

        init {
            // Make sure the sliding pane layout notifies this listener of slide events.
            slidingPaneLayout.addPanelSlideListener(this)
        }

        override fun handleOnBackPressed() {
            supportActionBar?.title = getString(R.string.app_name)
            slidingPaneLayout.close()
        }

        // TODO: Consider adding an actual toolbar. Apparently that's the way to set up things? (Action bar for simple stuff maybe).
        //  May wish to consult the android basics course.
        override fun onPanelSlide(panel: View, slideOffset: Float) { }

        override fun onPanelOpened(panel: View) {
            isEnabled = true
        }

        override fun onPanelClosed(panel: View) {
            isEnabled = false
        }

    }

    companion object {
        fun getIdlingResourceInTest(): CountingIdlingResource {
            /** Return MainActivity's article-getting idling resource. For test purposes only. */
            return articleIdlingRes
        }
    }
}

