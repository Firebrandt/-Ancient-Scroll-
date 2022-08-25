package com.example.volumen.work.worker

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.work.*
import com.example.volumen.R
import com.example.volumen.repository.ArticleRepository
import com.example.volumen.uicontrollers.BACKGROUND_LOAD_CHANNEL_ID
import com.example.volumen.uicontrollers.MainActivity

private const val OUTLINE_PAGE_TITLE = "Outline of ancient Rome"
private const val TAG = "BackgroundLoadWorker"
private const val BACKGROUND_LOAD_NOTIFICATION_ID = 137

class BackgroundLoadWorker(private val repository: ArticleRepository, private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    /** A worker that handles loading articles (directly to the UI), from the background.
     * Requires passing in a repository instance so it can access it to do the background loading.
     * Also requires the ListAdapter's adapter to submit a list for.
     *
     * I guess primarily intended for cases where we have nothing to read just yet, or we need
     * to get ten new articles. Otherwise, just load it from the cache!
     *
     * **/

    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        return try {
            repository.getRelatedPages(OUTLINE_PAGE_TITLE)
            Log.i(TAG, "doWork: Successfully loaded online articles to the database in" +
                    "the background!!!")

            Result.success()
        } catch (e : Exception) {
            Log.i(TAG, "doWork: For some reason loading in the background failed!")
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationIdler.increment()
        val foregroundInfo = ForegroundInfo(BACKGROUND_LOAD_NOTIFICATION_ID, createNotification())
        createNotificationIdler.decrement()
        return foregroundInfo
    }

    private fun createNotification(): Notification {
        /** Creates a notification to inform the user that we're loading an Article in the background.
         * used by some older versions of Android to start a ForegroundService when a WorkRequest is expedited
         * (as part of backwards compatibility, presumably it's a quirk of their implementation).
         */
        // Make an intent to navigate to the mainActivity (open the app).
        val intent = Intent(appContext, MainActivity::class.java)

        // Pass it as a pending intent to the notification so NotificationManager can execute it
        // with our app's permissions even if our app is dead at the time.
        val pendingIntent = PendingIntent.getActivity(
            appContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(appContext, BACKGROUND_LOAD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_placeholder_image_24)
            .setContentTitle(
                appContext
                    .getString(R.string.load_background_notification_title)
            )
            .setContentText(
                appContext
                    .getString(R.string.load_background_notification_text)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        // This is how we'd manually launch the notification:
        // NotificationManagerCompat.from(appContext).notify(BACKGROUND_LOAD_NOTIFICATION_ID,
        //    notification)
    }

    companion object {
        /** Idling resource used to sync Espresso tests w/ notification making. Use for tests only! */
        val createNotificationIdler = CountingIdlingResource("Sending Notification!")
    }
}

// This may not be the best place for it; this is supposed to handle UI code only,
// but to be fair this worker factory assists with the UI. So I think it's reasonably fair.
class BackgroundLoadWorkerFactory(private val repository: ArticleRepository) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {

        // We only have one worker here, so this works. Use a DelegatingWorkerFactory for several types of workers.
        return BackgroundLoadWorker(repository, appContext, workerParameters)
    }
}
