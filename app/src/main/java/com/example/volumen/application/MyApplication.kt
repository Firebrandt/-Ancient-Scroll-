package com.example.volumen.application

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.example.volumen.R

class MyApplication : Application(), ImageLoaderFactory {

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
}