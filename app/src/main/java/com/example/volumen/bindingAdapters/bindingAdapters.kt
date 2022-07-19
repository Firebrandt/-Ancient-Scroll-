package com.example.volumen.bindingAdapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.volumen.R

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String) {
    /** A binding adapter that handles the imageUrl custom ImageView attribute by either loading
     * the image into the view, or displaying a placeholder image if the url is null.
     */
    if (url != "null") {
        // TODO: load the image
    } else {
        view.setImageResource(R.drawable.ic_launcher_background)
    }
}
