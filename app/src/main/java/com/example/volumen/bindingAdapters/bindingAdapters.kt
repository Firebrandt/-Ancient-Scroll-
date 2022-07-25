package com.example.volumen.bindingAdapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import com.example.volumen.R

//TODO: Adding error handling would be nice here to give the user some status updates.

// TODO: Also, maybe make a listData attribute in data binding so that ostensibly all the
//  UI Info is in the file.
@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    /** A binding adapter that handles the imageUrl custom ImageView attribute by either loading
     * the image into the view, or displaying a placeholder image if the url is null.
     */
    view.load(url) {
        fallback(R.drawable.ic_placeholder_image_24)
        placeholder(R.drawable.ic_placeholder_image_24)
        error(R.drawable.ic_image_error_off_24)
    }
    view.setImageResource(R.drawable.ic_placeholder_image_24)
}
