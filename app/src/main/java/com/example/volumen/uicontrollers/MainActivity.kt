package com.example.volumen.uicontrollers

import android.os.Bundle
import android.util.Log
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
import com.example.volumen.data.Article
import com.example.volumen.databinding.ActivityMainBinding
import com.example.volumen.viewModels.ItemViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainActivity"

// Used for instrumentation tests.
val articleIdlingRes: CountingIdlingResource = CountingIdlingResource("Loading Articles")

class MainActivity : AppCompatActivity() {
    private val viewModel: ItemViewModel by viewModels()
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the action bar title
        supportActionBar?.title = getString(R.string.app_name)
        // Set up the recycler view.
        val adapter = ItemListAdapter(::handleClickedListItem)
        binding.itemList.adapter = adapter
        binding.itemList.layoutManager = LinearLayoutManager(this)

        // Set up cleaner swipe/back button behaviour.
        binding.slidingPane.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED_CLOSED
        this.onBackPressedDispatcher.addCallback(SlidingPaneOnBackPressedCallback(binding.slidingPane))

        lifecycleScope.launch(Dispatchers.IO) {
            // Tell espresso to wait for this task to finish before continuing:
            articleIdlingRes.increment()
            val dataset = viewModel.getDataSet()

            withContext(Dispatchers.Main) {
                adapter.submitList(dataset)
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
        binding.lifecycleOwner = this

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
            actionBar?.title = getString(R.string.app_name)
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

