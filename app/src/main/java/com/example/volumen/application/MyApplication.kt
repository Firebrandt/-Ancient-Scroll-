package com.example.volumen.application

import android.app.Application
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.example.volumen.R
import com.example.volumen.data.AppDatabase
import com.example.volumen.repository.ArticleRepository
import com.example.volumen.uicontrollers.MainActivity
import com.example.volumen.work.worker.BackgroundLoadWorker
import com.example.volumen.work.worker.BackgroundLoadWorkerFactory


class MyApplication : Application(), ImageLoaderFactory, Configuration.Provider {
    /** A custom application class used to provide reference to a few global variables and override
     * a couple methods across the entire application, at app start.
      */

    // Configure the singleton ImageLoader used by coil in this app in one place (by overriding the factory).
    // That singleton is used by the imageView.load() extension function.
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .placeholder(R.drawable.ic_placeholder_image_24)
            .error(R.drawable.ic_image_error_off_24)
            .fallback(R.drawable.ic_placeholder_image_24)
            .crossfade(true)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    // Expose the app database, which will create it on the disk at first access.
    val appDatabase: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    // Create a custom configuration for workManager, so we can make custom workers w/ new attributes.
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(BackgroundLoadWorkerFactory(ArticleRepository(
                appDatabase.getArticleDao(),
                appDatabase.getImageUrlsDao(),
                appDatabase.getJunctionsDao()
            )))
            .build()
    }

}