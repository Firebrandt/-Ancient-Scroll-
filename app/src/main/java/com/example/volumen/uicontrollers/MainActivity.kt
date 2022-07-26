package com.example.volumen.uicontrollers

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.data.Article
import com.example.volumen.databinding.ActivityMainBinding
import com.example.volumen.viewModels.ItemViewModel
import kotlinx.coroutines.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private val viewModel: ItemViewModel by viewModels()
    private lateinit var binding : ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the recycler view.
        val adapter = ItemListAdapter(::handleClickedListItem)
        binding.itemList.adapter = adapter
        binding.itemList.layoutManager = LinearLayoutManager(this)

        // Set up cleaner swipe/back button behaviour.
        binding.slidingPane.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED_CLOSED
        this.onBackPressedDispatcher.addCallback(SlidingPaneOnBackPressedCallback(binding.slidingPane))

        lifecycleScope.launch(Dispatchers.IO) {
            val dataset = viewModel.getDataSet()

            withContext(Dispatchers.Main) {
                adapter.submitList(dataset)
            }
        }


    }

    private fun handleClickedListItem(clickedArticle: Article) {
        /** Updates the view model with, and the UI in reaction to, a clicked list item (Article).
         * by expanding the details pane. Meant to be supplied as an onClickListener and not called
         * directly.
         */
        //TODO: Is property access like this OK? This is for the viewModel, not supplied to other classes.
        viewModel.currentArticle = clickedArticle
        binding.slidingPane.open()
    }

    class SlidingPaneOnBackPressedCallback(private val slidingPaneLayout: SlidingPaneLayout) :
        OnBackPressedCallback(slidingPaneLayout.isSlideable && slidingPaneLayout.isOpen),
            SlidingPaneLayout.PanelSlideListener {
        /** An OnBackPressedCallback that's used to ensure that the detailsPane can be closed with
         * the back button, when it's open. The listener (and constructor) ensures it is only
         * enabled in this case, hence why we combined the two.
         *
         * To use this, make sure to add it to the activity's onBackPressedDispatcher, which will
         * actually use this to handle back button presses.
         */

        init {
            // Make sure the sliding pane layout notifies this listener of slide events.
            slidingPaneLayout.addPanelSlideListener(this)
        }

        override fun handleOnBackPressed() {
            slidingPaneLayout.close()
        }

        override fun onPanelSlide(panel: View, slideOffset: Float) { }

        override fun onPanelOpened(panel: View) {
            isEnabled = true
        }

        override fun onPanelClosed(panel: View) {
            isEnabled = false
        }

    }
}

//// Generates some dummy list items
//val dummyArticle = Article(0, listOf(), "Tiny text!", "Dummy!", "No URL!")
//val dummyList = mutableListOf<Article>()
//repeat(5) {
//    dummyList.add(dummyArticle)
//}