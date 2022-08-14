package com.example.volumen.work.worker

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.lang.Exception

private const val OUTLINE_PAGE_TITLE = "Outline of ancient Rome"
private const val TAG = "BackgroundLoadWorker"

class BackgroundLoadWorker(val repository: ArticleRepository, appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    /** A worker that handles loading articles (directly to the UI), from the background.
     * Requires passing in a repository instance so it can access it to do the background loading.
     * Also requires the ListAdapter's adapter to submit a list for.
     *
     * I guess primarily intended for cases where we have nothing to read just yet, or we need
     * to get ten new articles. Otherwise, just load it from the cache!
     *
     * **/

    override suspend fun doWork(): Result {

        return try {
            val articles = repository.getRelatedPages(OUTLINE_PAGE_TITLE)
            Log.i(TAG, "doWork: Successfully loaded online articles to the database in" +
                    "the background!!!")

            Result.success()
        } catch (e : Exception) {
            Log.i(TAG, "doWork: For some reason loading in the background failed!")
            Result.failure()
        }
    }

}

// This may not be the best place for it; this is supposed to handle UI code only,
// but to be fair this worker factory assists with the UI. So I think it's reasonably fair.
class BackgroundLoadWorkerFactory(private val repository: ArticleRepository) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {

        // We only have one worker here, so this works. Use a DelegatingWorkerFactory for several types of workers.
        return BackgroundLoadWorker(repository, appContext, workerParameters)
    }
}
