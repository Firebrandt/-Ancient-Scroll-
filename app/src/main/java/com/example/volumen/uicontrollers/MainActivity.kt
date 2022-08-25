package com.example.volumen.uicontrollers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.work.*
import com.example.volumen.R
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.application.MyApplication
import com.example.volumen.data.Article
import com.example.volumen.databinding.ActivityMainBinding
import com.example.volumen.viewModels.ItemViewModel
import com.example.volumen.viewModels.ItemViewModelFactory
import com.example.volumen.work.worker.BackgroundLoadWorker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainActivity"
private const val LOAD_IN_BACKGROUND = "Load article data from background"
// Used for instrumentation tests.
const val BACKGROUND_LOAD_CHANNEL_ID = "Background Load"

class MainActivity : AppCompatActivity() {

    private lateinit var workManager : WorkManager
    private val viewModel: ItemViewModel by viewModels {
        val appDatabase = (application as MyApplication).appDatabase
        ItemViewModelFactory(appDatabase.getArticleDao(),
            appDatabase.getImageUrlsDao(),
            appDatabase.getJunctionsDao())
    }

    private lateinit var binding : ActivityMainBinding

    // Used to track the status of loading articles from various sources.
    private lateinit var backgroundLoadStatus : LiveData<MutableList<WorkInfo>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()
        workManager = WorkManager.getInstance(this)

        // Set up the action bar title
        supportActionBar?.title = getString(R.string.app_name)

        // Set up cleaner swipe/back button behaviour.
        binding.slidingPane.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED_CLOSED
        this.onBackPressedDispatcher.addCallback(SlidingPaneOnBackPressedCallback(binding.slidingPane))

        // Now set up the recycler view and load in the data. Cached stuff first, then online stuff.
        val adapter = ItemListAdapter(::handleClickedListItem)

        lifecycleScope.launch(Dispatchers.IO) {
            // Tell espresso to wait for initial loading to finish before continuing:
            Log.d(TAG, "onCreate: Idling Resource Incremented")
            articleIdlingRes.increment()

            // Set up the recycler view adapter
            binding.itemList.adapter = adapter
            binding.itemList.layoutManager = LinearLayoutManager(this@MainActivity)

            // Try loading previously retrieved articles from the Room Database, if applicable.
            val dataset = viewModel.getCachedData() as MutableList<Article>
            withContext(Dispatchers.Main){
                adapter.submitList(dataset)
            }
            Log.d(TAG, "onCreate: Dataset after cache check is $dataset")



            if (dataset.isEmpty()) {
                // If the dataset is EMPTY, we have no articles, load some new ones from the background.
                Snackbar.make(binding.root, "We found nothing in the database, and will instead fetch" +
                        "stuff from the internet. This WILL TAKE A WHILE, so exit the app and come back " +
                        "to it later.", Snackbar.LENGTH_LONG).show()
                loadOnlineArticle()
            }

            Log.d(TAG, "onCreate: Idling Resource Decremented")
            articleIdlingRes.decrement()
        }

        Log.d(TAG, "onCreate: Attempting to set observer. It should be up soonish?")
        backgroundLoadStatus = workManager.getWorkInfosForUniqueWorkLiveData(LOAD_IN_BACKGROUND)
        backgroundLoadStatus.observe(this) {
            Log.d(TAG, "Load status changed: $it")
            it?.let {
                // We only queued one request/task.
                if (it.isNotEmpty()) {
                    val workState = it[0]

                    lifecycleScope.launch(Dispatchers.IO) {
                        val dataset = viewModel.getCachedData() as MutableList<Article>
                        if (workState.state == WorkInfo.State.SUCCEEDED) {
                            withContext(Dispatchers.Main) {
                                // submitList only updates on a NEW list, so we circumvent a Google technicality.
                                adapter.submitList(dataset.toList())
                                // Tell espresso the loading is done and we're good :)
                                // Sometimes workState.state == SUCCEEDED when we restart, randomly.
                                // Only decrement when we actually have no data, and the idling resource
                                // is not idle. The reason I do this is because this observer can fire
                                // When we don't expect it to (despite there not actually being a thing
                                // that espresso has to wait for), due to residual WorkState statuses.
                                if (dataset.isNotEmpty() && !articleIdlingRes.isIdleNow) {
                                    Log.d(TAG, "onCreate: Idling Resource Decremented")
                                    articleIdlingRes.decrement()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadOnlineArticle(){
        /** Fetches an Article from the internet using WorkManager to perform the long-running
         * task while the app is in the background (possibly. It will still work while the app
         * is in the foreground).
         *
         * Adds the article to the given dataset list.
         */
        // Tell espresso to wait for articles to fully
        // complete before continuing (decremented when
        // the article is received and the list updated).
        Log.d(TAG, "loadOnlineArticle: Idling Resource Incremented")
        articleIdlingRes.increment()
        val backgroundLoadRequest = OneTimeWorkRequestBuilder<BackgroundLoadWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        // Enqueue it as unique work so we don't try to enact it several times at once
        // if something weird happens.
        workManager.enqueueUniqueWork(
            LOAD_IN_BACKGROUND,
            ExistingWorkPolicy.REPLACE, backgroundLoadRequest)

        Log.i(TAG, "onCreate: We've enqueued the online load work. Take a break!")
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
        // inflates INTO this activity's menu variable.
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_everything_item -> {
                // We actually want to clear everything. So, the screen too!
                viewModel.clearCache()
                (binding.itemList.adapter as ItemListAdapter).submitList(listOf())
                Snackbar.make(binding.root, "We have cleared the cache and everything!",
                    Snackbar.LENGTH_LONG).show()
                true
            }
            R.id.load_background_item -> {
                // Load another article. Let the user know this'll take a while.
                loadOnlineArticle()
                Snackbar.make(binding.root, "Hey, this could take a while. Feel free to do something else" +
                        "and come back later, a new article should be loaded in by then.", Snackbar.LENGTH_LONG).show()
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

    private fun createNotificationChannel() {
        /** A helper to create the notification channel for the fairly basic notification(s) used
         * in this app. Best done at app startup since: repeating this does nothing, and
         * because we need the channel created before sending any notifications.
         *
         * Necessary for API 26+.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Make the channel
            val channelName = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(BACKGROUND_LOAD_CHANNEL_ID, channelName,
            importance).apply {
                description = descriptionText
            }
            // Actually register the channel w/ the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
             as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }

    }

    companion object {
        /** The Idling Resource used to sync Espresso tests w/ article loading. Use for tests only! */
        val articleIdlingRes: CountingIdlingResource = CountingIdlingResource("Loading Articles")
    }

}


